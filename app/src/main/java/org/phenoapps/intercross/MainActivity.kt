package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.databinding.ActivityMainBinding
import org.phenoapps.intercross.fragments.PatternFragment
import org.phenoapps.intercross.fragments.SettingsFragment
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.EventsListViewModel
import org.phenoapps.intercross.viewmodels.ParentsViewModel
import org.phenoapps.intercross.viewmodels.WishlistViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var mSnackbar: SnackbarQueue

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mWishListViewModel: WishlistViewModel

    private lateinit var mEventsViewModel: EventsListViewModel
    private lateinit var mEvents: List<Events>

    private lateinit var mParentsViewModel: ParentsViewModel

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mNavController: NavController

    //location to save data
    private val mDirectory: File by lazy {
        File(Environment.getExternalStorageDirectory().path + "/Intercross")
    }

    private fun isExternalStorageWritable(): Boolean {
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

    private fun setupDirs() {
        if (!mDirectory.isDirectory) mDirectory.mkdirs()

        //will import example csv files (that are stored in the raw folder) into the import folder
        val importDir = File(mDirectory.path + "/Import")
        val exportDir = File(mDirectory.path + "/Export")
        if (!importDir.isDirectory) importDir.mkdirs()
        if (!exportDir.isDirectory) exportDir.mkdirs()
        val wishImport = File(importDir.path + "/Wishlist")
        if (!wishImport.isDirectory) wishImport.mkdirs()
        val parentImport = File(importDir.path + "/Parents")
        if (!parentImport.isDirectory) parentImport.mkdirs()
        val exampleWish = File(wishImport.path + "/example.csv")
        val exampleParents = File(parentImport.path + "/example.csv")
        if (!exampleWish.isFile) {
            val stream = resources.openRawResource(R.raw.wishlist_example)
            exampleWish.writeBytes(stream.readBytes())
            stream.close()
        }
        if (!exampleParents.isFile) {
            val stream = resources.openRawResource(R.raw.parents_example)
            exampleParents.writeBytes(stream.readBytes())
            stream.close()
        }
    }

    override fun onStart() {
        super.onStart()

        val db = IntercrossDatabase.getInstance(this)

        mEventsViewModel = ViewModelProviders.of(this@MainActivity,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventsListViewModel(
                                EventsRepository.getInstance(db.eventsDao())) as T
                    }
                }).get(EventsListViewModel::class.java)

        mWishListViewModel = ViewModelProviders.of(this@MainActivity,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WishlistViewModel(WishlistRepository.getInstance(db.wishlistDao())) as T

                    }
                }).get(WishlistViewModel::class.java)

        mParentsViewModel = ViewModelProviders.of(this@MainActivity,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ParentsViewModel(ParentsRepository.getInstance(db.parentsDao())) as T

                    }
                }).get(ParentsViewModel::class.java)

        mEventsViewModel.events.observe(this, Observer {
            it?.let {
                mEvents = it
            }
        })

        //change the hamburger toggle to a back button whenever the fragment is
        //not the main events fragment
        mNavController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.events_fragment -> {
                    mDrawerToggle.isDrawerIndicatorEnabled = true
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                }
                else -> {
                    mDrawerToggle.isDrawerIndicatorEnabled = false
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this@MainActivity,
                R.layout.activity_main)

        supportActionBar.apply {
            title = ""
            this?.let {
                it.themedContext
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }

        mDrawerLayout = mBinding.drawerLayout

        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                closeKeyboard()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                //update the person viewed under "Intercross" each time the drawer opens
                findViewById<NavigationView>(R.id.nvView).getHeaderView(0).apply {
                    findViewById<TextView>(R.id.navHeaderText)
                            .text = PreferenceManager
                            .getDefaultSharedPreferences(this@MainActivity)
                            .getString("org.phenoapps.intercross.PERSON", "")
                }
            }
        }

        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerLayout.addDrawerListener(mDrawerToggle)

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
                R.id.action_nav_pollen_manager -> {
                    mNavController.navigate(MainGraphDirections.globalActionToPollenManagerFragment())
                }
                R.id.action_nav_import -> {
                    mWishListViewModel.deleteAll()
                    val uri = Uri.parse("${mDirectory.path}/Import/Wishlist/")

                    val i = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT), "Choose wishlist to import.")
                    i.setDataAndType(uri, "*/*")
                    startActivityForResult(i, REQ_FILE_IMPORT)
                }
                R.id.action_nav_export -> {
                    val lineSeparator = System.getProperty("line.separator")

                    try {
                        val dir = File(mDirectory.path + "/Export/")
                        dir.mkdir()
                        val output = File(dir, "crosses_${DateUtil().getTime()}.csv")
                        val fstream = FileOutputStream(output)

                        fstream.write("eventDbId,eventName,eventValue,femaleObsUnitDbId,maleObsUnitDbId,person,timestamp,experiment".toByteArray())
                        fstream.write(lineSeparator.toByteArray())

                        mEvents.forEachIndexed { i, e ->
                            fstream.write(e.toString().toByteArray())
                            fstream.write(lineSeparator?.toByteArray())
                        }
                        scanFile(this@MainActivity, output)
                        fstream.flush()
                        fstream.close()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (io: IOException) {
                        io.printStackTrace()
                    } finally {
                        Snackbar.make(mBinding.root, "File write successful!", Snackbar.LENGTH_SHORT).show()
                    }
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

        mSnackbar = SnackbarQueue()

        mNavController = Navigation.findNavController(this@MainActivity, R.id.nav_fragment)

        //Show Tutorial Fragment for first-time users
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.apply {
            if (!getBoolean(SettingsFragment.TUTORIAL, false)) {
                startActivityForResult(Intent(this@MainActivity, IntroActivity::class.java), REQ_FIRST_OPEN)
            } else if (isExternalStorageWritable()) {
                setupDirs()
            }
        }

        pref.edit().apply {
            putBoolean(SettingsFragment.TUTORIAL, true)
            apply()
        }

    }

    fun scanFile(ctx: Context, filePath: File) {
        MediaScannerConnection.scanFile(ctx, arrayOf(filePath.absolutePath), null, null)
    }

    private fun closeKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
         menuInflater.inflate(R.menu.activity_main_toolbar, menu)
         return super.onCreateOptionsMenu(menu)
     }*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)

        closeKeyboard()

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            android.R.id.home -> {

                mNavController.currentDestination?.let {
                    when (it.id) {
                        R.id.events_fragment -> {
                            dl.openDrawer(GravityCompat.START)
                        }
                        //go back to the last fragment instead of opening the navigation drawer
                        else -> mNavController.popBackStack()
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onRequestPermissionsResult(resultCode: Int, permissions: Array<String>, granted: IntArray) {
        permissions.forEachIndexed { index, perm ->
            when {
                perm == Manifest.permission.WRITE_EXTERNAL_STORAGE
                        && granted[index] == PackageManager.PERMISSION_GRANTED -> {
                    setupDirs()
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
                            val row = it.split(",").map { it.trim() }
                            if (row.size == numCols) {
                                mParentsViewModel.addParents(row[0], row[2], "female", "")
                                mParentsViewModel.addParents(row[1], row[3], "male", "")
                                mWishListViewModel.addWishlist(
                                        row[0], row[1], row[2], row[3], row[4], row[5].toIntOrNull()
                                        ?: 0, row[6].toIntOrNull() ?: 0
                                )
                            }
                        }
                    }
                }
            }
        } else if (requestCode == REQ_FIRST_OPEN && resultCode == Activity.RESULT_OK) {
            if (isExternalStorageWritable()) {
                setupDirs()
            }
        }
    }

    override fun onBackPressed() {
        mNavController.currentDestination?.let {
            when (it.id) {
                R.id.pattern_fragment -> {

                    supportFragmentManager.primaryNavigationFragment?.let {
                        (it.childFragmentManager.fragments[0] as PatternFragment).onBackButtonPressed()
                        //(supportFragmentManager.primaryNavigationFragment as PatternFragment).onBackButtonPressed()
                    }
                }
                //go back to the last fragment instead of opening the navigation drawer
                else -> super.onBackPressed()
            }
        }
    }

    internal companion object {
        const val packageName = "org.phenoapps.intercross"

        //requests
        const val REQ_EXT_STORAGE = 101
        const val REQ_CAMERA = 102
        const val REQ_FILE_IMPORT = 103
        const val REQ_FIRST_OPEN = 104

    }

}
