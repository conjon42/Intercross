package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.preference.*
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.dialogs.MetadataCreatorDialog
import org.phenoapps.intercross.dialogs.MetadataDefaultEditorDialog
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.util.Dialogs

class WorkflowFragment : ToolbarPreferenceFragment(R.xml.workflow_preferences,
    "org.phenoapps.intercross.ROOT_PREFERENCES_WORKFLOW"), MetadataManager {

    private val eventsList: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).eventsDao()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ensure metadata creation / setting defaults preference is invisible by default
        context?.let { ctx ->

            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

            val metadataPref = findPreference<Preference>("org.phenoapps.intercross.META_DATA")
            val defaultsPref = findPreference<Preference>("org.phenoapps.intercross.META_DATA_DEFAULTS")

            val isCollect = prefs.getBoolean("org.phenoapps.intercross.COLLECT_INFO", false)

            defaultsPref?.isVisible = isCollect
            metadataPref?.isVisible = isCollect

        }

        //when collect info is changed, update visibility of metadata preference
        with (findPreference<SwitchPreference>("org.phenoapps.intercross.COLLECT_INFO")) {
            this?.let {

                setOnPreferenceChangeListener { preference, newValue ->

                    findPreference<Preference>("org.phenoapps.intercross.META_DATA")?.let { metadataPref ->
                        metadataPref.isVisible = newValue as? Boolean ?: false
                    }

                    findPreference<Preference>("org.phenoapps.intercross.META_DATA_DEFAULTS")?.let { metadataPref ->
                        metadataPref.isVisible = newValue as? Boolean ?: false
                    }
                    true
                }
            }
        }

        //setup click listener to handle metadata creation when pressed
        with (findPreference<Preference>("org.phenoapps.intercross.META_DATA")) {
            this?.let {

                setOnPreferenceClickListener {

                    context?.let { ctx ->

                        MetadataCreatorDialog(ctx, this@WorkflowFragment).show()

                    }

                    true
                }
            }
        }

        //setup click listener to handle metadata creation when pressed
        with (findPreference<Preference>("org.phenoapps.intercross.META_DATA_DEFAULTS")) {
            this?.let {

                setOnPreferenceClickListener {

                    context?.let { ctx ->

                        eventsList.events.observeOnce {

                            it?.first()?.let { x ->

                                val defaults = x.getMetadataDefaults()

                                val properties = defaults.map { it.first }.toTypedArray()

                                val viewed = defaults.map { "${it.first} -> ${it.second}" }
                                    .toTypedArray()

                                if (properties.isNotEmpty()) {

                                    AlertDialog.Builder(ctx).setSingleChoiceItems(viewed, 0) { dialog, item ->

                                        val default = defaults.toMap()[properties[item]] ?: 1

                                        MetadataDefaultEditorDialog(ctx,
                                            properties[item],
                                            default,
                                            this@WorkflowFragment).show()

                                        dialog.dismiss()

                                    }.show()

                                } else Toast.makeText(ctx, R.string.fragment_settings_no_metadata_exists, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    true
                }
            }
        }
    }

    override fun onMetadataUpdated(property: String, value: Int) {}

    //asks the user to delete the property,
    //metadata entryset size is monotonic across all rows
    override fun onMetadataLongClicked(property: String) {

        context?.let { ctx ->

            Dialogs.onOk(
                AlertDialog.Builder(ctx),
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
            }
        }
    }

    override fun onMetadataDefaultUpdated(property: String, value: Int) {

        eventsList.events.observeOnce {

            it.forEach {

                eventsList.update(
                    it.apply {
                        updateMetadataDefault(value, property)
                    }
                )
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

    //updates a property with a new default value
    private fun Event.updateMetadataDefault(value: Int, property: String) = try {

        val element = JsonParser.parseString(this.metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            json[property].asJsonArray[1] = JsonPrimitive(value)

            this.metadata = json.toString()

        } else throw JsonSyntaxException("Malformed metadata format found: ${element.asString}")

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()
    }

    //returns array of metadata properties with default values
    private fun Event.getMetadataDefaults(): List<Pair<String, Int>> = try {

        val element = JsonParser.parseString(this.metadata)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            //return a set of property names to their default values
            json.entrySet().map { it.key to it.value.asJsonArray[1].asInt }

        } else throw JsonSyntaxException("Malformed metadata format found: ${element.asString}")

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()

        listOf()
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
}
