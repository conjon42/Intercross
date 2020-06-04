package org.phenoapps.intercross.fragments

import android.app.Notification
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.WishlistAdapter
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.FragmentWishlistManagerBinding


class WishlistFragment : IntercrossBaseFragment<FragmentWishlistManagerBinding>(R.layout.fragment_wishlist_manager) {

    private lateinit var mAdapter: WishlistAdapter

    data class WishlistData(var m: String, var f: String, var count: String, var event: List<Events>)

    override fun FragmentWishlistManagerBinding.afterCreateView() {

        //recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = WishlistAdapter(requireContext())

        recyclerView.adapter = mAdapter

        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        recyclerView.layoutManager = GridLayoutManager(context, 1)

        mEventsListViewModel.crosses.observe(viewLifecycleOwner, Observer { events ->

            mWishlistViewModel.wishlist.observe(viewLifecycleOwner, Observer { wishlist ->

                var data = ArrayList<WishlistData>()

                wishlist.forEach { wish ->

                    var current = 0
                    var crosses = ArrayList<Events>()
                    events.forEach { event ->
                        if (event.femaleObsUnitDbId == wish.femaleDbId && event.maleOBsUnitDbId == wish.maleDbId) {
                            current++
                            crosses.add(event)
                        }
                    }
                    data.add(WishlistData(wish.maleDbId, wish.femaleDbId, "$current/${wish.wishMax}", crosses))
                }

                mAdapter.submitList(data)

                mAdapter.notifyDataSetChanged()

            })
        })
    }
}