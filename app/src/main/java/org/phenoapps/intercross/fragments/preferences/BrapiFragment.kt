package org.phenoapps.intercross.fragments.preferences

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import org.phenoapps.intercross.GeneralKeys
import org.phenoapps.intercross.R

class BrapiFragment : ToolbarPreferenceFragment(R.xml.brapi_preferences,
    "org.phenoapps.intercross.ROOT_PREFERENCES_BRAPI") {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findPreference<EditTextPreference>(GeneralKeys.BRAPI_BASE_URL)) {
            this?.let {
                setOnPreferenceChangeListener { _, newValue ->
                    context.getSharedPreferences("Settings", MODE_PRIVATE)
                        .edit().putString(GeneralKeys.BRAPI_BASE_URL, newValue.toString()).apply()
                    true
                }
            }
        }
    }
}
