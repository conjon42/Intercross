package org.phenoapps.intercross.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_barcode_scan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brapi.v2.model.germ.BrAPICrossParent
import org.brapi.v2.model.germ.BrAPICrossingProject
import org.brapi.v2.model.germ.BrAPIPlannedCross
import org.phenoapps.intercross.R
import org.phenoapps.intercross.brapi.model.BrapiTrial
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
class BrapiTrialsFragment: IntercrossBaseFragment<FragmentBrapiImportBinding>(R.layout.fragment_brapi_import) {

    val args: BrapiTrialsFragmentArgs by navArgs()

    private val parentsViewModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val wishViewModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private companion object {

        val TAG = BrapiTrialsFragment::class.simpleName

    }

    /**
     * Whenever a list row is chosen, it is added to a set of ids (or removed if already chosen).
     * When the next table is chosen, all the ids in the respective set will be used to query.
     */
    private var mTrials = HashSet<BrapiTrial>()

    //populated from previous fragment, users selected a list of program ids.
    private var mProgramDbIds: List<String>? = null

    private val mScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    private val mService: BrAPIServiceV2 by lazy {

        BrAPIServiceV2(this@BrapiTrialsFragment.context)

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

    /**
     * For this import activity, the top bar shows the current table we are filtering for,
     * which navigates to the previous table on click (like a back button).
     * Similarly, the bottom bar moves to the next table but is like a select all query if no fields are chosen.
     */
    private fun setupTopAndBottomButtons() {

        mBinding.currentButton.setOnClickListener {
             findNavController().popBackStack() //go back to programs filter
        }

        mBinding.nextButton.setOnClickListener { //navigate to germplasm filter
            findNavController().navigate(BrapiTrialsFragmentDirections
                .actionToGermplasmFragment())

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
    private fun updateUi() {

        mBinding.currentButton.text = "Trials"

        mBinding.nextButton.text = "Germplasm"

    }

    //use search call to find trials within list of programs,
    //TODO: if search returns a searchRequestId, make another GET for the saved search data.
    private fun loadTrials(programDbIds: List<String>?) {

        mBinding.progressVisibility = View.VISIBLE

        mService.searchTrials(programDbIds, mPaginationManager, { trials ->

            mBinding.progressVisibility = View.GONE

            buildArrayAdapter(trials)

            null

        }) { fail ->

            //todo add error toast

            mBinding.progressVisibility = View.GONE

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

        updateUi()

        mScope.launch { //uses Dispatchers.IO for network background processing

            try {

                loadTrials(mProgramDbIds)

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

                is BrapiTrial -> mTrials.addOrRemove(item)

            }
        }

        val itemDataList: MutableList<Any?> = ArrayList()

        //load data into adapter
        data.forEach {

            when (it) {

                is BrapiTrial -> it.trialName?.let { name -> itemDataList.add(name) }
                is BrAPICrossingProject -> it.crossingProjectName?.let { name -> itemDataList.add(name) }
                //is BrAPIPlannedCross -> it.plannedCrossName?.let { name -> itemDataList.add(name) }

            }
        }

        context?.let { ctx ->

            this@BrapiTrialsFragment.activity?.runOnUiThread {

                listView.adapter = ArrayAdapter(ctx, android.R.layout.simple_list_item_single_choice, itemDataList)

            }
        }
    }
//
//    private fun searchObservationUnits() {
//
//        mPaginationManager.reset()
//
//        mService.searchObservationUnits(
//    }

//    private fun importSelected() {
//
//        mPaginationManager.reset()
//
//        mService.getPlannedCrosses(crossProject.crossingProjectDbId,
//
//            mPaginationManager, { planned ->
//
//                if (planned.isNotEmpty()) {
//
//                    val crossProjectPlans =
//                        planned.filter { it.crossingProjectDbId == crossProject.crossingProjectDbId }
//                    this@BrapiProgramsFragment.activity?.let { act ->
//
//                        act.runOnUiThread {
//
//                            WishlistImportDialog(
//                                act, crossProject.crossingProjectName,
//                                //filter planned crosses only in the selected cross project
//                                crossProjectPlans
//                            ).apply {
//
//                                //when the submit button is pressed, the dialog is dismissed and this is called
//                                this.setOnDismissListener {
//
//                                    this.enableProgress()
//
//                                    if (crossProjectPlans.isEmpty()) {
//
//                                        Toast.makeText(
//                                            act,
//                                            R.string.fragment_brapi_import_no_planned_crosses_found,
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//
//                                    } else crossProjectPlans.forEach { plan ->
//
//                                        //default min: 1 max: 10 type: cross
//                                        //plan.additionalInfo
//
//                                        val male: BrAPICrossParent
//                                        val female: BrAPICrossParent
//
//                                        //determine which parent is female/male
//                                        if (plan.parent1.parentType.toString() == "MALE") {
//                                            male = plan.parent1
//                                            female = plan.parent2
//                                        } else {
//                                            male = plan.parent2
//                                            female = plan.parent1
//                                        }
//
//                                        val infoMap = plan.additionalInfo.withDefault { null }
//                                        //insert wishlist row, check if brapi has wish metadata
//                                        val wishMin by infoMap //delegate metadata from brapi additional info map
//                                        val wishMax by infoMap
//                                        val wishType by infoMap
//
//                                        wishViewModel.insert(
//                                            Wishlist(
//                                                femaleDbId = female.observationUnitDbId,
//                                                maleDbId = male.observationUnitDbId,
//                                                femaleName = female.observationUnitName,
//                                                maleName = male.observationUnitName,
//                                                wishType = wishType ?: "cross",
//                                                wishMin = wishMin?.toIntOrNull() ?: 1,
//                                                wishMax = wishMax?.toIntOrNull() ?: 10
//                                            )
//                                        )
//
//                                        //insert parents from plannedcross data
//                                        parentsViewModel.insert(Parent(
//                                            codeId = male.observationUnitDbId, 1
//                                        ).also { it.name = male.observationUnitName },
//                                            Parent(
//                                                codeId = female.observationUnitDbId, 0
//                                            ).also { it.name = female.observationUnitName })
//
//                                    }
//
//                                    this.disableProgress()
//
//                                    findNavController().popBackStack()
//                                }
//
//                                show()
//
//                            }
//                        }
//                    }
//
//                } else Toast.makeText(this.context,
//                    getString(R.string.fragment_brapi_import_no_planned_crosses_found),
//                    Toast.LENGTH_SHORT).show()
//
//                null
//
//            }) { fail -> handleFailure(fail) }
//    }

    override fun FragmentBrapiImportBinding.afterCreateView() {

        activity?.let { act ->

            mProgramDbIds = args.programDbIds.split(",")

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