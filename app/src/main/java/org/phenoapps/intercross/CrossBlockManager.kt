package org.phenoapps.intercross


import android.os.Build
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.AbsListView
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.adapters.HeaderAdapter
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.CrossBlockManagerBinding
import org.phenoapps.intercross.fragments.IntercrossBaseFragment


class CrossBlockManager : IntercrossBaseFragment<CrossBlockManagerBinding>(R.layout.cross_block_manager),
    GestureDetector.OnGestureListener {

    //private lateinit var mGesture: GestureDetectorCompat // = GestureDetectorCompat(this, this)

//    override fun onShowPress(p0: MotionEvent?) {
//
//    }
//
//    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
//        return false
//    }
//
//    override fun onDown(p0: MotionEvent?): Boolean {
//        return false
//    }
//
//    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
//        return false
//    }
//
//    override fun onLongPress(p0: MotionEvent?) {
//    }
//
//    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
//
//        Log.d("HACK", "das a fling")
//        return false
//    }

    private lateinit var mGesture: GestureDetectorCompat
    private lateinit var mParentAdapter: HeaderAdapter
    private lateinit var mRowAdapter: HeaderAdapter
    private lateinit var mColumnAdapter: HeaderAdapter

    private var mParents = ArrayList<String>()

    private var crosses = ArrayList<String>()
    private var males = ArrayList<String>()
    private var females = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun CrossBlockManagerBinding.afterCreateView() {

        mGesture = GestureDetectorCompat(requireContext(), this@CrossBlockManager)

        mParentAdapter = HeaderAdapter(requireContext())
        mRowAdapter = HeaderAdapter(requireContext())
        mColumnAdapter = HeaderAdapter(requireContext())

        table.adapter = mParentAdapter

        val scrollListeners = ArrayList<RecyclerView.OnScrollListener>()


        val scrollViewListener = View.OnScrollChangeListener { p0, p1, p2, p3, p4 ->
            Log.d("p1", p1.toString())
            Log.d("p2", p2.toString())
            Log.d("p3", p3.toString())
            Log.d("p4", p4.toString())

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

        mParentsViewModel.parents.observe(viewLifecycleOwner, Observer { parents ->


            males = ArrayList()
            females = ArrayList()

            parents.forEach { pair ->
                if (pair.parentType == "male") {
                    males.add(pair.parentName)
                }
                else females.add(pair.parentName)
            }
            table.layoutManager = GridLayoutManager(requireContext(), males.size, GridLayoutManager.HORIZONTAL, false)

            mWishlistViewModel.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->
                var data = ArrayList<WishlistData>()
                crosses = ArrayList<String>()
                females.forEach { f ->
                    males.forEach { m ->
                        val filter = wishlist.filter {
                            it.maleName == m &&
                                    it.femaleName == f
                        }
                        if (filter.isEmpty()) {
                            crosses.add("NONE")
                        } else {
                            with (filter.first()) {
                                crosses.add("$wishMin")
                            }
                        }
                    }
                }
                mColumnAdapter.submitList(males)
                mRowAdapter.submitList(females)
                mParentAdapter.submitList(crosses)
            })
        })
    }

    data class WishlistData(var m: String, var f: String, var count: String, var event: List<Events>)

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