package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.evrencoskun.tableview.sort.ISortableModel
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.CrossTrackerAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetaValuesViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetadataViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentCrossTrackerBinding
import org.phenoapps.intercross.fragments.preferences.GeneralKeys
import org.phenoapps.intercross.util.Dialogs
import kotlin.collections.ArrayList

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class CrossTrackerFragment : IntercrossBaseFragment<FragmentCrossTrackerBinding>(R.layout.fragment_cross_tracker) {

    companion object {
        const val SORT_DELAY_MS = 500L
    }

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private val metaValuesViewModel: MetaValuesViewModel by viewModels {
        MetaValuesViewModelFactory(MetaValuesRepository.getInstance(db.metaValuesDao()))
    }

    private val metadataViewModel: MetadataViewModel by viewModels {
        MetadataViewModelFactory(MetadataRepository.getInstance(db.metadataDao()))
    }

    private var mWishlistEmpty = true
    private var mMetaValuesList: List<MetadataValues> = ArrayList()
    private var mMetaList: List<Meta> = ArrayList()
    private var mEvents: List<Event> = ArrayList()

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private var mExpandedColumns = false

    private var systemMenu: Menu? = null

    private val crossAdapter = CrossTrackerAdapter { male, female ->
        showChildren(male, female)
    }

    /**
     * Polymorphism setup to allow adapter to work with two different types of objects.
     * Wishlists and Summary data are the same but they have to be rendered differently.
     */
    open class ListEntry(open var m: String, open var f: String,
                         open var count: String, open var person: String = "",
                         open var date: String = "")

    data class CrossData(override var m: String,
                         override var f: String,
                         override var count: String,
                         override var person: String = "",
                         override var date: String = "",
                         val wishMin: String = "0",
                         val wishMax: String = "0",
                         val progress: String = "0"): ListEntry(m, f, count, person, date)

    data class CellData(val text: String, val uuid: String = "", val complete: Boolean = false): ISortableModel {

        override fun getId(): String {
            return text
        }

        override fun getContent(): Any {
            return text.toIntOrNull() ?: text
        }

    }

    private var currentFilter = CrossFilter.ALL
    private var crossesAndWishesData: List<CrossData> = emptyList()

    enum class CrossFilter {
        ALL,
        PLANNED,
        UNPLANNED
    }

    override fun FragmentCrossTrackerBinding.afterCreateView() {

        mPref.edit().putString("last_visited_summary", "summary").apply()

        startObservers()

        mBinding.crossesRecyclerView.apply {
            adapter = crossAdapter
            layoutManager = LinearLayoutManager(context)
        }

        bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        setupBottomNavBar()

        summaryTabLayout.getTabAt(0)?.select()

        setupTabLayout()

        eventsModel.events.observe(viewLifecycleOwner) {
            mEvents = it
        }


        fragmentCrossTrackerSearchButton.setOnClickListener {
            findNavController().navigate(CrossTrackerFragmentDirections
                .actionFromCrossTrackerToSearch())
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val commutativeCrossingEnabled = mPref.getBoolean(GeneralKeys.COMMUTATIVE_CROSSING, false)
            when (checkedIds.firstOrNull()) {
                R.id.filter_all -> currentFilter = CrossFilter.ALL
                R.id.filter_planned -> currentFilter = CrossFilter.PLANNED
                R.id.filter_unplanned -> currentFilter = CrossFilter.UNPLANNED
            }
            val filteredData = filterCrosses()
            val groupedData = groupCrosses(filteredData, commutativeCrossingEnabled)
            crossAdapter.submitList(groupedData) {
                mBinding.crossesRecyclerView.scrollToPosition(0)
            }
        }
    }

    private fun startObservers() {

        loadData()

        /**
         * Keep track if wishlist repo is empty to disable options items menu
         */
        wishModel.wishlist.observe(viewLifecycleOwner) { wishes ->

            mWishlistEmpty = wishes.none { it.wishType == "cross" }
            updateToolbarWishlistIcon()
        }
    }

    private fun showChildren(male: String, female: String) {

        val isCommutativeCrossing = mPref.getBoolean(GeneralKeys.COMMUTATIVE_CROSSING, false)

        if (isCommutativeCrossing) showCommutativeChildren(male, female)
        else showNonCommutativeChildren(male, female)
    }

    private fun showChildren(male: String, female: String, data: List<Event>) {

        context?.let { ctx ->
            Dialogs.listAndBuildCross(
                AlertDialog.Builder(ctx),
                getString(R.string.click_item_for_child_details),
                getString(R.string.no_child_exists),
                male, female, data, { id ->

                    findNavController()
                        .navigate(CrossTrackerFragmentDirections
                            .globalActionToEventDetail(id))
                }) { male, female ->

                findNavController()
                    .navigate(CrossTrackerFragmentDirections
                        .actionFromCrossTrackerToEventsList(male, female))
            }
        }
    }

    //a quick wrapper function for tab selection
    private fun tabSelected(onSelect: (TabLayout.Tab?) -> Unit) = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            onSelect(tab)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    private fun showCommutativeChildren(male: String, female: String) {

        eventsModel.parents.observe(viewLifecycleOwner) {

            it?.let { crosses ->

                eventsModel.events.observe(viewLifecycleOwner) { data ->

                    data?.let { events ->

                        showChildren(male, female, events.filter { e ->
                            (e.maleObsUnitDbId == male && e.femaleObsUnitDbId == female)
                                    || (e.maleObsUnitDbId == female && e.femaleObsUnitDbId == male)
                        })
                    }
                }
            }
        }
    }

    private fun showNonCommutativeChildren(male: String, female: String) {

        eventsModel.parents.observe(viewLifecycleOwner) {

            it?.let { crosses ->

                eventsModel.events.observe(viewLifecycleOwner) { data ->

                    data?.let { events ->

                        showChildren(male, female, events.filter { e ->
                            e.maleObsUnitDbId == male
                                    && e.femaleObsUnitDbId == female
                        })
                    }
                }
            }
        }
    }

    private fun loadData() {
        val commutativeCrossingEnabled = mPref.getBoolean(GeneralKeys.COMMUTATIVE_CROSSING, false)
        eventsModel.parents.observe(viewLifecycleOwner) { parentsCount ->
            parentsCount?.let { crosses ->
                wishModel.wishes.observe(viewLifecycleOwner) { wishes ->
                    val crossData = crosses.map { parentRow ->
                        val wish = if (commutativeCrossingEnabled) {
                            wishes?.find { w ->
                                (w.dadId == parentRow.dad && w.momId == parentRow.mom) ||
                                        (w.dadId == parentRow.mom && w.momId == parentRow.dad)
                            }
                        } else {
                            wishes?.find { w ->
                                w.dadId == parentRow.dad && w.momId == parentRow.mom
                            }
                        }

                        // add to crossData depending on
                        // whether cross belongs to wishlist or not
                        CrossData(
                            parentRow.dad,
                            parentRow.mom,
                            parentRow.count.toString(),
                            parentRow.person,
                            parentRow.date,
                            wish?.wishMin?.toString() ?: "0",
                            wish?.wishMax?.toString() ?: "0",
                            wish?.wishProgress?.toString() ?: parentRow.count.toString()
                        )
                    }

                    // Add remaining wishes that don't have crosses
                    val remainingWishes = wishes?.filter { wish ->
                        if (commutativeCrossingEnabled) {
                            crossData.none { cross ->
                                (cross.m == wish.dadId && cross.f == wish.momId) ||
                                        (cross.m == wish.momId && cross.f == wish.dadId)
                            }
                        } else {
                            crossData.none { cross ->
                                cross.m == wish.dadId && cross.f == wish.momId
                            }
                        }
                    }?.map { wish ->
                        CrossData(
                            wish.dadName,
                            wish.momName,
                            "0",
                            "",
                            "",
                            wish.wishMin.toString(),
                            wish.wishMax.toString(),
                            wish.wishProgress.toString()
                        )
                    } ?: emptyList()

                    crossesAndWishesData = crossData + remainingWishes

                    val filteredData = filterCrosses()

                    // If commutative, group the data
                    val groupedData = groupCrosses(filteredData, commutativeCrossingEnabled)

                    crossAdapter.submitList(groupedData)
                }
            }
        }
    }

    private fun filterCrosses(): List<CrossData> {
        return when (currentFilter) {
            CrossFilter.ALL -> crossesAndWishesData
            CrossFilter.PLANNED -> crossesAndWishesData.filter {
                (it.wishMax.toIntOrNull() ?: 0) > 0
            }
            CrossFilter.UNPLANNED -> crossesAndWishesData.filter {
                (it.wishMax.toIntOrNull() ?: 0) == 0
            }
        }
    }

    private fun groupCrosses(filteredData: List<CrossData>, commutativeCrossingEnabled: Boolean): List<CrossData> {
        return if (commutativeCrossingEnabled) {
            filteredData.groupBy { cross ->
                if (cross.m < cross.f) "${cross.m}${cross.f}".hashCode()
                else "${cross.f}${cross.m}".hashCode()
            }.map { entry ->
                if (entry.value.size == 1) entry.value[0]
                else {
                    val totalCount = entry.value.sumBy { it.count.toInt() }.toString()
                    entry.value[0].copy(count = totalCount)
                }
            }
        } else {
            filteredData
        }
    }

    private fun FragmentCrossTrackerBinding.setupTabLayout() {

        summaryTabLayout.addOnTabSelectedListener(tabSelected { tab ->

            when (tab?.position) {
                1 ->
                    Navigation.findNavController(mBinding.root)
                        .navigate(CrossTrackerFragmentDirections.actionToSummary())
            }
        })
    }

    override fun onResume() {
        super.onResume()

        (activity as? AppCompatActivity)?.setSupportActionBar(mBinding.fragCrossTrackerTb)

        updateToolbarWishlistIcon()

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_cross_count

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.cross_tracker_toolbar, menu)

        systemMenu = menu

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun updateToolbarWishlistIcon() {
        systemMenu?.findItem(R.id.action_to_crossblock)?.isVisible = !mWishlistEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_import -> {

                (activity as MainActivity).launchImport()

                findNavController().navigate(R.id.cross_tracker_fragment)
            }
            R.id.action_to_crossblock -> {
                findNavController().navigate(CrossTrackerFragmentDirections.actionToCrossblock())
            }
            R.id.action_cross_count_delete_all -> {
                context?.let { ctx ->
                    Dialogs.onOk(AlertDialog.Builder(ctx),
                        getString(R.string.menu_cross_count_delete_all_title),
                        getString(android.R.string.cancel),
                        getString(android.R.string.ok),
                        getString(R.string.dialog_cross_count_delete_all_message)) {

                        Dialogs.onOk(AlertDialog.Builder(ctx),
                            getString(R.string.menu_cross_count_delete_all_title),
                            getString(android.R.string.cancel),
                            getString(android.R.string.ok),
                            getString(R.string.dialog_cross_count_delete_all_message_2)) {

                            eventsModel.deleteAll()

                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun FragmentCrossTrackerBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_preferences -> {

                    findNavController().navigate(CrossTrackerFragmentDirections.globalActionToPreferencesFragment())
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(CrossTrackerFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_home -> {

                    findNavController().navigate(CrossTrackerFragmentDirections.globalActionToEvents())

                }
            }

            true
        }
    }
}