package org.phenoapps.intercross

import android.os.Bundle
import android.preference.PreferenceFragment

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(org.phenoapps.intercross.R.xml.preferences)


    }
}
