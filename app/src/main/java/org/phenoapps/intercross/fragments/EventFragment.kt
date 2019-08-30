package org.phenoapps.intercross.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.FragmentEventBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.DateUtil


class EventFragment: IntercrossBaseFragment<FragmentEventBinding>(R.layout.fragment_event) {

    private lateinit var mEvents: List<Events>
    private var mHarvests: Int? = null
    private var mThreshes: Int? = null
    private var mFlowers: Int? = null
    private lateinit var mEvent: Events

    override fun FragmentEventBinding.afterCreateView() {

        setHasOptionsMenu(true)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getParcelable<Events>("events")?.let {
            mEvent = it
        }

        if (mCollectData) {
            //mBinding.dateEditText.visibility = View.VISIBLE
            countEditText.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE
        } else {
            //mBinding.dateEditText.visibility = View.INVISIBLE
            countEditText.visibility = View.INVISIBLE
            tabLayout.visibility = View.INVISIBLE
        }

        events = mEvent

        maleName.setOnClickListener {
            searchForParents(mBinding.maleName.text.toString())
        }

        femaleName.setOnClickListener {
            searchForParents(mBinding.femaleName.text.toString())
        }

        button2.setOnClickListener {
            val x = countEditText.text.toString()
            x.toIntOrNull()?.let { x ->
                Thread {
                    when (tabLayout.selectedTabPosition) {
                        0 -> mEventsListViewModel.updateFlowers(mEvent, x)
                        1 -> mEventsListViewModel.updateFruits(mEvent, x)
                        2 -> mEventsListViewModel.updateSeeds(mEvent, x)
                    }
                    updateCountEditText()

                }.run()
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
                updateCountEditText()
                countEditText.requestFocus()
                countEditText.setSelection(countEditText.text.length)
                (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(countEditText, InputMethodManager.SHOW_FORCED)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateCountEditText()
                countEditText.requestFocus()
                countEditText.setSelection(countEditText.text.length)
                (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(countEditText, InputMethodManager.SHOW_FORCED)
            }
        })

        startObservers()
    }

    private fun startObservers() {

        mEventsListViewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {
                mEvents = it
                //updateCountEditText()
            }
        })
        mEventsListViewModel.getPollination(mEvent).observe(viewLifecycleOwner, Observer {
            it?.let {
                mFlowers = it.eventValue
                updateCountEditText()
            }
        })
        mEventsListViewModel.getHarvest(mEvent).observe(viewLifecycleOwner, Observer {
            it?.let {
                mHarvests = it.eventValue
                updateCountEditText()
            }
        })
        mEventsListViewModel.getThresh(mEvent).observe(viewLifecycleOwner, Observer {
            it?.let {
                mThreshes = it.eventValue
                updateCountEditText()
            }
        })
    }

    private fun updateCountEditText() {
        mBinding.countEditText.setText(when (mBinding.tabLayout.selectedTabPosition) {
            0 -> (mFlowers ?: "None").toString()
            1 -> (mHarvests ?: "None").toString()
            else -> (mThreshes ?: "None").toString()
        } )
    }

    private fun searchForParents(name: String) {
        if (::mEvents.isInitialized) {
            mEvents.forEach {
                if (it.eventDbId == name) {
                    findNavController().navigate(
                            EventFragmentDirections.actionEventFragmentSelf(it))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(org.phenoapps.intercross.R.menu.print_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            org.phenoapps.intercross.R.id.action_print -> {
                BluetoothUtil().templatePrint(requireContext(), arrayOf(mEvent))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}