package org.phenoapps.intercross.fragments

import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.HeaderAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.CrossBlockManagerBinding
import org.phenoapps.intercross.util.AsyncLoadCrossblock
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.KeyUtil


class CrossBlockFragment : IntercrossBaseFragment<CrossBlockManagerBinding>(R.layout.cross_block_manager),
    GestureDetector.OnGestureListener {

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    /***
     * Polymorphism class structure to serve different cell types to the Cross Block Table.
     */
    open class BlockData
    data class HeaderData(val name: String, val code: String) : BlockData()
    data class CellData(val current: Int, val min: Int, val max: Int, val onClick: View.OnClickListener, val color: Int): BlockData()
    class EmptyCell: BlockData()

    private lateinit var mGesture: GestureDetectorCompat

    private lateinit var mParentAdapter: HeaderAdapter

    private lateinit var mRowAdapter: HeaderAdapter

    private lateinit var mColumnAdapter: HeaderAdapter

    private lateinit var mWishlist: List<WishlistView>

    private var mEvents: List<Event> = ArrayList()

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun CrossBlockManagerBinding.afterCreateView() {

        setHasOptionsMenu(false)

        //(activity as MainActivity).supportActionBar?.hide()

        bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        setupBottomNavBar()

        mPref.edit().putString("last_visited_summary", "crossblock").apply()

        val isCommutative = mPref.getBoolean(mKeyUtil.workCommutativeKey, false)

        mGesture = GestureDetectorCompat(requireContext(), this@CrossBlockFragment)

        mParentAdapter = HeaderAdapter(requireContext())
        mRowAdapter = HeaderAdapter(requireContext())
        mColumnAdapter = HeaderAdapter(requireContext())

        table.adapter = mParentAdapter

        val scrollListeners = ArrayList<RecyclerView.OnScrollListener>()

        val scrollViewListener = View.OnScrollChangeListener { p0, p1, p2, p3, p4 ->

            rows.removeOnScrollListener(scrollListeners[2])

            rows.scrollBy(p1, p2-p4)

            rows.addOnScrollListener(scrollListeners[2])

        }

        scrollListeners.add(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)

                columns.removeOnScrollListener(scrollListeners[1])

                columns.scrollBy(dx, dy)

                columns.addOnScrollListener(scrollListeners[1])

                rows.removeOnScrollListener(scrollListeners[2])

                rows.scrollBy(dx, dy)

                rows.addOnScrollListener(scrollListeners[2])

            }
        })

        scrollListeners.add(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)

                table.removeOnScrollListener(scrollListeners[0])

                table.scrollBy(dx, dy)

                table.addOnScrollListener(scrollListeners[0])
            }
        })

        scrollListeners.add(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                super.onScrolled(recyclerView, dx, dy)

                scrollView.setOnScrollChangeListener { view: View, i: Int, i1: Int, i2: Int, i3: Int -> }

                scrollView.scrollBy(dx, dy)

                scrollView.setOnScrollChangeListener(scrollViewListener)
            }
        })

        table.addOnScrollListener(scrollListeners[0])

        columns.addOnScrollListener(scrollListeners[1])

        rows.addOnScrollListener(scrollListeners[2])

        scrollView.setOnScrollChangeListener(scrollViewListener)

        columns.adapter = mColumnAdapter

        rows.adapter = mRowAdapter

        /**
         * list for events model, disable options menu for summary if the list is empty
         */
        eventsModel.events.observe(viewLifecycleOwner, {

            it?.let {

                mEvents = it

                if (isCommutative) {

                    wishModel.commutativeCrossblock.observe(viewLifecycleOwner, { block ->

                        mWishlist = block

                        AsyncLoadCrossblock(requireContext(), mBinding.root, block, mEvents, table, rows, columns).execute()

                    })

                } else {

                    wishModel.crossblock.observe(viewLifecycleOwner, { block ->

                        mWishlist = block

                        AsyncLoadCrossblock(requireContext(), mBinding.root, block, mEvents, table, rows, columns).execute()

                    })
                }
            }
        })

        summaryTabLayout.getTabAt(2)?.select()

        setupTabLayout()
    }

    override fun onResume() {
        super.onResume()

        //(activity as MainActivity).supportActionBar?.hide()

        mBinding.summaryTabLayout.getTabAt(2)?.select()

        mBinding.bottomNavBar.menu.findItem(R.id.action_nav_cross_count).isEnabled = false

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        mBinding.bottomNavBar.menu.findItem(R.id.action_nav_cross_count).isEnabled = true

    }

    //a quick wrapper function for tab selection
    private fun tabSelected(onSelect: (TabLayout.Tab?) -> Unit) = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            onSelect(tab)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    private fun CrossBlockManagerBinding.setupTabLayout() {

        summaryTabLayout.addOnTabSelectedListener(tabSelected { tab ->

            when (tab?.text) {
                getString(R.string.summary) -> {

                    if (mEvents.isNotEmpty()) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(CrossBlockFragmentDirections.actionToSummary())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.crosses_empty))
                        summaryTabLayout.getTabAt(2)?.select()

                    }
                }

                getString(R.string.cross_count) ->
                    Navigation.findNavController(mBinding.root)
                        .navigate(CrossBlockFragmentDirections.actionToCrossCount())
                getString(R.string.wishlist) ->
                    Navigation.findNavController(mBinding.root)
                        .navigate(CrossBlockFragmentDirections.actionToWishlist())
            }
        })
    }

    private fun CrossBlockManagerBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_home -> {

                    findNavController().navigate(CrossBlockFragmentDirections.globalActionToEvents())
                }
                R.id.action_nav_settings -> {

                    findNavController().navigate(CrossBlockFragmentDirections.globalActionToSettingsFragment())
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(CrossBlockFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        findNavController().navigate(R.id.crossblock_fragment)
                    }

                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(CrossBlockFragmentDirections.globalActionToCrossCount())

                }
            }

            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onShowPress(p0: MotionEvent?) {
        TODO("Not yet implemented")
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        TODO("Not yet implemented")
    }

    override fun onLongPress(p0: MotionEvent?) {
        TODO("Not yet implemented")
    }
}