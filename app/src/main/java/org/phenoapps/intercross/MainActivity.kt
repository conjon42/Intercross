package org.phenoapps.intercross

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
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
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.*
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
import org.phenoapps.intercross.util.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

//    private val mFirebaseAnalytics by lazy {
//        FirebaseAnalytics.getInstance(this)
//    }

    private var doubleBackToExitPressedOnce = false

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

    /**
     * User selects a new uri document with CreateDocument(), default name is intercross.db
     * which can be changed where this is launched.
     */
    private val exportDatabase by lazy {

        registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->

            uri?.let { x ->

                FileUtil(this).exportDatabase(x)

            }
        }
    }

    /**
     * Used in main activity to import a user-chosen database.
     * User selects a uri from a GetContent() call which is passed to FileUtil to copy streams.
     * Finally, the app is recreated to use the new database.
     */
    private val importDatabase by lazy {

        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            uri?.let { x ->

                FileUtil(this).importDatabase(x)

                finish()

                startActivity(intent)
            }
        }
    }

    /**
     * Ask the user to either drop table before import or append to the current table.
     *
     */
    private val importedFileContent by lazy {

        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            //TODO documentation says uri can't be null, but it can...might want to check this for a bug
            uri?.let {

                val tables = FileUtil(this).parseInputFile(it)

                CoroutineScope(Dispatchers.IO).launch {

                    if (tables.size == 3) {

                        if (tables[0].isNotEmpty()) {

                            val crosses = tables[0].filterIsInstance(Event::class.java)

                            val polycrosses = crosses.filter { it.type == CrossType.POLY }

                            val nonPolys = crosses - polycrosses

                            polycrosses.forEach { poly ->

                                val maleGroup = poly.maleObsUnitDbId

                                if (maleGroup.isNotBlank()
                                        && "::" in maleGroup
                                        && "{" in maleGroup
                                        && "}" in maleGroup) {

                                    val tokens = maleGroup.split("::")

                                    val groupId = tokens[0]

                                    val groupName = tokens[1]

                                    var males = tokens[2]

                                    males = males.replace("{", "").replace("}", "")

                                    males.split(";").forEach {

                                        val pid = parentsList.insertForId(Parent(it, 1))

                                        groupList.insert(PollenGroup(groupId, groupName, pid))
                                    }

                                    eventsModel.insert(poly.apply {

                                        maleObsUnitDbId = groupId

                                    })
                                }

                            }

                            nonPolys.forEach { cross ->

                                parentsList.insert(Parent(cross.maleObsUnitDbId, 1), Parent(cross.femaleObsUnitDbId, 0))

                            }

                            eventsModel.insert(*nonPolys.toTypedArray())

                        }

                        if (tables[1].isNotEmpty()) {

                            parentsList.insert(*tables[1].filterIsInstance(Parent::class.java).toTypedArray())

                        }

                        if (tables[2].isNotEmpty()) {

                            wishModel.insert(*tables[2].filterIsInstance(Wishlist::class.java).toTypedArray())

                        }
                    }
                }
            }

        }
    }

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


    private fun writeStream(file: File, resourceId: Int) {

        if (!file.isFile) {

            val stream = resources.openRawResource(resourceId)

            file.writeBytes(stream.readBytes())

            stream.close()
        }

    }

    /**
     * Function that creates example files for parents/zpl/wishlist tables in the app's cache directory.
     */
    private fun setupDirs() {

        //create separate subdirectory foreach type of import
        val wishlists = File(this@MainActivity.externalCacheDir, "Wishlist")
        val parents = File(this@MainActivity.externalCacheDir, "Parents")
        val zpl = File(this@MainActivity.externalCacheDir, "ZPL")
        val crosses = File(this@MainActivity.externalCacheDir, "Crosses")

        crosses.mkdirs()
        wishlists.mkdirs()
        parents.mkdirs()
        zpl.mkdirs()

        //create empty files for the examples
        val exampleWish = File(wishlists, "/wishlist_example.csv")
        val exampleWishLarge = File(wishlists, "/large_wishlist.csv")
        val exampleParents = File(parents, "/parents_example.csv")
        val exampleZpl = File(zpl, "/zpl_example.zpl")
        val exampleCrosses = File(crosses, "/crosses_example.csv")

        //blocking code can be run with Dispatchers.IO
        CoroutineScope(Dispatchers.IO).launch {

            writeStream(exampleCrosses, R.raw.crosses_example)

            writeStream(exampleWish, R.raw.wishlist_example)

            writeStream(exampleParents, R.raw.parents_example)

            writeStream(exampleZpl, R.raw.example)

            if ("demo" in BuildConfig.BUILD_TYPE) {

                writeStream(exampleWishLarge, R.raw.large_wishlist)

            }
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

    }

    private fun startObservers() {

        eventsModel.events.observe(this, {

            it?.let {

                mEvents = it

            }
        })

        parentsList.parents.observe(this, {

            it?.let {

                mParents = it
            }
        })

        wishModel.wishlist.observe(this, {

            it?.let {

                mWishlist = it

            }
        })

        groupList.groups.observe(this, {

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

                    mNavController.navigate(EventsFragmentDirections.actionToParentsFragment())

                }
                R.id.action_nav_import -> {

                    val mimeType = "*/*"

                    with(AlertDialog.Builder(this@MainActivity)) {

                        setSingleChoiceItems(arrayOf("CSV", "Database"), 0) { dialog, which ->

                            when (which) {

                                0 -> importedFileContent.launch(mimeType)

                                1 -> importDatabase.launch(mimeType)

                            }

                            dialog.dismiss()
                        }

                        setTitle(R.string.export)

                        show()
                    }

                }
                R.id.action_nav_export -> {

                    val defaultFileNamePrefix = getString(R.string.default_crosses_export_file_name)

                    with(AlertDialog.Builder(this@MainActivity)) {

                        setSingleChoiceItems(arrayOf("CSV", "Database"), 0) { dialog, which ->

                            when (which) {

                                0 -> exportCrossesFile.launch("${defaultFileNamePrefix}_${DateUtil().getTime()}.csv")

                                1 -> exportDatabase.launch("intercross.db")

                            }

                            dialog.dismiss()
                        }

                        setTitle(R.string.export)

                        show()
                    }
                }
                R.id.action_nav_summary -> {

                    navigateToLastSummaryFragment()

                }
                R.id.action_nav_about -> {

                    mNavController.navigate(EventsFragmentDirections.actionToAbout())

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
                else if(mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                        getString(R.string.summary_and_wishlist_empty))
            }
            "crossblock" -> {
                if (mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToCrossblock())
                else if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToSummaryFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                        getString(R.string.summary_and_wishlist_empty))
            }
            "wishlist" -> {
                if (mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
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
                R.id.events_fragment -> {

                    if (doubleBackToExitPressedOnce) {

                        super.onBackPressed()

                        return
                    }

                    this.doubleBackToExitPressedOnce = true

                    Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

                    Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
                }
                else -> super.onBackPressed()
            }
        }
    }
}
