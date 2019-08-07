package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_event.view.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.databinding.FragmentEventBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.viewmodels.EventsListViewModel
import org.phenoapps.intercross.viewmodels.SettingsViewModel

class EventFragment: Fragment() {

    private lateinit var mBinding: FragmentEventBinding

    private lateinit var mEventsViewModel: EventsListViewModel

    private lateinit var mSettingsViewModel: SettingsViewModel

    private lateinit var mEvents: List<Events>

    private lateinit var mEvent: Events

    private var mCollectData = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        arguments?.getParcelable<Events>("events")?.let {
            mEvent = it
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mCollectData = pref.getBoolean(SettingsFragment.COLLECT_INFO, true)

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

        if (mEvent.flowers == 0) mBinding.tabLayout.getTabAt(0)?.select()
        else if (mEvent.fruits == 0) mBinding.tabLayout.getTabAt(1)?.select()
        else mBinding.tabLayout.getTabAt(2)?.select()

        updateCountEditText()

        mBinding.button2.setOnClickListener {
            val x = mBinding.countEditText.text.toString().toIntOrNull() ?: 0
            mEventsViewModel.update(mEvent.apply {
                when (mBinding.tabLayout.selectedTabPosition) {
                    0 -> flowers = x
                    1 -> fruits = x
                    2 -> seeds = x
                }
            })
        }

        mBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateCountEditText()
            }
        })

        mSettingsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(SettingsRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).settingsDao())) as T
                    }
                }).get(SettingsViewModel::class.java)

        mEventsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventsListViewModel(EventsRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).eventsDao())) as T
                    }
                }).get(EventsListViewModel::class.java)

        mEventsViewModel.events.observe(viewLifecycleOwner, Observer {
            it?.let {
                mEvents = it
                updateCountEditText()
            }
        })

        return mBinding.root
    }

    private fun updateCountEditText() {
        mBinding.countEditText.setText(when (mBinding.tabLayout.selectedTabPosition) {
            0 -> mEvent.flowers.toString()
            1 -> mEvent.fruits.toString()
            else -> mEvent.seeds.toString()
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
        inflater.inflate(R.menu.print_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_print -> {
                BluetoothUtil().templatePrint(requireContext(), arrayOf(mEvent))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}