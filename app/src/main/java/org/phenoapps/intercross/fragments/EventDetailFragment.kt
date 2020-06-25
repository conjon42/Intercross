package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.viewmodels.EventDetailViewModel
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventDetailViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventDetailBinding
import org.phenoapps.intercross.util.Dialogs


class EventDetailFragment: IntercrossBaseFragment<FragmentEventDetailBinding>(R.layout.fragment_event_detail) {

    val eventsList: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun getMetaDataVisibility(context: Context): Int {

        //determine if meta data collection is enabled
        val collect: String = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsFragment.COLLECT_INFO, "0") ?: "0"

        return collect.toInt()

    }

    override fun FragmentEventDetailBinding.afterCreateView() {

        arguments?.getLong("eid")?.let { rowid ->

            if (rowid == -1L) findNavController().navigate(
                    EventDetailFragmentDirections.actionToEvents())

            val viewModel: EventDetailViewModel by viewModels {
                EventDetailViewModelFactory(EventsRepository.getInstance(db.eventsDao()), rowid)
            }

            metaDataVisibility = getMetaDataVisibility(requireContext())

            viewModel.event.observeForever {

                it?.let {

                    event = it

                    eventDetailLayout.event = it
                }
            }


            //TODO add better query/view to return more Parent details (s.a id)
            viewModel.parents.observe(viewLifecycleOwner, Observer {

                it?.let { parents ->

                    momName = parents.momReadableName

                    dadName = parents.dadReadableName

                    momCode = parents.momCode

                    dadCode = parents.dadCode

                    eventsList.events.observe(viewLifecycleOwner, Observer {

                        it?.let { events ->

                            events.find { e -> e.eventDbId == parents.momCode }.let { mom ->

                                femaleName.setOnClickListener {

                                    if (mom?.id == null) {

                                        Dialogs.notify(AlertDialog.Builder(requireContext()),
                                            getString(R.string.parent_event_does_not_exist))

                                    } else findNavController()
                                            .navigate(EventDetailFragmentDirections
                                            .actionToParentEvent(mom.id ?: -1L))
                                }

                            }

                            events.find { e -> e.eventDbId == parents.dadCode }.let { dad ->

                                maleName.setOnClickListener {

                                    if (dad?.id == null) {

                                        Dialogs.notify(AlertDialog.Builder(requireContext()),
                                                getString(R.string.parent_event_does_not_exist))

                                    } else findNavController()
                                            .navigate(EventDetailFragmentDirections
                                            .actionToParentEvent(dad.id ?: -1L))
                                }
                            }
                        }
                    })
                }
            })


//            submitMetaDataButton.setOnClickListener {
//                mEvent.metaData.flowers=Integer.parseInt(flowerText.text.toString())
//                mEvent.metaData.fruits=Integer.parseInt(fruitText.text.toString())
//                mEvent.metaData.seeds=Integer.parseInt(seedText.text.toString())
//                mEventStore.update(mEvent)
//            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.cross_entry_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        when(item.itemId) {
//
//            R.id.action_print -> {
//
//                BluetoothUtil().print(requireContext(), arrayOf(mEvent))
//            }
//            R.id.action_delete -> {
//
//                val builder = AlertDialog.Builder(requireContext()).apply {
//
//                    setNegativeButton("Cancel") { _, _ ->
//
//                    }
//
//                    setPositiveButton("Confirm") { _, _ ->
//
//                        //mEventStore.delete(mEvent)
//
//                        findNavController().navigate(R.id.events_fragment)
//                    }
//                }
//
//                builder.setTitle("Delete this cross entry?")
//
//                builder.show()
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }
}