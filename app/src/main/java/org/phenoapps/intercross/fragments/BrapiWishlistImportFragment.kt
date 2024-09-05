package org.phenoapps.intercross.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brapi.v2.model.germ.BrAPICrossParent
import org.brapi.v2.model.germ.BrAPICrossingProject
import org.brapi.v2.model.germ.BrAPIPlannedCross
import org.phenoapps.intercross.R
import org.phenoapps.intercross.brapi.service.BrAPIService
import org.phenoapps.intercross.brapi.service.BrAPIServiceV2
import org.phenoapps.intercross.brapi.service.BrapiPaginationManager
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentBrapiImportBinding
import org.phenoapps.intercross.dialogs.WishlistImportDialog
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/*
Programs -> Crossing Project (programDbId) -> Crosses
Germplasm calls for parents
 */
class BrapiWishlistImportFragment: IntercrossBaseFragment<FragmentBrapiImportBinding>(R.layout.fragment_brapi_import) {

    private val parentsViewModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val wishViewModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private companion object {

        val TAG = BrapiWishlistImportFragment::class.simpleName

        const val CROSSPROJECTS = 0
        const val PLANNEDCROSSES = 1
    }

    /**
     * mFilterState is a state variable that represents which table we are filtering for.
     */
    private var mFilterState: Int = CROSSPROJECTS

    /**
     * Whenever a list row is chosen, it is added to a set of ids (or removed if already chosen).
     * When the next table is chosen, all the ids in the respective set will be used to query.
     */
    private var mProjects: BrAPICrossingProject? = null
    private var mPlannedCrosses = HashSet<BrAPIPlannedCross>()

    private val mScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    private val mService: BrAPIServiceV2 by lazy {

        BrAPIServiceV2(this@BrapiWishlistImportFragment.context)

    }

    private val mPaginationManager by lazy {

        BrapiPaginationManager(context)

    }

    private fun setupPageController() {

        with (mBinding.pageController) {

            this.setNextClick {

                mPaginationManager.setNewPage(nextButton.id)

                loadBrAPIData()

            }

            this.setPrevClick {

                mPaginationManager.setNewPage(prevButton.id)

                loadBrAPIData()

            }
        }
    }

