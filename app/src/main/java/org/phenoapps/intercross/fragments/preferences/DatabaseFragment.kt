package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.util.KeyUtil

class DatabaseFragment : ToolbarPreferenceFragment(R.xml.database_preferences, R.string.root_database) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setBackButtonToolbar()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (findPreference<Preference>(mKeyUtil.dbImportKey)) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.importDatabase?.launch("application/zip")
                    }

                    true
                }
            }
        }
        with (findPreference<Preference>(mKeyUtil.dbExportKey)) {
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
