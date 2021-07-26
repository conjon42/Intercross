package org.phenoapps.intercross.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.phenoapps.intercross.GeneralKeys
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.databinding.FragmentPreferencesBinding


class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository
                .getInstance(IntercrossDatabase.getInstance(requireContext()).settingsDao()))
    }

    private var mBottomNavBar: BottomNavigationView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setPreferencesFromResource(R.xml.preferences, "org.phenoapps.intercross.ROOT_PREFERENCES")

        mBottomNavBar = view.findViewById(R.id.preferences_bottom_nav_bar)

        mBottomNavBar?.selectedItemId = R.id.action_nav_settings

        setupBottomNavBar()

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

        with(findPreference<Preference>("org.phenoapps.intercross.ABOUT")) {

            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(SettingsFragmentDirections
                        .actionToAbout())

                    true
                }
            }
        }
        with(findPreference<Preference>("org.phenoapps.intercross.CREATE_PATTERN")) {

            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(SettingsFragmentDirections
                        .actionToPatternFragment())

                    true
                }
            }
        }
        with(findPreference<Preference>("org.phenoapps.intercross.ZPL_IMPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    findNavController().navigate(SettingsFragmentDirections.actionToImportZplFragment())
                    true
                }
            }
        }
        with(findPreference<EditTextPreference>(GeneralKeys.BRAPI_BASE_URL)) {
            this?.let {
                setOnPreferenceChangeListener { _, newValue ->
                    context.getSharedPreferences("Settings", MODE_PRIVATE)
                        .edit().putString(GeneralKeys.BRAPI_BASE_URL, newValue.toString()).apply()
                    true
                }
            }
        }
        with (findPreference<Preference>("org.phenoapps.intercross.DATABASE_IMPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.importDatabase?.launch("application/zip")
                    }

                    true
                }
            }
        }
        with (findPreference<Preference>("org.phenoapps.intercross.DATABASE_EXPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    activity?.let { act ->
                        (act as? MainActivity)?.exportDatabase?.launch("intercross.zip")
                    }

                    true
                }
            }
        }

        val printSetup = findPreference<Preference>("org.phenoapps.intercross.PRINTER_SETUP")
        printSetup?.setOnPreferenceClickListener {
            val intent = activity?.packageManager
                ?.getLaunchIntentForPackage("com.zebra.printersetup")
            when (intent) {
                null -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.zebra.printersetup")
                    startActivity(i)
                }
                else -> {
                    startActivity(intent)
                }
            }
            true
        }

//        setHasOptionsMenu(false)

        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onResume() {
        super.onResume()

        mBottomNavBar?.selectedItemId = R.id.action_nav_settings

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val askPerson = (arguments ?: Bundle())
                .getString("org.phenoapps.intercross.ASK_PERSON", "false")

        if (askPerson == "true") {
            preferenceManager.showDialog(findPreference<EditTextPreference>("org.phenoapps.intercross.PERSON"))
        }
    }

    private fun setupBottomNavBar() {

        mBottomNavBar?.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_home -> {


                    findNavController().navigate(R.id.global_action_to_events)
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(R.id.global_action_to_parents)
                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        mBottomNavBar?.selectedItemId = R.id.action_nav_settings

                    }
                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(R.id.global_action_to_cross_count)
                }
            }

            true
        }
    }

    companion object {
        const val AUDIO_ENABLED = "org.phenoapps.intercross.AUDIO_ENABLED"
        const val BLANK = "org.phenoapps.intercross.BLANK_MALE_ID"
        const val ORDER = "org.phenoapps.intercross.CROSS_ORDER"
        const val COLLECT_INFO = "org.phenoapps.intercross.COLLECT_INFO"
    }
}
