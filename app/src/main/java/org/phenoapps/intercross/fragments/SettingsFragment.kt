package org.phenoapps.intercross.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.phenoapps.intercross.GeneralKeys
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory


class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository
                .getInstance(IntercrossDatabase.getInstance(requireContext()).settingsDao()))
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

    companion object {
        const val AUDIO_ENABLED = "org.phenoapps.intercross.AUDIO_ENABLED"
        const val BLANK = "org.phenoapps.intercross.BLANK_MALE_ID"
        const val ORDER = "org.phenoapps.intercross.CROSS_ORDER"
        const val COLLECT_INFO = "org.phenoapps.intercross.COLLECT_INFO"
    }
}
