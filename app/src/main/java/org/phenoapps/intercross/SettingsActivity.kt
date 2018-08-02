package org.phenoapps.intercross

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

class SettingsActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null) {
            supportActionBar!!.title = null
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

        var SCAN_MODE_LIST = "org.phenoapps.verify.SCAN_MODE"
        var AUDIO_ENABLED = "org.phenoapps.verify.AUDIO_ENABLED"
        var TUTORIAL_MODE = "org.phenoapps.verify.TUTORIAL_MODE"
        var FIRST_NAME = "org.phenoapps.verify.FIRST_NAME"
        var LAST_NAME = "org.phenoapps.verify.LAST_NAME"
        var LIST_KEY_NAME = "org.phenoapps.verify.LIST_KEY_NAME"
        var PAIR_NAME = "org.phenoapps.verify.PAIR_NAME"
        var DISABLE_PAIR = "org.phenoapps.verify.DISABLE_PAIR"
        var AUX_INFO = "org.phenoapps.verify.AUX_INFO"
        var HEADER_SET = "org.phenoapps.intercross.HEADER_SET"
    }
}