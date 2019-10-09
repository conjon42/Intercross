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

    data class SummaryData(var m: String, var f: String, var count: Int, var event: List<Events>)

    override fun FragmentSummaryBinding.afterCreateView() {

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = SummaryAdapter(requireContext())

        recyclerView.adapter = mAdapter

        mEventsListViewModel.crosses.observe(viewLifecycleOwner, Observer { events ->
            events.let {
                var summaryList = ArrayList<SummaryData>()

                it.forEach { x ->

                    val female = x.femaleObsUnitDbId
                    val male = x.maleOBsUnitDbId
                    var f: String? = null
                    var m: String? = null
                    var count = 0
                    var events: ArrayList<Events> = ArrayList()

                    (it - x).forEach { y ->

                        if (x.maleOBsUnitDbId == y.maleOBsUnitDbId
                                && x.femaleObsUnitDbId == y.femaleObsUnitDbId
                                && x.eventDbId != y.eventDbId) {
                            f = x.femaleObsUnitDbId
                            m = x.maleOBsUnitDbId
                            if (y !in events) events.add(y)
                        }

                        //if (x.maleOBsUnitDbId == y.eventDbId) m = y
                        //if (x.femaleObsUnitDbId == y.eventDbId) f = y
                    }

                    f?.let { fit ->
                        m?.let { mit ->
                            summaryList.add(SummaryData(fit, mit, events.size, events))
                        }
                    }
                }

                mAdapter.submitList(
                        summaryList.distinctBy { "${it.f}/${it.m}" }
                )
            }
        })
    }


}