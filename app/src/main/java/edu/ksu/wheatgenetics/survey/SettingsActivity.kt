package edu.ksu.wheatgenetics.survey

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Chaney on 4/20/2017.
 */

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getSupportActionBar() != null) {
            getSupportActionBar()?.setTitle(null)
            getSupportActionBar()?.getThemedContext()
            getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
            getSupportActionBar()?.setHomeButtonEnabled(true)
        }

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_OK)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        var PERSON = "edu.ksu.wheatgenetics.survey.PERSON"
        var EXPERIMENT = "edu.ksu.wheatgenetics.survey.EXPERIMENT_ID"
        var MIN_ACCURACY = "edu.ksu.wheatgenetics.survey.MIN_ACCURACY"
        var MIN_DISTANCE = "edu.ksu.wheatgenetics.survey.MIN_DISTANCE"
        var MIN_TIME = "edu.ksu.wheatgenetics.survey.MIN_TIME"
    }
}