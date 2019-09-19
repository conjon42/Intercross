package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.adapters.PollenAdapter
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.databinding.FragmentPollenBinding

class PollenFragment : IntercrossBaseFragment<FragmentPollenBinding>(R.layout.fragment_pollen) {

    private lateinit var mAdapter: PollenAdapter

    override fun FragmentPollenBinding.afterCreateView() {

        val pollen = arguments?.getParcelable<PollenGroup>("pollen")

        model = pollen

        maleView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = PollenAdapter(requireContext())

        maleView.adapter = mAdapter

        executePendingBindings()

        //TODO clarify which males should be considered
        mEventsListViewModel.crosses.observe(viewLifecycleOwner, Observer {
            it?.let {list ->
                mAdapter.submitList(list)
            }
        })

        updateButton.setOnClickListener {

        }
    }
}
