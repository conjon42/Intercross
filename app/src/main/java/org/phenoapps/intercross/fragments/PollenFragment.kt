package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.databinding.FragmentPollenBinding

class PollenFragment : IntercrossBaseFragment() {

    private lateinit var mBinding: FragmentPollenBinding

    private lateinit var mAdapter: EventsAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val pollen = arguments?.getParcelable<PollenGroup>("pollen")

        mBinding = FragmentPollenBinding
                .inflate(inflater, container, false)

        mBinding.model = pollen

        mBinding.maleView.layoutManager = LinearLayoutManager(requireContext())

        mAdapter = EventsAdapter(requireContext())

        mBinding.maleView.adapter = mAdapter

        mBinding.executePendingBindings()

        //TODO clarify which males should be considered
        mEventsListViewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {list ->
                mAdapter.submitList(list)
            }
        })

        return mBinding.root
    }
}
