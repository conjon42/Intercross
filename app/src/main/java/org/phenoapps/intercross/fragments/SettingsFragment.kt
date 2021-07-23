package org.phenoapps.intercross.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import org.phenoapps.intercross.GeneralKeys
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.dialogs.MetadataCreatorDialog
import org.phenoapps.intercross.dialogs.MetadataDefaultEditorDialog
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.util.Dialogs


class SettingsFragment : PreferenceFragmentCompat(), MetadataManager {

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository
                .getInstance(IntercrossDatabase.getInstance(requireContext()).settingsDao()))
    }

    private val eventsList: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).eventsDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        settingsModel.settings.observeForever { settings ->

            settings?.let {

                findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN").apply {

                    this?.let {

                        summary = when {

                            settings.isPattern -> {
                                "Pattern"
                            }
                            !settings.isUUID && !settings.isPattern -> {
                                "None"
                            }
                            else -> {
                                "UUID"
                            }
                        }
                    }
                }
            }
        }

        //ensure metadata creation / setting defaults preference is invisible by default
        context?.let { ctx ->

            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

            val metadataPref = findPreference<Preference>("org.phenoapps.intercross.META_DATA")
            val defaultsPref = findPreference<Preference>("org.phenoapps.intercross.META_DATA_DEFAULTS")

            val isCollect = prefs.getBoolean("org.phenoapps.intercross.COLLECT_INFO", false)

            defaultsPref?.isVisible = isCollect
            metadataPref?.isVisible = isCollect

        }

        with(findPreference<Preference>("org.phenoapps.intercross.ABOUT")){

            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(SettingsFragmentDirections
                        .actionToAbout())

                    true
                }
            }
        }
        with(findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN")){

            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(SettingsFragmentDirections
                            .actionToPatternFragment())

                    true
                }
            }
        }

        with(findPreference<Preference>("org.phenoapps.intercross.ZPL_IMPORT")){
            this?.let {
                setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections.actionToImportZplFragment())
                    true
                }
            }
        }

        with(findPreference<EditTextPreference>(GeneralKeys.BRAPI_BASE_URL)) {
            this?.let {
                setOnPreferenceChangeListener { _, newValue ->
                    context.getSharedPreferences("Settings", MODE_PRIVATE)
                            .edit().putString(GeneralKeys.BRAPI_BASE_URL, newValue.toString()).apply()
                    true
                }
            }
        }

        with (findPreference<Preference>("org.phenoapps.intercross.DATABASE_IMPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.importDatabase?.launch("application/zip")
                    }

                    true
                }
            }
        }

        with (findPreference<Preference>("org.phenoapps.intercross.DATABASE_EXPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.exportDatabase?.launch("intercross.zip")
                    }

                    true
                }
            }
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

                        MetadataCreatorDialog(ctx, this@SettingsFragment).show()

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
                                            this@SettingsFragment).show()

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

        val printSetup = findPreference<Preference>("org.phenoapps.intercross.PRINTER_SETUP")
        printSetup?.setOnPreferenceClickListener {
            val intent = activity?.packageManager
                    ?.getLaunchIntentForPackage("com.zebra.printersetup")
            when (intent) {
                null -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(
                            "https://play.google.com/store/apps/details?id=com.zebra.printersetup")
                    startActivity(i)
                }
                else -> {
                    startActivity(intent)
                }
            }
            true
        }

        setHasOptionsMenu(false)

        (activity as MainActivity).supportActionBar?.hide()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val askPerson = (arguments ?: Bundle())
                .getString("org.phenoapps.intercross.ASK_PERSON", "false")

        if (askPerson == "true") {
            preferenceManager.showDialog(findPreference<EditTextPreference>("org.phenoapps.intercross.PERSON"))
        }

//        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
//            val pref = findPreference<Preference>(key)
//
//            if (pref is ListPreference) {
//                pref.setSummary(pref.entry)
//            }
//        }
    }

    override fun onMetadataUpdated(property: String, value: Int) {
        //unimplemented
    }

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

    companion object {
        const val AUDIO_ENABLED = "org.phenoapps.intercross.AUDIO_ENABLED"
        const val BLANK = "org.phenoapps.intercross.BLANK_MALE_ID"
        const val ORDER = "org.phenoapps.intercross.CROSS_ORDER"
        const val COLLECT_INFO = "org.phenoapps.intercross.COLLECT_INFO"
    }
}
