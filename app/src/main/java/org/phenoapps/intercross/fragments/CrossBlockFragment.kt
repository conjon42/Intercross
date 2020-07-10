package org.phenoapps.intercross.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.HeaderAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.CrossBlockManagerBinding
import org.phenoapps.intercross.util.AsyncLoadCrossblock
import org.phenoapps.intercross.util.Dialogs


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

    private var mEventsEmpty = true

    private lateinit var mEvents: List<Event>

    @RequiresApi(Build.VERSION_CODES.M)
    override fun CrossBlockManagerBinding.afterCreateView() {
        
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("last_visited_summary", "crossblock").apply()

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


        wishModel.crossblock.observe(viewLifecycleOwner, Observer { block ->

            AsyncLoadCrossblock(requireContext(), mBinding.root, block, mEvents, table, rows, columns).execute()

        })

        /**
         * list for events model, disable options menu for summary if the list is empty
         */
        eventsModel.events.observe(viewLifecycleOwner, Observer {

            it?.let {

                mEvents = it

                mEventsEmpty = it.isEmpty()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.crossblock_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_nav_wishlist -> {
                Navigation.findNavController(mBinding.root)
                        .navigate(CrossBlockFragmentDirections.actionToWishlist())
            }

            R.id.action_nav_summary -> {

                if (!mEventsEmpty) {
                    Navigation.findNavController(mBinding.root)
                            .navigate(CrossBlockFragmentDirections.actionToSummary())
                } else Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.summary_empty))
            }
        }
        return super.onOptionsItemSelected(item)
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