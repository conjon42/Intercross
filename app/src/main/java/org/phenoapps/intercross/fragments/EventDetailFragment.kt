package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.viewmodels.EventDetailViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventDetailViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventDetailBinding


class EventDetailFragment: IntercrossBaseFragment<FragmentEventDetailBinding>(R.layout.fragment_event_detail) {


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

                    momName = parents.momName ?: parents.mom

                    dadName = parents.dadName ?: parents.dad
                }
            })


//            submitMetaDataButton.setOnClickListener {
//                mEvent.metaData.flowers=Integer.parseInt(flowerText.text.toString())
//                mEvent.metaData.fruits=Integer.parseInt(fruitText.text.toString())
//                mEvent.metaData.seeds=Integer.parseInt(seedText.text.toString())
//                mEventStore.update(mEvent)
//            }
//
//            maleName.setOnClickListener {
//                searchForParents(maleName.text.toString())
//            }
//
//            femaleName.setOnClickListener {
//                searchForParents(femaleName.text.toString())
//            }

        }
    }

//    private fun searchForParents(name: String) {
//
//        if (::mEvents.isInitialized) {
//
//            mEvents.forEach {
//
//                if (it.eventDbId == name) {
//
////                    findNavController().navigate(
////                            EventDetailFragmentDirections.actionEventFragmentSelf(it))
//                }
//            }
//        }
//    }

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