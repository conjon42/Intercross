package org.phenoapps.intercross.fragments

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.phenoapps.intercross.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val askPerson = (arguments ?: Bundle())
                .getString("org.phenoapps.intercross.ASK_PERSON", "false")

        if (askPerson == "true") {
            preferenceManager.showDialog(findPreference<EditTextPreference>("org.phenoapps.intercross.PERSON"))
        }

        findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN").setOnPreferenceClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionToPatternFragment())
            true
        }


    }

    companion object {
        const val TUTORIAL = "org.phenoapps.intercross.TUTORIAL"
        const val AUDIO_ENABLED = "org.phenoapps.intercross.AUDIO_ENABLED"
        const val COLLECT_INFO = "org.phenoapps.intercross.COLLECT_INFO"

    }
}
