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
import android.provider.DocumentsContract
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.michaelflisar.changelog.ChangelogBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.databinding.ActivityMainBinding
import org.phenoapps.intercross.fragments.PatternFragment
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

//    private val mFirebaseAnalytics by lazy {
//        FirebaseAnalytics.getInstance(this)
//    }

    private lateinit var mSnackbar: SnackbarQueue

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mWishListViewModel: WishlistViewModel

    private lateinit var mEventsViewModel: EventsListViewModel
    private lateinit var mEvents: List<Events>

    private lateinit var mParentsViewModel: ParentsViewModel

    private lateinit var mPollenGroupViewModel: PollenGroupViewModel
    private lateinit var mGroups: List<PollenGroup>

    private lateinit var mPollenViewModel: PollenViewModel
    private lateinit var mPollens: List<Pollen>

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mNavController: NavController

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

        //create separate subdirectory foreach type of import
        val wishlists = File(this@MainActivity.externalCacheDir, "Wishlist")
        val parents = File(this@MainActivity.externalCacheDir, "Parents")
        val zpl = File(this@MainActivity.externalCacheDir, "ZPL")
        wishlists.mkdirs()
        parents.mkdirs()
        zpl.mkdirs()

        //create empty files for the examples
        val exampleWish = File(wishlists, "/wishlist_example.csv")
        val exampleParents = File(parents, "/parents_example.csv")
        val exampleZPL = File(zpl, "/zpl_example.zpl")

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
        if (!exampleZPL.isFile) {
            val stream = resources.openRawResource(R.raw.example)
            exampleZPL.writeBytes(stream.readBytes())
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

        mPollenGroupViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PollenGroupViewModel(PollenGroupRepository.getInstance(
                                db.pollenGroupDao())) as T

                    }
                }).get(PollenGroupViewModel::class.java)

        mPollenViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return PollenViewModel(PollenRepository.getInstance(
                                db.pollenDao())) as T

                    }
                }).get(PollenViewModel::class.java)

        mEventsViewModel.events.observe(this, Observer {
            it?.let {
                mEvents = it
            }
        })

        mPollenGroupViewModel.groups.observe(this, Observer {
            it?.let {
                mGroups = it
            }
        })

        mPollenViewModel.pollen.observe(this, Observer {
            it?.let {
                mPollens = it
            }
        })

        //change the hamburger toggle to a back button whenever the fragment is
        //not the main events fragment
        mNavController.addOnDestinationChangedListener { _, destination, _ ->
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

        //if (isExternalStorageWritable()) setupDirs()
        setupDirs()

//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN,
//                Bundle().apply {
//                    putString(FirebaseAnalytics.Param.TERM, "APP OPEN")
//                })

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
                R.id.action_cross_block_manager -> {
                    mNavController.navigate(MainGraphDirections.globalActionToCrossBlockManager())
                }
                R.id.action_nav_import -> {
                    mWishListViewModel.deleteAll()

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type="*/*"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                                FileProvider.getUriForFile(this@MainActivity,
                                "org.phenoapps.intercross.fileprovider",
                                File(File(this@MainActivity.externalCacheDir, "Wishlist"), "wishlist_example.csv")))


                    }

                    startActivityForResult(Intent.createChooser(intent, "Import Wishlist file."), REQ_FILE_IMPORT)

                }
                R.id.action_nav_export -> {

                    val filename = "crosses_${DateUtil().getTime()}.csv"

                    val input = EditText(this@MainActivity).apply {
                        inputType = InputType.TYPE_CLASS_TEXT
                        hint = "Exported file name"
                        setText(filename)
                    }

                    val builder = AlertDialog.Builder(this@MainActivity).apply {

                        setView(input)

                        setPositiveButton("OK") { _, _ ->
                            val value = input.text.toString()
                            if (value.isNotEmpty()) {
                                exportFile(value)
                            } else {
                                Snackbar.make(mBinding.root,
                                        "You must enter a new file name.", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        setTitle("Enter a new file name")
                    }
                    builder.show()
                }
                R.id.action_nav_summary -> {
                    mNavController.navigate(R.id.global_action_to_summary_fragment)
                }
                R.id.action_nav_wishlist_manager -> {
                    mNavController.navigate(R.id.global_action_to_wishlist_manager_fragment)
                }
                R.id.action_nav_about -> {
                    mNavController.navigate(R.id.aboutActivity)
                }
            }
            mDrawerLayout.closeDrawers()
            true
        }

        mSnackbar = SnackbarQueue()

        mNavController = Navigation.findNavController(this@MainActivity, R.id.nav_fragment)

    }

    private fun exportFile(filename: String) {
        val lineSeparator = System.getProperty("line.separator")

        try {
            var dir = this.getDir("Intercross/Export", Context.MODE_PRIVATE)
            dir.mkdir()
            val output = File(dir, filename)
            val fstream = FileOutputStream(output)

            fstream.write("eventDbId,eventName,eventValue,femaleObsUnitDbId,maleObsUnitDbId,person,timestamp,experiment".toByteArray())
            fstream.write(lineSeparator?.toByteArray() ?: "\n".toByteArray())

            mEvents.forEachIndexed { _, e ->
                mGroups.let {
                    for (g in it) {
                        if (e.maleOBsUnitDbId == g.uuid) {
                            val dads = ArrayList<String>()
                            for (p in mPollens) {
                                if (p.pid == g.id) {
                                    dads.add(p.pollenId)
                                }
                            }
                            e.maleOBsUnitDbId = dads.joinToString(";")
                        }
                    }
                    val groups = it.map { it.uuid }
                    if (e.maleOBsUnitDbId in groups) {
                        e.isPoly = true
                    }
                }
                if (e.eventName == "flower") {
                    fstream.write(e.toString().toByteArray())
                    fstream.write(lineSeparator?.toByteArray() ?: "\n".toByteArray())
                } else {
                    e.eventValue?.let {
                        fstream.write(e.toString().toByteArray())
                        fstream.write(lineSeparator?.toByteArray() ?: "\n".toByteArray())
                    }
                }

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
