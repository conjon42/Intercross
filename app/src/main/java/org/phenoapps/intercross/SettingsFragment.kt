package org.phenoapps.intercross

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(org.phenoapps.intercross.R.xml.preferences)

        val sharedPrefs = super.getPreferenceManager().sharedPreferences
        val mode = findPreference(SettingsActivity.SCAN_MODE_LIST) as ListPreference

        mode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, o ->
            //check if Pair mode is chosen, if it's disabled then show a message and switch
            //back to default mode.
            if (o == "4" && sharedPrefs.getBoolean(SettingsActivity.DISABLE_PAIR, false)) {
                (preference as ListPreference).value = "0"
                Toast.makeText(activity,
                        "Pair mode cannot be used without setting a pair ID.",
                        Toast.LENGTH_SHORT).show()
                return@OnPreferenceChangeListener false
            }
            true
        }
    }
}
