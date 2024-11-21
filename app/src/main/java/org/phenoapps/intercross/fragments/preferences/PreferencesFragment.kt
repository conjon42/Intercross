package org.phenoapps.intercross.fragments.preferences

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

import org.phenoapps.intercross.util.KeyUtil
import org.phenoapps.intercross.R
import androidx.appcompat.app.AppCompatActivity
import android.util.Log



import com.bytehamster.lib.preferencesearch.SearchPreference
import org.phenoapps.intercross.activities.MainActivity

/**
 * Root preferences fragment that populates the setting categories.
 * Each category can be clicked to navigate to their corresponding fragment.
 */
class PreferencesFragment : ToolbarPreferenceFragment(R.xml.preferences, R.string.root_preferences) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            if (args.getBoolean(mKeyUtil.argProfAskPerson))
                findNavController().navigate(PreferencesFragmentDirections
                    .actionFromPreferencesFragmentToProfileFragment())
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
                R.xml.about_preferences, R.xml.database_preferences, R.xml.behavior_preferences,
                R.xml.preferences, R.xml.printing_preferences, R.xml.profile_preferences,
            ).forEach {
                this?.index(it)
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_profile))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        PreferencesFragmentDirections
                            .actionFromPreferencesFragmentToProfileFragment()
                    )
                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_behavior))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    Log.d("SettingsFragment", "Behavior preference clicked")
                    try {
                        findNavController().navigate(PreferencesFragmentDirections.actionFromPreferencesFragmentToBehaviorPreferencesFragment())
                    } catch (e: Exception) {
                        Log.e("SettingsFragment", "Error navigating to Behavior: ${e.message}", e)
                    }
                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_printing))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        PreferencesFragmentDirections
                            .actionFromPreferencesFragmentToPrintingFragment()
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
                    findNavController().navigate(PreferencesFragmentDirections.actionToAbout())

                    true
                }
            }

            true
        }

        with(findPreference<PreferenceScreen>(getString(R.string.root_database))) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(
                        PreferencesFragmentDirections
                            .actionFromPreferencesFragmentToDatabaseFragment()
                    )
                    true
                }
            }
        }
    }
}
