package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.databinding.FragmentEventBinding
import org.phenoapps.intercross.viewmodels.EventsListViewModel
import org.phenoapps.intercross.viewmodels.SettingsViewModel

class EventFragment: Fragment() {

    private lateinit var mBinding: FragmentEventBinding

    private lateinit var mEventsViewModel: EventsListViewModel

    private lateinit var mSettingsViewModel: SettingsViewModel

    private lateinit var mEvents: List<Events>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val events = arguments?.getParcelable<Events>("events")

        mBinding = FragmentEventBinding
                .inflate(inflater, container, false)

        mBinding.events = events

        mBinding.maleName.setOnClickListener {
            searchForParents(mBinding.maleName.text.toString())
        }

        mBinding.femaleName.setOnClickListener {
            searchForParents(mBinding.femaleName.text.toString())
        }

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
            }
        })

        mSettingsViewModel.settings.observe(viewLifecycleOwner, Observer {
            it?.let {
                mBinding.model = it
            }
        })
        return mBinding.root
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
        inflater.inflate(R.menu.activity_main_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_search_barcode -> {
                findNavController().navigate(
                        EventsFragmentDirections.actionToBarcodeScanFragment())
            }
        }
        return super.onOptionsItemSelected(item)
    }
}