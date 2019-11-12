package org.phenoapps.intercross.fragments

import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.FragmentSummaryBinding


class SummaryFragment : IntercrossBaseFragment<FragmentSummaryBinding>(R.layout.fragment_summary) {

    private lateinit var mAdapter: SummaryAdapter

    data class SummaryData(var m: String, var f: String, var count: Int, var event: List<Events>)

    override fun FragmentSummaryBinding.afterCreateView() {

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = SummaryAdapter(requireContext())

        recyclerView.adapter = mAdapter

        deleteButton.setOnClickListener {
            mEventsListViewModel.deleteAll()
            findNavController().navigate(R.id.events_fragment)
        }

        mEventsListViewModel.crosses.observe(viewLifecycleOwner, Observer { events ->

            events.let {

                val parents = HashMap<Pair<String,String>, ArrayList<Events>>()

                it.forEach { x ->

                    val female = x.femaleObsUnitDbId
                    val male = x.maleOBsUnitDbId

                    parents[Pair(female,male)] = ArrayList<Events>().also { list ->
                        list.add(x)
                    }

                    (it - x).forEach { y ->

                        val yf = y.femaleObsUnitDbId
                        val ym = y.maleOBsUnitDbId
                        val key = Pair(yf,ym)
                        if (key in parents.keys) {
                            parents[key]?.let { children ->
                                if (y !in children) children.add(y)
                            }
                        }
                    }
                }

                var summaryList = ArrayList<SummaryData>()

                for ((p,c) in parents) {
                    summaryList.add(SummaryData(p.first, p.second,
                            c.size, c))
                }


                mAdapter.submitList(
                        summaryList.distinctBy { "${it.f}/${it.m}" }
                )

                mAdapter.notifyDataSetChanged()
            }
        })
    }


}