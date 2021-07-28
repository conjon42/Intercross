package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceScreen
import org.phenoapps.intercross.R
import org.phenoapps.intercross.fragments.preferences.ToolbarPreferenceFragment

/**
 * Root preferences fragment that populates the setting categories.
 * Each category can be clicked to navigate to their corresponding fragment.
 */
class SettingsFragment : ToolbarPreferenceFragment(R.xml.preferences,
    "org.phenoapps.intercross.ROOT_PREFERENCES") {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.getBoolean("org.phenoapps.intercross.ASK_PERSON"))
                findNavController().navigate(SettingsFragmentDirections
                    .actionFromSettingsToProfileFragment())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.PROFILE_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToProfileFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.NAMING_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToNamingFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.WORKFLOW_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToWorkflowFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.PRINTING_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToPrintingFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.DATABASE_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToDatabaseFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.BRAPI_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToBrapiFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("org.phenoapps.intercross.ABOUT_SCREEN")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections.actionToAbout())

                    true
                }
            }
        }
    }
}
