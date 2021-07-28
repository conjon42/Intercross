package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R

class DatabaseFragment : ToolbarPreferenceFragment(R.xml.database_preferences, R.string.root_database) {

    private var mBottomNavBar: BottomNavigationView? = null

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
