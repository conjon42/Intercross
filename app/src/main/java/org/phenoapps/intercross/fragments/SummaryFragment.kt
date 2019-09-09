package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.FragmentSummaryBinding


class SummaryFragment : IntercrossBaseFragment<FragmentSummaryBinding>(R.layout.fragment_summary) {

    private lateinit var mAdapter: SummaryAdapter

    data class SummaryData(var m: Events?, var f: Events?, var event: Events, var count: Int)

    override fun FragmentSummaryBinding.afterCreateView() {

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = SummaryAdapter(requireContext())

        recyclerView.adapter = mAdapter

        mEventsListViewModel.events.observe(viewLifecycleOwner, Observer { events ->
            events.let {
                var summaryList = ArrayList<SummaryData>()

                val pollinations = events.filter { it.eventName == EventName.POLLINATION.itemType }

                pollinations.forEach { x ->

                    var f: Events? = null
                    var m: Events? = null
                    var count = 0

                    pollinations.forEach { y ->

                        if (x.maleOBsUnitDbId == y.maleOBsUnitDbId
                                && x.femaleObsUnitDbId == y.femaleObsUnitDbId) {
                            count++
                        }

                        if (x.maleOBsUnitDbId == y.eventDbId) m = y
                        if (x.femaleObsUnitDbId == y.eventDbId) f = y
                    }

                    summaryList.add(SummaryData(m, f, x, count))

                }

                mAdapter.submitList(
                        summaryList.distinctBy { "${it.event.femaleObsUnitDbId}/${it.event.maleOBsUnitDbId}" }
                )
            }
        })
    }


}