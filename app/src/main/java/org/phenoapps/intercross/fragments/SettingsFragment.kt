package org.phenoapps.intercross.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceScreen
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import org.phenoapps.intercross.GeneralKeys

import org.phenoapps.intercross.fragments.preferences.ToolbarPreferenceFragment
import org.phenoapps.intercross.util.KeyUtil
import org.phenoapps.intercross.R
import androidx.appcompat.app.AppCompatActivity



import com.bytehamster.lib.preferencesearch.SearchPreference
import org.phenoapps.intercross.activities.MainActivity

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

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setBackButtonToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchPreference = findPreference(mKeyUtil.searchPrefKey) as SearchPreference?

        //add all xml files to the search preference index
        with(searchPreference?.searchConfiguration) {
            this?.setActivity(activity as AppCompatActivity)
            arrayOf(
                R.xml.about_preferences, R.xml.database_preferences, R.xml.naming_preferences,
                R.xml.preferences, R.xml.printing_preferences, R.xml.profile_preferences,
                R.xml.workflow_preferences
            ).forEach {
                this?.index(it)
            }
        }
        addSummaryToPreference(R.string.root_profile, R.string.profile_summary)
        addSummaryToPreference(R.string.root_naming, R.string.naming_summary)
        addSummaryToPreference(R.string.root_workflow, R.string.workflow_summary)
        addSummaryToPreference(R.string.root_printing, R.string.printing_summary)
        addSummaryToPreference(R.string.root_database, R.string.database_summary)
        addSummaryToPreference(R.string.root_brapi, R.string.brapi_summary)
        addSummaryToPreference(R.string.root_about, R.string.about_summary)

        with(findPreference<PreferenceScreen>(getString(R.string.root_profile))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        SettingsFragmentDirections
                            .actionFromSettingsToProfileFragment()
                    )

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_naming))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        SettingsFragmentDirections
                            .actionFromSettingsToNamingFragment()
                    )

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_workflow))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        SettingsFragmentDirections
                            .actionFromSettingsToWorkflowFragment()
                    )

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_printing))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        SettingsFragmentDirections
                            .actionFromSettingsToPrintingFragment()
                    )

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_database))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        SettingsFragmentDirections
                            .actionFromSettingsToDatabaseFragment()
                    )

                    true
                }
            }
        }

        with(findPreference<EditTextPreference>(GeneralKeys.BRAPI_BASE_URL)) {
            this?.let {
                setOnPreferenceChangeListener { _, newValue ->
                    context.getSharedPreferences("Settings", MODE_PRIVATE)
                        .edit()
                        .putString(GeneralKeys.BRAPI_BASE_URL, newValue.toString())
                        .apply()
                    true
                }
            }
        }

        val printSetup =
            findPreference<Preference>("org.phenoapps.intercross.PRINTER_SETUP")
        printSetup?.setOnPreferenceClickListener {
            val intent = activity?.packageManager
                ?.getLaunchIntentForPackage("com.zebra.printersetup")
            when (intent) {
                null -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.zebra.printersetup"
                    )
                    startActivity(i)
                }

                else -> {
                    startActivity(intent)
                }
            }
            true
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

            true
        }
    }
    private fun addSummaryToPreference(preferenceKey: Int, summaryKey: Int) {
        findPreference<PreferenceScreen>(getString(preferenceKey))?.apply {
            summary = getString(summaryKey)
        }
    }
}
