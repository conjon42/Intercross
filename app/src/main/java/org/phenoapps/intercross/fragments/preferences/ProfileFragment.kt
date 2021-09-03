package org.phenoapps.intercross.fragments.preferences

import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity

class ProfileFragment : ToolbarPreferenceFragment(R.xml.profile_preferences, R.string.root_profile) {

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).supportActionBar?.show()
    }
}