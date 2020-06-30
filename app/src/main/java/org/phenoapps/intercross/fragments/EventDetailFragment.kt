package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_event_detail.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventDetailViewModel
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventDetailViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventDetailBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.Dialogs


class EventDetailFragment: IntercrossBaseFragment<FragmentEventDetailBinding>(R.layout.fragment_event_detail) {

    private lateinit var mEvent: Event

    private val eventsList: EventListViewModel by viewModels {
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

    private val textUpdateWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {

            try {

                if (s.toString().isNotBlank()) {

                    eventsList.update(mEvent.apply {

                        metaData.fruits = fruitText.text.toString().toInt()

                    })
                    eventsList.update(mEvent.apply {

                        metaData.flowers = flowerText.text.toString().toInt()

                    })
                    eventsList.update(mEvent.apply {

                        metaData.seeds = seedText.text.toString().toInt()

                    })
                }

            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Log.d("InputError", e.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //TODO("Not yet implemented")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //TODO("Not yet implemented")
        }

    }

    override fun FragmentEventDetailBinding.afterCreateView() {

        arguments?.getLong("eid")?.let { rowid ->

            if (rowid == -1L) findNavController().popBackStack()

            val viewModel: EventDetailViewModel by viewModels {
                EventDetailViewModelFactory(EventsRepository.getInstance(db.eventsDao()), rowid)
            }

            metaDataVisibility = getMetaDataVisibility(requireContext())

            viewModel.event.observeForever {

                it?.let {

                    mEvent = it

                    event = it

                    eventDetailLayout.event = it

                    fruitText.addTextChangedListener(textUpdateWatcher)
                    flowerText.addTextChangedListener(textUpdateWatcher)
                    seedText.addTextChangedListener(textUpdateWatcher)
                }
            }

            //TODO add better query/view to return more Parent details (s.a id)
            viewModel.parents.observe(viewLifecycleOwner, Observer { data ->

                data?.let { parents ->

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

                                    } else findNavController().navigate(EventDetailFragmentDirections.actionToParentEvent(dad.id ?: -1L))
                                }
                            }
                        }
                    })
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.cross_entry_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_print -> {

                if (::mEvent.isInitialized) {

                    BluetoothUtil().print(requireContext(), arrayOf(mEvent))

                }
            }
            R.id.action_delete -> {

                Dialogs.onOk(AlertDialog.Builder(requireContext()),
                        getString(R.string.delete_cross_entry_title),
                        getString(R.string.cancel),
                        getString(R.string.zxing_button_ok)) {

                    eventsList.deleteById(mEvent.id ?: -1L)

                    findNavController().popBackStack()

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}