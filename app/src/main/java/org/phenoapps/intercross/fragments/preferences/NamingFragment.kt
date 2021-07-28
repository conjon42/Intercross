package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory

class NamingFragment : ToolbarPreferenceFragment(R.xml.naming_preferences, R.string.root_naming) {

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository
                .getInstance(IntercrossDatabase.getInstance(requireContext()).settingsDao()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsModel.settings.observeForever { settings ->

            settings?.let {

                findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN").apply {

                    this?.let {

                        summary = when {

                            settings.isPattern -> {
                                "Pattern"
                            }
                            !settings.isUUID && !settings.isPattern -> {
                                "None"
                            }
                            else -> {
                                "UUID"
                            }
                        }
                    }
                }
            }
        }

        with(findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN")) {

            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(NamingFragmentDirections
                        .actionToPatternFragment())

                    true
                }
            }
        }
    }
}
