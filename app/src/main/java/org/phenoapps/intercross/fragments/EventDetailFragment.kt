package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.MetadataAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Metadata
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.EventDetailViewModel
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventDetailViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventDetailBinding
import org.phenoapps.intercross.dialogs.MetadataCreatorDialog
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.FileUtil


class EventDetailFragment:
    IntercrossBaseFragment<FragmentEventDetailBinding>(R.layout.fragment_event_detail),
    MetadataManager {

    private lateinit var mEvent: Event

    private val eventsList: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private lateinit var eventDetailViewModel: EventDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun getMetaDataVisibility(context: Context): Int {

        //determine if meta data collection is enabled
        val collect: Boolean = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SettingsFragment.COLLECT_INFO, false)

        return if (collect) View.VISIBLE else View.GONE

    }

    //updates a property's value
    private fun Event.updateMetadata(value: Int, property: String) = try {

        val element = JsonParser.parseString(this.metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            if (json.has(property)) {

                val old = json.getAsJsonArray(property)

                json.remove(property)

                json.add(property, JsonArray(2).apply {
                    add(JsonPrimitive(value))
                    add(old[1])
                })

                this.metadata = json.toString()

            } else {

                json.add(property, JsonArray(2).apply {
                    add(JsonPrimitive(value))
                    add(JsonPrimitive(value))
                })

                this.metadata = json.toString()
            }

        } else throw JsonSyntaxException("Malformed metadata format found: ${element.asString}")

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()
    }

    //adds the new default value and property to the metadata string
    private fun Event.createNewMetadata(value: Int, property: String) = try {

        val element = JsonParser.parseString(this.metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            json.remove(property)

            json.add(property, JsonArray(2).apply {
                add(JsonPrimitive(value))
                add(JsonPrimitive(value))
            })

            this.metadata = json.toString()

        } else throw JsonSyntaxException("Malformed metadata format found: ${element.asString}")

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()
    }

    //deletes the given property from the metdata string
    private fun Event.deleteMetadata(property: String) = try {

        val element = JsonParser.parseString(this.metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            json.remove(property)

            this.metadata = json.toString()

        } else throw JsonSyntaxException("Malformed metadata format found: ${element.asString}")

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()
    }

    override fun FragmentEventDetailBinding.afterCreateView() {

        arguments?.getLong("eid")?.let { rowid ->

            if (rowid == -1L){

                FileUtil(requireContext()).ringNotification(false)

                findNavController().popBackStack()
            }

            val viewModel: EventDetailViewModel by viewModels {
                EventDetailViewModelFactory(EventsRepository.getInstance(db.eventsDao()), rowid)
            }

            eventDetailViewModel = viewModel

            metaDataVisibility = getMetaDataVisibility(requireContext())

            eventDetailMetadataRecyclerView.adapter = MetadataAdapter(this@EventDetailFragment)

            fragEventDetailAddMetadataButton.setOnClickListener {

                context?.let { ctx ->

                    MetadataCreatorDialog(ctx, this@EventDetailFragment).show()

                }
            }

            refreshObservers()

            refreshMetadata()
        }
    }

    //loads the metadata into the ui
    //this doesn't listen forever to avoid circular recursive updates
    private fun refreshMetadata() {

        eventDetailViewModel.metadata.observeOnce { metadata ->

            val json = JsonParser.parseString(metadata)

            (mBinding.eventDetailMetadataRecyclerView.adapter as MetadataAdapter)
                .submitList(json.asJsonObject.entrySet()
                    .map { Metadata(it.key, it.value.asJsonArray[0].asInt) }
                    .sortedBy { it.property })

            mBinding.eventDetailMetadataRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    //when new properties are added, the fragment is refreshed to reload the metadata list
    private fun refreshFragment() {

        mEvent.id?.let { eid ->

            findNavController().navigate(EventDetailFragmentDirections.actionToEventRefresh(eid))

        }
    }

    private fun FragmentEventDetailBinding.refreshObservers() {

        if (::eventDetailViewModel.isInitialized) {

            eventDetailViewModel.event.observe(viewLifecycleOwner) {

                it?.let {

                    mEvent = it

                    event = it

                    eventDetailLayout.event = it

                    eventDetailLayout.timestamp = if ("_" in it.timestamp) {

                        it.timestamp.split("_")[0]

                    } else it.timestamp
                }
            }

            eventDetailViewModel.parents.observe(viewLifecycleOwner) { data ->

                data?.let { parents ->

                    eventDetailLayout.female = parents.momReadableName

                    eventDetailLayout.male = parents.dadReadableName

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

                                    } else {
                                        findNavController()
                                            .navigate(EventDetailFragmentDirections
                                                .actionToParentEvent(mom.id ?: -1L))
                                    }
                                }

                            }

                            events.find { e -> e.eventDbId == parents.dadCode }.let { dad ->

                                maleName.setOnClickListener {

                                    if (dad?.id == null) {

                                        Dialogs.notify(AlertDialog.Builder(requireContext()),
                                            getString(R.string.parent_event_does_not_exist))

                                    } else {
                                        findNavController().navigate(EventDetailFragmentDirections
                                            .actionToParentEvent(dad.id ?: -1L))
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.cross_entry_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (::mEvent.isInitialized) {

            when (item.itemId) {

                R.id.action_print -> {

                    BluetoothUtil().print(requireContext(), arrayOf(mEvent))

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
        }

        return super.onOptionsItemSelected(item)
    }

    //updates a single row value for the current event
    override fun onMetadataUpdated(property: String, value: Int) {

        eventsList.update(mEvent.apply {

            updateMetadata(value, property)

        })
    }

    //extension function for live data to only observe once when the data is not null
    private fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observe(viewLifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                t?.let { data ->
                    observer.onChanged(data)
                    removeObserver(this)
                }
            }
        })
    }

    //asks the user to delete the property,
    //metadata entryset size is monotonic across all rows
    override fun onMetadataLongClicked(property: String) {

        context?.let { ctx ->

            Dialogs.onOk(AlertDialog.Builder(ctx),
                title = getString(R.string.dialog_confirm_remove_metadata),
                cancel = getString(android.R.string.cancel),
                ok = getString(android.R.string.ok),
                message = getString(R.string.dialog_confirm_remove_for_all)) {

                eventsList.events.observeOnce {

                    it.forEach {

                        eventsList.update(
                            it.apply {
                                deleteMetadata(property)
                            }
                        )
                    }
                }

                refreshFragment()
            }
        }
    }

    //adds the new property to all crosses in the database
    override fun onMetadataCreated(property: String, value: String) {

        eventsList.events.observeOnce {

            it.forEach {

                eventsList.update(
                    it.apply {
                        createNewMetadata(value.toInt(), property)
                    }
                )
            }
        }

        refreshFragment()
    }
}