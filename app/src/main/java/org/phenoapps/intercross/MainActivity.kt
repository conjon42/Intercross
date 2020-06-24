package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.ActivityMainBinding
import org.phenoapps.intercross.fragments.EventsFragmentDirections
import org.phenoapps.intercross.fragments.PatternFragment
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import java.io.File

class MainActivity : AppCompatActivity() {

//    private val mFirebaseAnalytics by lazy {
//        FirebaseAnalytics.getInstance(this)
//    }

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(mDatabase.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(mDatabase.wishlistDao()))
    }

    private val parentsList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(mDatabase.parentsDao()))
    }

    private var parentsEmpty = true

    private var wishlistEmpty = true

    private var eventsEmpty = true

    private lateinit var mDatabase: IntercrossDatabase

    private lateinit var mParentsStore: ParentsListViewModel

    private lateinit var mSnackbar: SnackbarQueue

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mNavController: NavController


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

        setupDirs()

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


        //TODO name not showing up until app re-open
        mDrawerToggle = object : ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                closeKeyboard()
            }
        }

        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerLayout.addDrawerListener(mDrawerToggle)

        // Setup drawer view
        val nvDrawer = findViewById<NavigationView>(R.id.nvView)

        nvDrawer.getHeaderView(0).apply {
            findViewById<TextView>(R.id.navHeaderText)
                    .text = PreferenceManager
                    .getDefaultSharedPreferences(this@MainActivity)
                    .getString("org.phenoapps.intercross.PERSON", "")
        }

        nvDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.action_nav_settings -> {

                    mNavController.navigate(R.id.global_action_to_settings_fragment)
                }
                R.id.action_nav_parents -> {

                    if (!parentsEmpty)
                        mNavController.navigate(EventsFragmentDirections.actionToParentsFragment())
                    else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                            getString(R.string.parents_table_empty))
                }
                R.id.action_nav_import -> {

                    importWishlist()
                }
                R.id.action_nav_export -> {

                    exportFile()
                }
                R.id.action_nav_summary -> {

                    val lastSummaryFragment = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                            .getString("last_visited_summary", "summary")

                    /***
                     * Prioritize navigation to summary fragment, otherwise pick the last chosen view using preferences
                     * The key "last_visited_summary" is updated at the start of each respective fragment.
                     */
                    when (lastSummaryFragment) {

                        "summary" -> {
                            if (!eventsEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                            else if(!wishlistEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                            else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                                    getString(R.string.summary_and_wishlist_empty))
                        }
                        "crossblock" -> {
                            if (!wishlistEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToCrossblock())
                            else if (!eventsEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                            else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                                    getString(R.string.summary_and_wishlist_empty))
                        }
                        "wishlist" -> {
                            if (!wishlistEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                            else if (!eventsEmpty)
                                mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                            else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                                    getString(R.string.summary_and_wishlist_empty))
                        }
                    }
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

        mDatabase = IntercrossDatabase.getInstance(this)

        mParentsStore = ParentsListViewModel(ParentsRepository.getInstance(mDatabase.parentsDao()))

        eventsModel.events.observe(this, Observer {

            it?.let {

                eventsEmpty = it.isEmpty()
            }
        })

        parentsList.parents.observe(this, Observer {

            it?.let {

                parentsEmpty = it.isEmpty()
            }
        })

        wishModel.wishlist.observe(this, Observer {

            it?.let {

                wishlistEmpty = it.isEmpty()
            }
        })
    }

    private fun importWishlist() {

        wishModel.deleteAll()

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type="*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        putExtra(DocumentsContract.EXTRA_INITIAL_URI,
//                                FileProvider.getUriForFile(this@MainActivity,
//                                "org.phenoapps.intercross.fileprovider",
//                                File(File(this@MainActivity.externalCacheDir, "Wishlist"), "wishlist_example.csv")))


        }

        startActivityForResult(Intent.createChooser(intent, "Import Wishlist file."), REQ_FILE_IMPORT)

    }

    private fun exportFile() {

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

    private fun exportFile(filename: String) {

//        val lineSeparator = System.getProperty("line.separator")
//
//            mLiveEvents.forEachIndexed { _, e ->
//
//                try {
//                    val dir = this.getDir("Intercross/Export", Context.MODE_PRIVATE)
//                    dir.mkdir()
//                    val output = File(dir, filename)
//                    val fstream = FileOutputStream(output)
//
//                    //TODO update headers
//                    fstream.write("eventDbId,femaleObsUnitDbId,maleObsUnitDbId,crossType,person,timestamp,experiment,flowers,seeds,fruits".toByteArray())
//                    fstream.write(lineSeparator?.toByteArray() ?: "\n".toByteArray())
//
//                    if (!e.unknown) {
////                                mGroups.let { it ->
////                                    for (g in it) {
////                                        if (e.maleObsUnitDbId == g.uuid) {
////                                            val dads = ArrayList<String>()
////                                            for (p in mPollens) {
////                                                if (p.pid == g.id) {
////                                                    dads.add(p.pollenId)
////                                                }
////                                            }
////                                            e.maleObsUnitDbId = dads.joinToString(";")
////                                        }
////                                    }
////                                    val groups = it.map { it.uuid }
////                                    if (e.maleObsUnitDbId in groups) {
////                                        e.crossType = "Polycross"
////                                    }
////                                }
//
//                        fstream.write(e.toString().toByteArray())
//                        fstream.write(lineSeparator?.toByteArray() ?: "\n".toByteArray())
//                    }
//                    scanFile(this@MainActivity, output)
//                    fstream.flush()
//                    fstream.close()
//                } catch (e: FileNotFoundException) {
//                    e.printStackTrace()
//                } catch (io: IOException) {
//                    io.printStackTrace()
//                } finally {
//                    Snackbar.make(mBinding.root, "File write successful!", Snackbar.LENGTH_SHORT).show()
//                }
//            }
    }


    private fun scanFile(ctx: Context, filePath: File) {

        MediaScannerConnection.scanFile(ctx,
                arrayOf(filePath.absolutePath),
                null, null)
    }

    private fun closeKeyboard() {

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }


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

            i?.data?.let { it ->

                val columns = FileUtil(this).parseInputFile(it)

                columns["Parents"]?.let { insertableParents ->

                    mParentsStore.insert(
                            *(insertableParents as ArrayList<Parent>)
                                    .toTypedArray())
                }


                columns["Wishlist"]?.let {

                    wishModel.addWishlist(
                            *(it as ArrayList<Wishlist>)
                                    .toTypedArray()
                    )

                }
            }
        }
    }

    override fun onBackPressed() {

        mNavController.currentDestination?.let { it ->
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

        //requests
        const val REQ_EXT_STORAGE = 101
        const val REQ_CAMERA = 102
        const val REQ_FILE_IMPORT = 103
        const val REQ_FIRST_OPEN = 104

    }

}
