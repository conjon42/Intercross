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
class PreferencesFragment : BasePreferenceFragment(R.xml.preferences, R.string.root_preferences) {

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
        searchPreference = findPreference(mKeyUtil.searchPrefKey) as SearchPreference?
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
                    try {
                        findNavController().navigate(PreferencesFragmentDirections.actionFromPreferencesFragmentToBehaviorPreferencesFragment())
                    } catch (e: Exception) {
                        e.printStackTrace()
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

    fun onSearchResultClicked(result: SearchPreferenceResult) {
        result.closeSearchPage(activity as MainActivity)
        if (result.resourceFile == R.xml.preferences) {
            // Handle preferences.xml case
            findPreference<SearchPreference>(mKeyUtil.searchPrefKey)?.isVisible = false
            scrollToPreference(result.key)
            result.highlight(this)
        } else {
            // Navigate to appropriate fragment using NavController
            findNavController().navigate(
                when (result.key) {
                    in mKeyUtil.profileKeySet -> R.id.profile_preference_fragment
                    in mKeyUtil.behaviorKeySet -> R.id.behavior_preferences_fragment
                    in mKeyUtil.printKeySet -> R.id.printing_preference_fragment
                    in mKeyUtil.dbKeySet -> R.id.database_preference_fragment
                    in mKeyUtil.aboutKeySet -> R.id.about_fragment
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
