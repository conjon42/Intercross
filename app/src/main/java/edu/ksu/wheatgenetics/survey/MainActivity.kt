package edu.ksu.wheatgenetics.survey

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import edu.ksu.wheatgenetics.survey.databinding.ActivityMainBinding
import java.io.File

//The purpose of this class is to allow the user to choose an experiment ID to continue/start surveying on
//Experiment Activity
class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mNavController: NavController

    //location to save Survey data s.a exporting lat/lng .csv
    private lateinit var mSurveyDirectory: File

    private fun isExternalStorageWritable(): Boolean  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQ_EXT_STORAGE)
            }
        } else
            return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        return false
    }

    override fun onStart() {
        super.onStart()

        if (isFineLocationAccessible()) {
            setupLocationUpdates()
        }

        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
        }
        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        supportActionBar.apply {
            title = "Survey"
            this?.let {
                it.themedContext
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }

        // Setup drawer view
        val nvDrawer = findViewById<NavigationView>(R.id.nvView)

        nvDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_plot_satellites -> {
                    //keep the user from opening multiple satellite fragments
                    if (!(mNavController.currentDestination?.label ?: "").contains("Satellite"))
                        mNavController.navigate(R.id.satellite_plot_fragment)
                }
            }
            mDrawerLayout.closeDrawers()
            true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_main)

        mDrawerLayout = binding.drawerLayout

        mNavController = Navigation.findNavController(this, R.id.experiment_nav_fragment)

        if (isExternalStorageWritable()) {
            mSurveyDirectory = File(Environment.getExternalStorageDirectory().path + "/Survey")
            if (!mSurveyDirectory.isDirectory) {
                mSurveyDirectory.mkdirs()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            android.R.id.home -> dl.openDrawer(GravityCompat.START)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(resultCode: Int, permissions: Array<String>, granted: IntArray) {
        permissions.forEachIndexed { index, perm ->
            when {
                perm == Manifest.permission.ACCESS_FINE_LOCATION
                        && granted[index] == PackageManager.PERMISSION_GRANTED -> {

                }
                perm == Manifest.permission.WRITE_EXTERNAL_STORAGE
                        && granted[index] == PackageManager.PERMISSION_GRANTED -> {

                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    //stop the geo nav service
    override fun onStop() {
        super.onStop()
        removeLocationUpdates()
    }

    private fun removeLocationUpdates() {
        val geoNavServiceIntent = Intent(this, GeoNavService::class.java)
        stopService(geoNavServiceIntent)
    }

    /**
     * 1. starts geo nav service
     * 2. sets up communication between service and this context
     * 3. attempts to get google map
     */
    private fun setupLocationUpdates() {
        val geoNavServiceIntent = Intent(this, GeoNavService::class.java)
        ContextCompat.startForegroundService(this, geoNavServiceIntent)
    }

    private fun isFineLocationAccessible(): Boolean {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) return true
        else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_FINE_LOCATION)

        return false
    }

    internal companion object {
        const val packageName = "org.phenoapps.survey"
        const val EXTRA_EXPERIMENT_ID = "$packageName.EXTRA_EXPERIMENT_ID"

        //requests
        const val REQ_EXT_STORAGE = 101
        const val REQ_FINE_LOCATION = 102
    }

}
