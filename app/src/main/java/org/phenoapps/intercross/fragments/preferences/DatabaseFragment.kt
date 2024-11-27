package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.util.KeyUtil

class DatabaseFragment : BasePreferenceFragment(R.xml.database_preferences, R.string.root_database) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_database_title))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }
}
