package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.WishlistAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Metadata
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetaValuesViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetadataViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentWishlistBinding
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.KeyUtil
import org.phenoapps.intercross.util.observeOnce

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class WishlistFragment : IntercrossBaseFragment<FragmentWishlistBinding>(R.layout.fragment_wishlist) {

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

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private var wishlistEmpty = true

    private var mMetaValuesList: List<MetadataValues> = ArrayList()
    private var mMetadataList: List<Metadata> = ArrayList()
    private var mEvents: List<Event> = ArrayList()

    override fun FragmentWishlistBinding.afterCreateView() {

        bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        setupBottomNavBar()

        mPref.edit().putString("last_visited_summary", "wishlist").apply()

        recyclerView.adapter = WishlistAdapter(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        val isCommutative = mPref.getBoolean(mKeyUtil.workCommutativeKey, false)

        eventsModel.events.observe(viewLifecycleOwner, {
            mEvents = it

            metadataViewModel.metadata.observe(viewLifecycleOwner) {
                mMetadataList = it

                metaValuesViewModel.metaValues.observe(viewLifecycleOwner) {
                    mMetaValuesList = it

                    if (isCommutative) loadCommutativeWishlist()
                    else loadWishlist()
                }
            }
        })

        deleteButton.setOnClickListener {

            Dialogs.onOk(AlertDialog.Builder(requireContext()),
                getString(R.string.delete_all_wishlist_title),
                getString(R.string.cancel),
                getString(R.string.zxing_button_ok)) {

                wishModel.deleteAll()

                findNavController().navigate(WishlistFragmentDirections.globalActionToCrossCount())
            }
        }

        summaryTabLayout.getTabAt(1)?.select()

        setupTabLayout()
    }

    private fun FragmentWishlistBinding.loadWishlist() {

        wishModel.wishes.observe(viewLifecycleOwner, {

            it?.let { block ->

                val data = ArrayList<CrossCountFragment.WishlistData>()

                //get cross wishes which are automatically counted in the wishlist view
                val crosses = block.filter { wish -> wish.wishType == "cross" }.map { res ->
                    CrossCountFragment.WishlistData(res.dadName, res.momName,
                        res.wishProgress.toString() + "/" + res.wishMin + "/" + res.wishMax.toString(),
                        getChildren(res))
                }

                data.addAll(crosses)

                //get all other wish types and use metadata tables to populate the wish progress
                val otherWishes = block.filter { it.wishType != "cross" }.map { row ->

                    //get children to accumulate their metadata
                    val children = getChildren(row)
                    var progress = 0
                    val metadata = mMetadataList.filter { it.property == row.wishType }
                    if (metadata.isNotEmpty()) {
                        val meta = metadata.first()
                        progress = mMetaValuesList.filter { it.metaId == meta.id?.toInt()
                                && it.eid in children.map { it.id?.toInt() }}
                            .map { it.value ?: 0 }.sum()
                    }
                    CrossCountFragment.WishlistData(row.dadName, row.momName,
                        "$progress/${row.wishMin}/${row.wishMax}",
                        children)
                }

                data.addAll(otherWishes)

                wishlistEmpty = crosses.isEmpty()

                (recyclerView.adapter as WishlistAdapter)
                    .submitList(data.toList())

            }
        })
    }

    private fun getChildrenCommutative(data: WishlistView): List<Event> = mEvents.filter {
            event -> (event.femaleObsUnitDbId == data.momId && event.maleObsUnitDbId == data.dadId)
            || (event.femaleObsUnitDbId == data.dadId && event.maleObsUnitDbId == data.momId)
    }

    private fun getChildren(data: WishlistView): List<Event> = mEvents.filter {
        event -> event.femaleObsUnitDbId == data.momId && event.maleObsUnitDbId == data.dadId
    }

    private fun FragmentWishlistBinding.loadCommutativeWishlist() {

        wishModel.commutativeWishes.observe(viewLifecycleOwner, {

            it?.let { block ->

                val data = ArrayList<CrossCountFragment.WishlistData>()

                //get cross wishes which are automatically counted in the wishlist view
                val crosses = block.filter { wish -> wish.wishType == "cross" }.map { res ->
                    CrossCountFragment.WishlistData(res.dadName, res.momName,
                        res.wishProgress.toString() + "/" + res.wishMin + "/" + res.wishMax.toString(),
                        getChildrenCommutative(res))
                }

                data.addAll(crosses)

                //get all other wish types and use metadata tables to populate the wish progress
                val otherWishes = block.filter { it.wishType != "cross" }.map { row ->

                    //get children to accumulate their metadata
                    val children = getChildrenCommutative(row)
                    var progress = 0
                    val metadata = mMetadataList.filter { it.property == row.wishType }
                    if (metadata.isNotEmpty()) {
                        val meta = metadata.first()
                        progress = mMetaValuesList.filter { it.metaId == meta.id?.toInt()
                                && it.eid in children.map { it.id?.toInt() }}
                            .map { it.value ?: 0 }.sum()
                    }
                    CrossCountFragment.WishlistData(row.dadName, row.momName,
                        "$progress/${row.wishMin}/${row.wishMax}",
                        children)
                }

                data.addAll(otherWishes)

                wishlistEmpty = crosses.isEmpty()

                (recyclerView.adapter as WishlistAdapter)
                    .submitList(data.toList())

            }
        })
    }

    //a quick wrapper function for tab selection
    private fun tabSelected(onSelect: (TabLayout.Tab?) -> Unit) = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            onSelect(tab)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    private fun FragmentWishlistBinding.setupTabLayout() {

        summaryTabLayout.addOnTabSelectedListener(tabSelected { tab ->

            when (tab?.text) {
                getString(R.string.summary) -> {

                    if (mEvents.isNotEmpty()) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToSummary())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.crosses_empty))
                        summaryTabLayout.getTabAt(1)?.select()

                    }
                }

                getString(R.string.cross_count) ->
                    Navigation.findNavController(mBinding.root)
                        .navigate(WishlistFragmentDirections.actionToCrossCount())

                getString(R.string.crossblock) -> {

                    if (!wishlistEmpty) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossblock())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))
                        summaryTabLayout.getTabAt(1)?.select()

                    }
                }
            }
        })
    }

    private fun FragmentWishlistBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_home -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToEvents())
                }
                R.id.action_nav_settings -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToSettingsFragment())
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        findNavController().navigate(R.id.wishlist_fragment)
                    }

                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToCrossCount())

                }
            }

            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()

        mBinding.summaryTabLayout.getTabAt(1)?.select()

        mBinding.bottomNavBar.menu.findItem(R.id.action_nav_cross_count).isEnabled = false

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        mBinding.bottomNavBar.menu.findItem(R.id.action_nav_cross_count).isEnabled = true

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.wishlist_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_parents_toolbar_initiate_wf -> {

                findNavController().navigate(WishlistFragmentDirections
                    .actionFromWishlistToWishFactory())
            }

            R.id.action_import -> {

                (activity as MainActivity).launchImport()

                findNavController().navigate(R.id.wishlist_fragment)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}