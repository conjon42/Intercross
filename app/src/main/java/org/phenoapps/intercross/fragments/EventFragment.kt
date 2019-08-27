package org.phenoapps.intercross.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.FragmentEventBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.DateUtil


class EventFragment: IntercrossBaseFragment() {

    private lateinit var mBinding: FragmentEventBinding

    private lateinit var mEvents: List<Events>
    private var mHarvests: Int? = null
    private var mThreshes: Int? = null
    private var mFlowers: Int? = null
    private lateinit var mEvent: Events

    var mOrder: Int = 0
    var mAllowBlank: Boolean = false
    var mCollectData = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //TODO add shared prefs to base fragment interface
        val orderKey = "org.phenoapps.intercross.CROSS_ORDER"
        val blankKey = "org.phenoapps.intercross.BLANK_MALE_ID"
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mOrder = (pref.getString(orderKey, "0") ?: "0").toInt()
        mAllowBlank = pref.getBoolean(blankKey, false)
        mCollectData = pref.getBoolean(SettingsFragment.COLLECT_INFO, true)

        pref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when(key) {
                orderKey -> mOrder = (sharedPreferences.getString(key, "0") ?: "0").toInt()
                blankKey -> mAllowBlank = sharedPreferences.getBoolean(key, false)
                SettingsFragment.COLLECT_INFO -> mCollectData = sharedPreferences.getBoolean(key, true)
            }
        }

        setHasOptionsMenu(true)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        arguments?.getParcelable<Events>("events")?.let {
            mEvent = it
        }

        mBinding = FragmentEventBinding
                .inflate(inflater, container, false)

        if (mCollectData) {
            //mBinding.dateEditText.visibility = View.VISIBLE
            mBinding.countEditText.visibility = View.VISIBLE
            mBinding.tabLayout.visibility = View.VISIBLE
        } else {
            //mBinding.dateEditText.visibility = View.INVISIBLE
            mBinding.countEditText.visibility = View.INVISIBLE
            mBinding.tabLayout.visibility = View.INVISIBLE
        }

        mBinding.events = mEvent

        mBinding.maleName.setOnClickListener {
            searchForParents(mBinding.maleName.text.toString())
        }

        mBinding.femaleName.setOnClickListener {
            searchForParents(mBinding.femaleName.text.toString())
        }

        mBinding.button2.setOnClickListener {
            val x = mBinding.countEditText.text.toString()
            x.toIntOrNull()?.let { x ->
                Thread {
                    when (mBinding.tabLayout.selectedTabPosition) {
                        0 -> mEventsListViewModel.updateFlowers(mEvent, x)
                        1 -> mEventsListViewModel.updateFruits(mEvent, x)
                        2 -> mEventsListViewModel.updateSeeds(mEvent, x)
                    }
                    updateCountEditText()

                }.run()
            }
        }

        mBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
                updateCountEditText()
                mBinding.countEditText.requestFocus()
                mBinding.countEditText.setSelection(mBinding.countEditText.text.length)
                (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(mBinding.countEditText, InputMethodManager.SHOW_FORCED)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateCountEditText()
                mBinding.countEditText.requestFocus()
                mBinding.countEditText.setSelection(mBinding.countEditText.text.length)
                (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(mBinding.countEditText, InputMethodManager.SHOW_FORCED)
            }
        })

        startObservers()

        return mBinding.root
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