package org.phenoapps.intercross.activities

//import org.phenoapps.intercross.BuildConfig
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.bytehamster.lib.preferencesearch.SearchPreferenceResult
import com.bytehamster.lib.preferencesearch.SearchPreferenceResultListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.phenoapps.intercross.BuildConfig
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.CrossType
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetaValuesViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetadataViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.PollenGroupListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.ActivityMainBinding
import org.phenoapps.intercross.fragments.EventsFragmentDirections
import org.phenoapps.intercross.fragments.PatternFragment
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.KeyUtil
import org.phenoapps.intercross.util.SnackbarQueue
import java.io.File
import android.text.InputType

class MainActivity : AppCompatActivity(), SearchPreferenceResultListener {

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

    private val metaValuesViewModel: MetaValuesViewModel by viewModels {
        MetaValuesViewModelFactory(MetaValuesRepository.getInstance(mDatabase.metaValuesDao()))
    }

    private val metadataViewModel: MetadataViewModel by viewModels {
        MetadataViewModelFactory(MetadataRepository.getInstance(mDatabase.metadataDao()))
    }

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }


    private val mKeyUtil by lazy {
        KeyUtil(this)
    }

    private val exportCrossesFile = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->

        //check if uri is null or maybe throws an exception

        uri?.let { nonNullUri ->

            try {

                FileUtil(this).exportCrossesToFile(nonNullUri, mEvents, mParents, mGroups, mMetadata, mMetaValues)

            } catch (e: Exception) {

                e.printStackTrace()

            }
        }
    }

    /**
     * User selects a new uri document with CreateDocument(), default name is intercross.db
     * which can be changed where this is launched.
     */
    val exportDatabase = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->

        uri?.let { x ->

            FileUtil(this).exportDatabase(x)

        }
    }

    /**
     * Used in main activity to import a user-chosen database.
     * User selects a uri from a GetContent() call which is passed to FileUtil to copy streams.
     * Finally, the app is recreated to use the new database.
     */
    val importDatabase = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        uri?.let { x ->

            FileUtil(this).importDatabase(x)

            finish()

            startActivity(intent)
        }
    }

    /**
     * Ask the user to either drop table before import or append to the current table.
     *
     */
    private val importedFileContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        //TODO documentation says uri can't be null, but it can...might want to check this for a bug
        uri?.let {

            try {

                importFromUri(it)

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(this, R.string.error_importing_file, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val checkPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted -> }

    private fun importFromUri(uri: Uri) {

        val tables = FileUtil(this).parseInputFile(uri)

        CoroutineScope(Dispatchers.IO).launch {

            if (tables.size == 5) {

                val crosses = tables[0].filterIsInstance<Event>()

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

                parentsList.insert(*tables[1].filterIsInstance<Parent>().toTypedArray())

            }

            if (tables[2].isNotEmpty()) {

                wishModel.insert(*tables[2].filterIsInstance<Wishlist>().toTypedArray())

            }

        }
    }

    private var mEvents: List<Event> = ArrayList()

    private var mGroups: List<PollenGroup> = ArrayList()

    private var mWishlist: List<Wishlist> = ArrayList()

    private var mParents: List<Parent> = ArrayList()

    private var mMetadata: List<Meta> = ArrayList()

    private var mMetaValues: List<MetadataValues> = ArrayList()

    private lateinit var mDatabase: IntercrossDatabase

    private lateinit var mSnackbar: SnackbarQueue

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

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Set up toolbar immediately after binding
        setSupportActionBar(mBinding.mainTb)
        supportActionBar?.apply {
            title = mPref.getString("current_experiment", "") ?: ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            show() // Show by default
        }

        setupDirs()

        mSnackbar = SnackbarQueue()

        mNavController = Navigation.findNavController(this, R.id.nav_fragment)

        mDatabase = IntercrossDatabase.getInstance(this)

        startObservers()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermissions.launch(arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.INTERNET
            ))
        } else {
            checkPermissions.launch(arrayOf(
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.INTERNET
            ))
        }

        mBinding.mainTb.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setBackButtonToolbar(show: Boolean = true) {
        if (!::mBinding.isInitialized) return
        
        setSupportActionBar(mBinding.mainTb) // Re-set the toolbar
        supportActionBar?.apply {
            title = mPref.getString("current_experiment", "") ?: ""
            setDisplayHomeAsUpEnabled(show)
            setDisplayShowHomeEnabled(show)
            if (show) show() else hide()
        }
    }

    private fun startObservers() {

        eventsModel.events.observe(this) {

            it?.let {

                mEvents = it

            }
        }

        parentsList.parents.observe(this) {

            it?.let {

                mParents = it
            }
        }

        wishModel.wishlist.observe(this) {

            it?.let {

                mWishlist = it.filter { it.wishType == "cross" }

            }
        }

        groupList.groups.observe(this) {

            it?.let {

                mGroups = it

            }
        }

        metadataViewModel.metadata.observe(this) {

            mMetadata = it
        }

        metaValuesViewModel.metaValues.observe(this) {

            mMetaValues = it
        }
    }

    private fun showExportDialog() {

        val defaultFileNamePrefix = getString(R.string.default_crosses_export_file_name)

        with(AlertDialog.Builder(this@MainActivity)) {

            setSingleChoiceItems(arrayOf("CSV", "Database"), 0) { dialog, which ->

                when (which) {

                    0 -> exportCrossesFile?.launch("${defaultFileNamePrefix}_${DateUtil().getTime()}.csv")

                    1 -> exportDatabase?.launch("intercross.zip")

                }

                dialog.dismiss()
            }

            setTitle(R.string.export)

            show()
        }
    }

    fun launchImport() {

        //if (mAuthPref.getString(mKeyUtil.brapiKeys.brapiTokenKey, null) != null) {
        //show a dialog asking user to import from local file or brapi
        //TODO
//            AlertDialog.Builder(this)
//                .setSingleChoiceItems(arrayOf("Local", "BrAPI"), 0) { dialog, which ->
//                    when (which) {
//                        //import file from local directory
//                        0 -> importedFileContent?.launch("*/*")
//
//                        //start brapi import fragment
//                        1 -> mNavController.navigate(CrossCountFragmentDirections.globalActionToWishlistImport())
//
//                    }
//
//                    dialog.dismiss()
//                }
//                .show()
        //} else {
        importedFileContent.launch("*/*")
        //}
    }

    fun showExportDialog(onDismiss: () -> Unit) {

        val importCheck = mPref.getString(mKeyUtil.brapiHasBeenImported, null)
        val defaultFileNamePrefix = getString(R.string.default_crosses_export_file_name)

        if (importCheck != null) { //(tokenCheck != null || importCheck != null) {

            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_export_title)
                .setSingleChoiceItems(arrayOf("Local", "BrAPI"), 0) { dialog, which ->
                    when (which) {
                        0 -> {
                            exportCrossesFile?.launch("${defaultFileNamePrefix}_${DateUtil().getTime()}.csv")
                        }
                        else -> {
                            mNavController.navigate(R.id.global_action_to_brapi_export)
                        }
                    }

                    dialog.dismiss()
                }
                .setOnDismissListener {
                    onDismiss()
                }
                .show()

            onDismiss()

        } else {
            exportCrossesFile?.launch("${defaultFileNamePrefix}_${DateUtil().getTime()}.csv")
        }
    }

    fun navigateToLastSummaryFragment() {

        val lastSummaryFragment = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            .getString("last_visited_summary", "summary")

        /***
         * Prioritize navigation to summary fragment, otherwise pick the last chosen view using preferences
         * The key "last_visited_summary" is updated at the start of each respective fragment.
         */
        when (lastSummaryFragment) {

            "summary" -> {
                if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToCrossCountFragment())
                else if(mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                    getString(R.string.summary_and_wishlist_empty))
            }
            "crossblock" -> {
                if (mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToCrossblock())
                else if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToCrossCountFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                    getString(R.string.summary_and_wishlist_empty))
            }
            "wishlist" -> {
                if (mWishlist.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToWishlistFragment())
                else if (mEvents.isNotEmpty()) mNavController.navigate(EventsFragmentDirections.actionToCrossCountFragment())
                else Dialogs.notify(AlertDialog.Builder(this@MainActivity),
                    getString(R.string.summary_and_wishlist_empty))
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_experiment -> {
                showExperimentDialog()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    private fun showExperimentDialog() {
        val currentExperiment = mPref.getString("current_experiment", "") ?: ""
        
        val input = EditText(this).apply {
            setText(currentExperiment)
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine()
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.current_experiment)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val experimentName = input.text.toString()
                mPref.edit().putString("current_experiment", experimentName).apply()
                supportActionBar?.title = experimentName
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onSearchResultClicked(result: SearchPreferenceResult) {

        result.closeSearchPage(this)

        mNavController.navigate(
            when (result.key) {
                in mKeyUtil.profileKeySet -> R.id.profile_preference_fragment
                in mKeyUtil.nameKeySet -> R.id.naming_preference_fragment
                in mKeyUtil.workKeySet -> R.id.workflow_preference_fragment
                in mKeyUtil.printKeySet -> R.id.printing_preference_fragment
                in mKeyUtil.dbKeySet -> R.id.database_preference_fragment
                in mKeyUtil.aboutKeySet -> R.id.about_preference_fragment
                else -> throw RuntimeException() //todo R.id.brapi_preference_fragment
            }
        )
    }
}