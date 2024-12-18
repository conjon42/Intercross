package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.util.KeyUtil

class DatabaseFragment : BasePreferenceFragment(R.xml.database_preferences) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_database_title))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (findPreference<Preference>(getString(R.string.key_pref_db_import))) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.importDatabase?.launch("application/zip")
                    }

                    true
                }
            }
        }
        with (findPreference<Preference>(getString(R.string.key_pref_db_export))) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.exportDatabase?.launch("intercross.zip")
                    }

                    true
                }
            }
        }
    }
}
