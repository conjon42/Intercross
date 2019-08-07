package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.databinding.ActivityMainBinding
import org.phenoapps.intercross.fragments.SettingsFragment
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.viewmodels.WishlistViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mWishListViewModel: WishlistViewModel

    private lateinit var mNavController: NavController

    //location to save data
    private lateinit var mDirectory: File

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this@MainActivity,
                R.layout.activity_main)

        mDrawerLayout = binding.drawerLayout

        mNavController = Navigation.findNavController(this@MainActivity, R.id.nav_fragment)

        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
        }
        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        supportActionBar.apply {
            title = ""
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
                R.id.action_nav_settings -> {
                    mNavController.navigate(R.id.global_action_to_settings_fragment)
                }
                R.id.action_nav_parents -> {
                    mNavController.navigate(R.id.global_action_to_parents_fragment)
                }
                R.id.action_nav_import -> {
                    mWishListViewModel.deleteAll()
                    startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*" }, "Choose file to import"), REQ_FILE_IMPORT)
                }
                R.id.action_nav_intro -> {
                    startActivity(Intent(this@MainActivity, IntroActivity::class.java))
                }
                R.id.action_nav_summary -> {
                    mNavController.navigate(R.id.global_action_to_summary_fragment)
                }
            }
            mDrawerLayout.closeDrawers()
            true
        }

        //Show Tutorial Fragment for first-time users
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.apply {
            if (!getBoolean(SettingsFragment.TUTORIAL, false)) {
                startActivity(Intent(this@MainActivity, IntroActivity::class.java))
            }
        }

        pref.edit().apply{
            putBoolean(SettingsFragment.TUTORIAL, true)
            apply()
        }

        if (isExternalStorageWritable()) {
            mDirectory = File(Environment.getExternalStorageDirectory().path + "/Intercross")
            if (!mDirectory.isDirectory) {
                mDirectory.mkdirs()
            }
        }

        mWishListViewModel = ViewModelProviders.of(this@MainActivity,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WishlistViewModel(WishlistRepository.getInstance(
                                IntercrossDatabase.getInstance(this@MainActivity).wishlistDao())) as T

                    }
                }).get(WishlistViewModel::class.java)

    }

   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }*/
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)
        if (requestCode == REQ_FILE_IMPORT && resultCode == Activity.RESULT_OK) {
            i?.data?.let {
                val lines = FileUtil(this).parseUri(it)
                if (lines.isNotEmpty()) {
                    val headerLine = lines[0]
                    val headers = headerLine.split(",")
                    //TODO verify headers are correct
                    val numCols = headers.size
                    if (numCols == 7) { //lines = fid,mid,fname,mname,type,min,max
                        (lines - lines[0]).forEach {
                            val row = it.split(",")
                            if (row.size == numCols) {
                                mWishListViewModel.addWishlist(
                                        row[0], row[1], row[2], row[3], row[4], row[5].toIntOrNull() ?: 0, row[6].toIntOrNull() ?: 0
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    internal companion object {
        const val packageName = "org.phenoapps.intercross"

        //requests
        const val REQ_EXT_STORAGE = 101
        const val REQ_CAMERA = 102
        const val REQ_FILE_IMPORT = 103

    }

}
