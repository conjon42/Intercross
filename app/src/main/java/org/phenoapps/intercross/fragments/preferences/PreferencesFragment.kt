package org.phenoapps.intercross.fragments.preferences


import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.bytehamster.lib.preferencesearch.SearchConfiguration
import com.bytehamster.lib.preferencesearch.SearchPreference
import com.bytehamster.lib.preferencesearch.SearchPreferenceResult
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.util.KeyUtil

/**
 * Root preferences fragment that populates the setting categories.
 * Each category can be clicked to navigate to their corresponding fragment.
 */
class PreferencesFragment : BasePreferenceFragment(R.xml.preferences) {

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private var searchPreference: SearchPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as? MainActivity)?.setPreferencesFragment(this)

        arguments?.let { args ->
            if (args.getBoolean(GeneralKeys.MODIFY_PROFILE_SETTINGS)) {
                    findNavController().navigate(PreferencesFragmentDirections.actionFromPreferencesFragmentToProfileFragment())
            }
            if (args.getBoolean(GeneralKeys.PERSON_UPDATE)) {
                findNavController().navigate(PreferencesFragmentDirections.actionFromPreferencesFragmentToProfileFragment(PERSONUPDATE = true))
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        setSearchConfiguration()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setPreferencesFragment(this)
        (activity as MainActivity).setToolbar()
    }

    fun setSearchConfiguration() {
        searchPreference = findPreference("searchPreference") as SearchPreference?
        if (searchPreference != null) {
            val searchConfiguration = searchPreference?.searchConfiguration
            searchConfiguration.apply {

                this?.setActivity(activity as AppCompatActivity)
                arrayOf(
                    R.xml.preferences,
                    R.xml.profile_preferences,
                    R.xml.behavior_preferences,
                    R.xml.printing_preferences,
                    R.xml.database_preferences,
                ).forEach {
                    this?.index(it)
                }
                this?.setFragmentContainerViewId(android.R.id.list_container)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findPreference<PreferenceScreen>("pref_key_profile_settings")) {
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

        with(findPreference<PreferenceScreen>("pref_key_behavioral_settings")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    try {
                        findNavController().navigate(PreferencesFragmentDirections.actionFromPreferencesFragmentToBehaviorPreferencesFragment())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("pref_key_printing_settings")) {
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

        with(findPreference<PreferenceScreen>("pref_key_brapi_settings")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    //TODO
//                    findNavController().navigate(SettingsFragmentDirections
//                        .actionFromSettingsToBrapiFragment())

                    true
                }
            }
        }

        with(findPreference<PreferenceScreen>("pref_key_about_settings")) {
            this?.let { it ->
                it.setOnPreferenceClickListener {
                    findNavController().navigate(PreferencesFragmentDirections.actionToAbout())

                    true
                }
            }

            true
        }

        with(findPreference<PreferenceScreen>("pref_key_database_settings")) {
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

    fun onSearchResultClicked(result: SearchPreferenceResult) {
        result.closeSearchPage(activity as MainActivity)
        if (result.resourceFile == R.xml.preferences) {
            // Handle preferences.xml case
            findPreference<SearchPreference>("searchPreference")?.isVisible = false
            scrollToPreference(result.key)
            result.highlight(this)
        } else {
            // Navigate to appropriate fragment using NavController
            findNavController().navigate(
                when (result.resourceFile) {
                    R.xml.profile_preferences -> R.id.profile_preference_fragment
                    R.xml.behavior_preferences -> R.id.behavior_preferences_fragment
                    R.xml.printing_preferences -> R.id.printing_preference_fragment
                    R.xml.database_preferences -> R.id.database_preference_fragment
                    else -> throw RuntimeException()
                }
            )
            scrollToPreference(result.key)
            result.highlight(this)
        }
    }

    private fun destroySearchPreferencesFragment() {
        requireActivity().supportFragmentManager.let { fm ->
            // remove backstack entry
            fm.popBackStack("SearchPreferenceFragment",
                FragmentManager.POP_BACK_STACK_INCLUSIVE)

            // remove the fragment
            fm.findFragmentByTag("SearchPreferenceFragment")?.let { searchFragment ->
                fm.beginTransaction()
                    .remove(searchFragment)
                    .commitNow()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroySearchPreferencesFragment()
        (activity as? MainActivity)?.setPreferencesFragment(null)
    }
}
