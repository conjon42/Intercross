package org.phenoapps.intercross

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class SettingsActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                //exitTransition  = Explode()
            }
        } else {
            // Swap without transition
        }

        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        supportActionBar?.apply {
            title = "Settings"
            themedContext
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                supportFinishAfterTransition()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            setPreferencesFromResource(org.phenoapps.intercross.R.xml.preferences, rootKey)

            val printSetup = findPreference<androidx.preference.Preference>("org.phenoapps.intercross.PRINTER_SETUP")
            printSetup.setOnPreferenceClickListener {
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

            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            if (pref.getBoolean("LABEL_PATTERN_CREATED", false)) {
                val patternCreated = findPreference<androidx.preference.Preference>(SettingsActivity.PATTERN)
                patternCreated.isEnabled = true
            }
        }

        override fun onResume() {
            super.onResume()
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            if (pref.getBoolean("LABEL_PATTERN_CREATED", false)) {
                val patternCreated = findPreference<androidx.preference.Preference>(SettingsActivity.PATTERN)
                patternCreated.isEnabled = true
            }
        }
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }
    companion object {
        val BLANK_MALE_ID = "org.phenoapps.intercross.BLANK_MALE_ID"
        val CROSS_ORDER = "org.phenoapps.intercross.CROSS_ORDER"
        var PERSON = "org.phenoapps.intercross.PERSON"
        val PATTERN = "org.phenoapps.intercross.LABEL_PATTERN"
        val BT_ID = "org.phenoapps.intercross.BLUETOOTH_ID"
    }
}