package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.GeneralKeys
import org.phenoapps.intercross.R

class BrapiFragment : ToolbarPreferenceFragment(R.xml.brapi_preferences, R.string.root_brapi) {

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findPreference<EditTextPreference>(GeneralKeys.BRAPI_BASE_URL)) {
            this?.let {
                setOnPreferenceChangeListener { _, newValue ->
                    mPref.edit().putString(GeneralKeys.BRAPI_BASE_URL, newValue.toString()).apply()
                    true
                }
            }
        }
    }
}
