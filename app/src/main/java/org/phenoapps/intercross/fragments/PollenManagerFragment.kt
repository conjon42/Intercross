package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.adapters.PollenAdapter
import org.phenoapps.intercross.adapters.PollenGroupAdapter
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.databinding.FragmentPollenManagerBinding
import org.phenoapps.intercross.viewmodels.PollenGroupViewModel

class PollenManagerFragment : Fragment() {

    private lateinit var mBinding: FragmentPollenManagerBinding
    private lateinit var mAdapter: PollenGroupAdapter
    private lateinit var mPollenManagerViewModel: PollenGroupViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        mPollenManagerViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PollenGroupViewModel(PollenGroupRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).pollenGroupDao())) as T

                    }
                }).get(PollenGroupViewModel::class.java)

        mBinding = FragmentPollenManagerBinding
                .inflate(inflater, container, false)

        mAdapter = PollenGroupAdapter(requireContext())

        mPollenManagerViewModel.groups.observe(viewLifecycleOwner, Observer {
            it?.let {
                mAdapter.submitList(it)
            }
        })

        with(mBinding) {
            pollenView.layoutManager = LinearLayoutManager(requireContext())
            pollenView.adapter = mAdapter

            newButton.setOnClickListener {
                if (editText.text.toString().isNotEmpty()) {
                    mPollenManagerViewModel.addPollenSet(editText.text.toString())
                    editText.text.clear()
                }
            }

            return root
        }
    }
}
