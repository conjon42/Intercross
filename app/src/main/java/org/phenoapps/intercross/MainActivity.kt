package org.phenoapps.intercross

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.PollenGroupListViewModelFactory
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

    private val groupList: PollenGroupListViewModel by viewModels {
        PollenGroupListViewModelFactory(PollenGroupRepository.getInstance(mDatabase.pollenGroupDao()))
    }

    private val exportCrossesFile by lazy {

        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->

            //check if uri is null or maybe throws an exception

            FileUtil(this).exportCrossesToFile(uri, mEvents, mParents, mGroups)

        }

    }

    private val brapiAuth by lazy {

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {


            it?.let { result ->

                result.data

            }

        }
    }

    private fun authorizeBrApi() {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        pref.edit().putString("brapi.token", null).apply()

        try {

            val url = "https://test-server.brapi.org/brapi/v2/brapi/authorize?display_name=Intercross&return_url=intercross://"

            // Go to url with the default browser
            val uri: Uri = Uri.parse(url)

            val i = Intent(Intent.ACTION_VIEW, uri)

            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

            brapiAuth.launch(i)

        } catch (e: ApiException) {

            e.printStackTrace()

            Log.e("BrAPI", "Error starting BrAPI auth", e)

        } catch (e: ActivityNotFoundException) {

            e.printStackTrace()

            Log.e("BrAPI", "Error starting BrAPI activity auth.")
        }
    }

    /**
     * Ask the user to either drop table before import or append to the current table.
     *
     */
    private val importedFileContent by lazy {

        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            val tables = FileUtil(this).parseInputFile(uri)

            /**
             * Detect if there are changes in the wishlist/parent tables.
             * If there are, ask the user if they should be appended or start fresh.
             */

            if (tables.first.isNotEmpty()) {

                if (mParents.isEmpty()) {

                    parentsList.insert(*tables.first.toTypedArray())

                } else {

                    Dialogs.booleanOption(AlertDialog.Builder(this), getString(R.string.ask_user_to_append_or_drop_parents),
                            getString(R.string.start_fresh),
                            getString(R.string.cancel),
                            getString(R.string.append)) { erase ->

                        if (erase) {

                            parentsList.dropAndInsert(tables.first)

                        } else {

                            parentsList.insert(*tables.first.toTypedArray())

                        }
                    }
                }

            }

            if (tables.second.isNotEmpty()) {

                if (mWishlist.isEmpty()) {

                    wishModel.insert(*tables.second.toTypedArray())

                } else {

                    Dialogs.booleanOption(AlertDialog.Builder(this), getString(R.string.ask_user_to_append_or_drop_wishlist),
                            getString(R.string.start_fresh),
                            getString(R.string.cancel),
                            getString(R.string.append)) { erase ->

                        if (erase) {

                            wishModel.dropAndInsert(tables.second)

                        } else {

                            wishModel.insert(*tables.second.toTypedArray())
                        }
                    }
                }
            }
        }

    }

    private var wishlistEmpty = true

    private var mEvents: List<Event> = ArrayList()

    private var mGroups: List<PollenGroup> = ArrayList()

    private var mWishlist: List<Wishlist> = ArrayList()

    private var mParents: List<Parent> = ArrayList()

    private lateinit var mDatabase: IntercrossDatabase

    private lateinit var mSnackbar: SnackbarQueue

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mNavController: NavController


    /**
     * Function that creates example files for parents/zpl/wishlist tables in the app's cache directory.
     */
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

        setupNavDrawer()

        mSnackbar = SnackbarQueue()

        mNavController = Navigation.findNavController(this@MainActivity, R.id.nav_fragment)

        mDatabase = IntercrossDatabase.getInstance(this)

        startObservers()

        if ("demo" in BuildConfig.FLAVOR) {

            //authorizeBrApi()

        }
    }

    private fun startObservers() {

        eventsModel.events.observe(this, Observer {

            it?.let {

                mEvents = it

            }
        })

        parentsList.parents.observe(this, Observer {

            it?.let {

                mParents = it
            }
        })

        wishModel.wishlist.observe(this, Observer {

            it?.let {

                mWishlist = it

                wishlistEmpty = it.isEmpty()
            }
        })

        groupList.groups.observe(this, Observer {

            it?.let {

                mGroups = it

            }
        })

    }

    private fun setupNavDrawer() {

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

                    if (mParents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToParentsFragment())
                    else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                            getString(R.string.parents_table_empty))

                }
                R.id.action_nav_import -> {

                    val mimeType = "*/*"

                    importedFileContent.launch(mimeType)

                }
                R.id.action_nav_export -> {

                    val defaultFileNamePrefix = getString(R.string.default_crosses_export_file_name)

                    exportCrossesFile.launch("${defaultFileNamePrefix}_${DateUtil().getTime()}.csv")

                }
                R.id.action_nav_summary -> {

                    navigateToLastSummaryFragment()

                }
                R.id.action_nav_about -> {

                    mNavController.navigate(R.id.aboutActivity)

                }
            }

            mDrawerLayout.closeDrawers()

            true
        }
    }

    private fun navigateToLastSummaryFragment() {

        val lastSummaryFragment = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                .getString("last_visited_summary", "summary")

        /***
         * Prioritize navigation to summary fragment, otherwise pick the last chosen view using preferences
         * The key "last_visited_summary" is updated at the start of each respective fragment.
         */
        when (lastSummaryFragment) {

            "summary" -> {
                if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                else if(!wishlistEmpty) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                        getString(R.string.summary_and_wishlist_empty))
            }
            "crossblock" -> {
                if (!wishlistEmpty) mNavController.navigate(EventsFragmentDirections.actionToCrossblock())
                else if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                        getString(R.string.summary_and_wishlist_empty))
            }
            "wishlist" -> {
                if (!wishlistEmpty) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                else if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                        getString(R.string.summary_and_wishlist_empty))
            }
        }
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

    //TODO chaney replace with request permission contract
    override fun onRequestPermissionsResult(resultCode: Int, permissions: Array<String>, granted: IntArray) {
        super.onRequestPermissionsResult(resultCode, permissions, granted)

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
        const val REQ_CAMERA = 102

    }

}
