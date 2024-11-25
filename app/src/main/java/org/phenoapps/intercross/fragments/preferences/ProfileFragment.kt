package org.phenoapps.intercross.fragments.preferences

import androidx.appcompat.app.AppCompatActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity

class ProfileFragment : BasePreferenceFragment(R.xml.profile_preferences, R.string.root_profile) {

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_profile_title))
    }
}