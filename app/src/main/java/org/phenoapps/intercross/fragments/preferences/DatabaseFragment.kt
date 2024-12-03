package org.phenoapps.intercross.fragments.preferences

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.DefineStorageActivity
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

        with ( findPreference<Preference>("org.phenoapps.intercross.STORAGE_DEFINER")) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { _ ->
                        startActivity(Intent(context, DefineStorageActivity::class.java))
                    }
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
    }
}
