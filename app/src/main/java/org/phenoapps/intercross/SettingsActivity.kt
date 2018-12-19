package org.phenoapps.intercross

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

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