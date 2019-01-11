package org.phenoapps.intercross

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast


class SettingsActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null) {
            supportActionBar!!.title = "Settings"
            supportActionBar!!.themedContext
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
        }

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            addPreferencesFromResource(org.phenoapps.intercross.R.xml.preferences)

            val printSetup = findPreference("org.phenoapps.intercross.PRINTER_SETUP")
            printSetup.setOnPreferenceClickListener {
                val intent = activity.packageManager
                        .getLaunchIntentForPackage("com.zebra.printersetup")
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
                val patternCreated = findPreference(SettingsActivity.PATTERN)
                patternCreated.isEnabled = true
            }
        }

        override fun onResume() {
            super.onResume()
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            if (pref.getBoolean("LABEL_PATTERN_CREATED", false)) {
                val patternCreated = findPreference(SettingsActivity.PATTERN)
                patternCreated.isEnabled = true
            }
        }
    }

    companion object {

        val BLANK_MALE_ID = "org.phenoapps.intercross.BLANK_MALE_ID"
        val CROSS_ORDER = "org.phenoapps.intercross.CROSS_ORDER"
        val HEADER_SET = "org.phenoapps.intercross.HEADER_SET"
        var PERSON = "org.phenoapps.intercross.PERSON"
        var LOCATION = "org.phenoapps.intercross.LOCATION"
        var PRINTER = "org.phenoapps.intercross.PRINTER"
        var FEMALE_FIRST = "org.phenoapps.intercross.FEMALE_FIRST"
        val PATTERN = "org.phenoapps.intercross.LABEL_PATTERN"
        val BT_ID = "org.phenoapps.intercross.BLUETOOTH_ID"
    }
}