    private fun importSelected() {

        mProjects?.let { crossProject ->

            mPaginationManager.reset()

            mBinding.progressVisibility = View.VISIBLE

            mService.getPlannedCrosses(crossProject.crossingProjectDbId, mPaginationManager, { planned ->

                mBinding.progressVisibility = View.GONE

                if (planned.isNotEmpty()) {

                    val crossProjectPlans = planned.filter { it.crossingProjectDbId == crossProject.crossingProjectDbId }
                    this@BrapiWishlistImportFragment.activity?.let { act ->

                        act.runOnUiThread {

                            WishlistImportDialog(
                                act, crossProject.crossingProjectName,
                                //filter planned crosses only in the selected cross project
                                crossProjectPlans) {

                                if (crossProjectPlans.isEmpty()) {

                                    Toast.makeText(
                                        act,
                                        R.string.fragment_brapi_import_no_planned_crosses_found,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else crossProjectPlans.forEach { plan ->

                                    //default min: 1 max: 10 type: cross
                                    //plan.additionalInfo

                                    val male: BrAPICrossParent
                                    val female: BrAPICrossParent

                                    //determine which parent is female/male
                                    if (plan.parent1.parentType.toString() == "MALE") {
                                        male = plan.parent1
                                        female = plan.parent2
                                    } else {
                                        male = plan.parent2
                                        female = plan.parent1
                                    }

                                    val infoMap = Gson().fromJson(plan.additionalInfo, HashMap::class.java)
                                    //insert wishlist row, check if brapi has wish metadata
                                    val wishMin = infoMap["wishMin"] as? String
                                    val wishMax = infoMap["wishMax"] as? String
                                    val wishType = infoMap["wishType"] as? String

                                    //TODO: need external references for both parent1 and parent2, otherwise brapi overwrites observationUnitDbId
                                    wishViewModel.insert(
                                        Wishlist(
                                            femaleDbId = female.observationUnitDbId,
                                            maleDbId = male.observationUnitDbId,
                                            femaleName = female.observationUnitName,
                                            maleName = male.observationUnitName,
                                            wishType = wishType ?: "cross",
                                            wishMin = wishMin?.toIntOrNull() ?: 1,
                                            wishMax = wishMax?.toIntOrNull() ?: 10
                                        )
                                    )

                                    //insert parents from plannedcross data
                                    parentsViewModel.insert(Parent(
                                        codeId = male.observationUnitDbId, 1
                                    ).also { it.name = male.observationUnitName },
                                        Parent(
                                            codeId = female.observationUnitDbId, 0
                                        ).also { it.name = female.observationUnitName })

                                }

                                findNavController().popBackStack()

                            }.apply {

                                //when the submit button is pressed, the dialog is dismissed and this is called
                                this.setOnDismissListener {

                                    this.disableProgress()

                                }

                                show()
                            }
                        }
                    }


            } else Toast.makeText(this.context, getString(R.string.fragment_brapi_import_no_planned_crosses_found),Toast.LENGTH_SHORT).show()

                null

            }) { fail ->

                mBinding.progressVisibility = View.GONE

                handleFailure(fail)
            }
        }
    }

    /**
     * For this import activity, the top bar shows the current table we are filtering for,
     * which navigates to the previous table on click (like a back button).
     * Similarly, the bottom bar moves to the next table but is like a select all query if no fields are chosen.
     */
    private fun setupTopAndBottomButtons() {

        mBinding.currentButton.setOnClickListener {
            //update UI with the new state
            loadBrAPIData()
        }

        mBinding.nextButton.setOnClickListener {

            when(mFilterState) {
                CROSSPROJECTS -> importSelected()
            }

           loadBrAPIData()
        }

    }

    /**
     * TODO: This is what field book uses, might need to be updated
     */
    private fun isConnected(context: Context): Boolean {

        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connMgr.activeNetworkInfo

        return networkInfo != null && networkInfo.isConnected
    }

    /**
     * Updates the top/bottom bar text fields with the current state.
     */
    private fun updateUi(state: Int) {

        mFilterState = state

        mBinding.currentButton.text = "Cross Projects"

        mBinding.nextButton.text = "Import Selected"

    }

    /**
     * TODO:
     * Crosses get call might need a cross project db id parameter
     */
    private fun loadCrossingProjects() {

        mPaginationManager.reset()

        mBinding.listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        mService.getCrossingProjects(mPaginationManager, { crosses ->

            mBinding.progressVisibility = View.GONE

            buildArrayAdapter(crosses.filter { it.crossingProjectName?.isNotEmpty() ?: false && it.crossingProjectDbId != null })

            null

        }) { fail ->

            mBinding.progressVisibility = View.GONE

            //todo error message

            handleFailure(fail)
        }
    }

    /**
     * Logs each time a failure api callback occurs
     */
    private fun handleFailure(fail: Int): Void? {

        Log.d(TAG, "BrAPI callback failed. $fail")

        return null //default fail callback return type for brapi

    }

    //async loads the brapi calls using coroutines
    //updates the top/bottom bar text fields depending on the state
    private fun loadBrAPIData() {

        updateUi(mFilterState)

        mScope.launch { //uses Dispatchers.IO for network background processing

            try {

                when (mFilterState) {
                    CROSSPROJECTS -> loadCrossingProjects()
                    //PLANNEDCROSSES -> loadPlannedCrosses()
                }

            } catch (cme: ConcurrentModificationException) {

                Log.d(TAG, cme.localizedMessage ?: "Async update error.")

                cme.printStackTrace()

            }
        }
    }

    //function used to toggle existence of an item in a set
    private fun <T> HashSet<T>.addOrRemove(item: T) {
        if (this.contains(item)) {
            this.remove(item)
        } else this.add(item)
    }

    /**
     * Updates the UI with the data parameter. Type T can be BrapiProgram, BrapiStudyDetails,
     * or ProgramTrialPair. All of which contain information to reconstruct the filter tree
     * from user input.
     */
    private fun <T> buildArrayAdapter(data: List<T>) {

        val listView = mBinding.listView

        //set up list item click event listener
        listView.setOnItemClickListener { _, _, position, _ ->

            when (val item = data[position]) {

                is BrAPICrossingProject -> mProjects = item
                //is BrAPIPlannedCross -> mPlannedCrosses.addOrRemove(item)

            }
        }

        val itemDataList: MutableList<Any?> = ArrayList()

        //load data into adapter
        data.forEach {

            when (it) {

                is BrAPICrossingProject -> it.crossingProjectName?.let { name -> itemDataList.add(name) }
                //is BrAPIPlannedCross -> it.plannedCrossName?.let { name -> itemDataList.add(name) }

            }
        }

        context?.let { ctx ->

            this@BrapiWishlistImportFragment.activity?.runOnUiThread {

                listView.adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_single_choice, itemDataList)

            }
        }
    }

    override fun FragmentBrapiImportBinding.afterCreateView() {

        activity?.let { act ->

            //check if device is connected to a network
            if (isConnected(act.applicationContext)) {

                //checks that the preference brapi url matches a web url
                if (BrAPIService.hasValidBaseUrl(act.applicationContext)) {

                    mBinding.serverTextView.text = BrAPIService.getBrapiUrl(act)

                    setupPageController()

                    setupTopAndBottomButtons()

                    loadBrAPIData()

                } else {

                    Toast.makeText(act.applicationContext, R.string.brapi_must_configure_url, Toast.LENGTH_SHORT).show()

                    findNavController().popBackStack()
                }

            } else {

                Toast.makeText(act.applicationContext, R.string.device_offline_warning, Toast.LENGTH_SHORT).show()

                findNavController().popBackStack()
            }
        }
    }
}