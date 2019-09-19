package org.phenoapps.intercross.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.viewmodels.SettingsViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var mSettingsViewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        with(findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN")){
            setOnPreferenceClickListener {
                findNavController().navigate(SettingsFragmentDirections.actionToPatternFragment())
                true
            }

            mSettingsViewModel.settings.observe(viewLifecycleOwner, Observer {
                it?.let {
                    summary = when {
                        it.isPattern -> {
                            "Pattern"
                        }
                        it.isUUID -> {
                            "UUID"
                        }
                        else -> {
                            "None"
                        }
                    }
                }
            })
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

        val db = IntercrossDatabase.getInstance(requireContext())

        mSettingsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(SettingsRepository.getInstance(
                                db.settingsDao())) as T

                    }
                }).get(SettingsViewModel::class.java)
    }

    companion object {
        const val TUTORIAL = "org.phenoapps.intercross.TUTORIAL"
        const val AUDIO_ENABLED = "org.phenoapps.intercross.AUDIO_ENABLED"
        const val COLLECT_INFO = "org.phenoapps.intercross.COLLECT_INFO"
        const val PERSON = "org.phenoapps.intercross.PERSON"
        const val EXPERIMENT = "org.phenoapps.intercross.EXPERIMENT"
    }
}
