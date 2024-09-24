package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceScreen
import org.phenoapps.intercross.R
import org.phenoapps.intercross.fragments.preferences.ToolbarPreferenceFragment
import org.phenoapps.intercross.util.KeyUtil

import androidx.appcompat.app.AppCompatActivity

import com.bytehamster.lib.preferencesearch.SearchPreference

/**
 * Root preferences fragment that populates the setting categories.
 * Each category can be clicked to navigate to their corresponding fragment.
 */
class SettingsFragment : ToolbarPreferenceFragment(R.xml.preferences, R.string.root_preferences) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.getBoolean(mKeyUtil.argProfAskPerson))
                findNavController().navigate(SettingsFragmentDirections
                    .actionFromSettingsToProfileFragment())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchPreference = findPreference(mKeyUtil.searchPrefKey) as SearchPreference?

        //add all xml files to the search preference index
        with(searchPreference?.searchConfiguration) {
            this?.setActivity(activity as AppCompatActivity)
            arrayOf(R.xml.about_preferences, R.xml.database_preferences, R.xml.naming_preferences,
                R.xml.preferences, R.xml.printing_preferences, R.xml.profile_preferences,
                R.xml.workflow_preferences).forEach {
                    this?.index(it)
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_profile))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToProfileFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_naming))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToNamingFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_workflow))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToWorkflowFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_printing))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToPrintingFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_database))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections
                        .actionFromSettingsToDatabaseFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_brapi))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    //TODO
//                    findNavController().navigate(SettingsFragmentDirections
//                        .actionFromSettingsToBrapiFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_about))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections.actionToAbout())

                    true
                }
            }
        }
    }
}
