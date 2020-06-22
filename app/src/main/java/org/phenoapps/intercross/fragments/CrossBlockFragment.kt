package org.phenoapps.intercross.fragments


import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.HeaderAdapter
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.CrossBlockManagerBinding


class CrossBlockFragment : IntercrossBaseFragment<CrossBlockManagerBinding>(R.layout.cross_block_manager),
    GestureDetector.OnGestureListener {

    /***
     * Polymorphism class structure to serve different cell types to the Cross Block Table.
     */
    open class BlockData
    data class HeaderData(val name: String) : BlockData()
    data class CellData(val current: Int, val min: Int, val max: Int): BlockData()
    class EmptyCell: BlockData()

    private lateinit var mGesture: GestureDetectorCompat

    private lateinit var mParentAdapter: HeaderAdapter

    private lateinit var mRowAdapter: HeaderAdapter

    private lateinit var mColumnAdapter: HeaderAdapter


    @RequiresApi(Build.VERSION_CODES.M)
    override fun CrossBlockManagerBinding.afterCreateView() {

        mGesture = GestureDetectorCompat(requireContext(), this@CrossBlockFragment)

        mParentAdapter = HeaderAdapter()
        mRowAdapter = HeaderAdapter()
        mColumnAdapter = HeaderAdapter()

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

                mParentAdapter.notifyDataSetChanged()
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

        val viewModel: WishlistViewModel by viewModels {
            WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
        }

        val data = ArrayList<BlockData>()

        viewModel.crossblock.observe(viewLifecycleOwner, Observer { block ->

            val columns = block.size

            table.layoutManager = GridLayoutManager(requireContext(), columns,
                    GridLayoutManager.HORIZONTAL, false)

            val maleHeaders = block.map { HeaderData(it.maleName) }
            val femaleHeaders = block.map { HeaderData(it.femaleName) }

            for (f in femaleHeaders) {

                for (m in maleHeaders) {

                    val filter = block.filter { m.name == it.maleName && f.name == it.femaleName }

                    if (filter.isNotEmpty()) {

                        val res = filter.first()

                        data.add(CellData(res.count+res.pre, res.min, res.max))

                    } else data.add(EmptyCell())
                }
            }

            mRowAdapter.submitList(femaleHeaders)
            mColumnAdapter.submitList(maleHeaders)
            mParentAdapter.submitList(data)

            mParentAdapter.notifyDataSetChanged()
            mColumnAdapter.notifyDataSetChanged()
            mRowAdapter.notifyDataSetChanged()
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

            R.id.action_to_summary -> {
                Navigation.findNavController(mBinding.root)
                        .navigate(CrossBlockFragmentDirections.actionToSummary())
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