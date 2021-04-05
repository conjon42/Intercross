package org.phenoapps.intercross.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_barcode_scan.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.brapi.v2.model.germ.BrAPICrossParent
import org.brapi.v2.model.germ.BrAPICrossingProject
import org.brapi.v2.model.germ.BrAPIPlannedCross
import org.phenoapps.intercross.R
import org.phenoapps.intercross.brapi.model.BrapiProgram
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
class BrapiProgramsFragment: IntercrossBaseFragment<FragmentBrapiImportBinding>(R.layout.fragment_brapi_import) {

    private val parentsViewModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val wishViewModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private var mPrograms = HashSet<BrapiProgram>()

    companion object {

        val TAG = BrapiProgramsFragment::class.simpleName

        const val CROSSPROJECTS = 0
        const val PLANNEDCROSSES = 1

        const val TRIALS_REQUEST = "290801"
        const val KEY_PROGRAMS_ARRAY = "org.phenoapps.brapi.programs_bundle"
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

        BrAPIServiceV2(this@BrapiProgramsFragment.context)

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
            //update the state (go back one)
            //reset respective sets
            when(mFilterState) {
                CROSSPROJECTS -> findNavController().popBackStack()
//                 PLANNEDCROSSES -> {
//                    mProjects = HashSet()
//                    mPlannedCrosses = HashSet()
//                    mFilterState = CROSSPROJECTS
//                }
            }

            //update UI with the new state
            loadBrAPIData()
        }

        mBinding.nextButton.setOnClickListener {
            //update the state (go forward one)
            findNavController().navigate(BrapiProgramsFragmentDirections
                .actionToTrialsFragment(mPrograms.joinToString(",") { it.programDbId }))

            setFragmentResultListener(TRIALS_REQUEST) { key, bundle ->

            }

            //setFragmentResult()

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

        mBinding.currentButton.text = "Programs"

        mBinding.nextButton.text = "Trials"

    }

    //create a list of programs from brapi data source
    private fun loadPrograms() {

        mPaginationManager.reset()

        mBinding.listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        mBinding.progressVisibility = View.VISIBLE

        mService.getPrograms(mPaginationManager, { programs ->

            mBinding.progressVisibility = View.GONE

            buildArrayAdapter(programs)

            null

        }) { fail ->

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

    //load and display programs
    private fun loadBrAPIData() {

        updateUi(mFilterState)

        mScope.launch { //uses Dispatchers.IO for network background processing

            try {

                loadPrograms()

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

                is BrapiProgram -> mPrograms.addOrRemove(item)
                is BrAPICrossingProject -> mProjects = item
                //is BrAPIPlannedCross -> mPlannedCrosses.addOrRemove(item)

            }
        }

        val itemDataList: MutableList<Any?> = ArrayList()

        //load data into adapter
        data.forEach {

            when (it) {

                is BrapiProgram -> it.programName?.let { name -> itemDataList.add(name) }
                is BrAPICrossingProject -> it.crossingProjectName?.let { name -> itemDataList.add(name) }
                //is BrAPIPlannedCross -> it.plannedCrossName?.let { name -> itemDataList.add(name) }

            }
        }

        context?.let { ctx ->

            this@BrapiProgramsFragment.activity?.runOnUiThread {

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