package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import org.phenoapps.intercross.IntercrossConstants.REQUEST_WRITE_PERMISSION
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList

class MainActivity : AppCompatActivity(), LifecycleObserver {

    enum class PollinationType {
        Biparental,
        SelfPollinated,
        OpenPollinated
    }

    private lateinit var mFirstEditText: EditText
    private lateinit var mSecondEditText: EditText
    private lateinit var mCrossEditText: EditText
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mNavView: NavigationView
    private lateinit var mSaveButton: Button

    private var mAllowBlankMale: Boolean = false

    private var mCrossOrder: Int = 0

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            val type = mDbHelper.getPollinationType(obj.id)
            return when(type) {
                PollinationType.SelfPollinated.toString() -> R.layout.main_self_pollinated_row
                PollinationType.OpenPollinated.toString() -> R.layout.main_open_pollinated_row
                else -> R.layout.main_biparental_row
            }
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

    }

    private val mPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->
        key?.let{
            when (key) {
                SettingsActivity.PATTERN -> {
                    when (pref?.getBoolean(key, true)) {
                        true -> mCrossEditText.isEnabled = false
                        false -> mCrossEditText.isEnabled = true
                    }
                }
                SettingsActivity.CROSS_ORDER -> {
                    when (pref?.getString(key, "0")) {
                        "0" -> {
                            mFirstEditText.hint = "Female ID: "
                            mSecondEditText.hint = "Male ID: "
                        }
                        "1" -> {
                            mFirstEditText.hint = "Male ID: "
                            mSecondEditText.hint = "Female ID: "
                        }
                    }
                }
                SettingsActivity.BLANK_MALE_ID -> {
                    mAllowBlankMale = !mAllowBlankMale
                    findViewById<Button>(R.id.saveButton).isEnabled = isInputValid()
                }
            }
        }
    }

    private val mDbHelper: IdEntryDbHelper = IdEntryDbHelper(this)

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var mNameMap: MutableMap<String, String>

    private val isExternalStorageWritable: Boolean
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Security", "Permission is granted")
                    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
                }
            } else
                return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

            return false
        }

    override fun onStart() {

        super.onStart()

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView) as NavigationView

        // Setup drawer view
        setupDrawerContent(mNavView)
        setupDrawer()

        mSecondEditText = findViewById(R.id.secondText)
        mFirstEditText = findViewById(R.id.firstText)
        mCrossEditText = findViewById(R.id.editTextCross)
        mSaveButton = findViewById(R.id.saveButton)

        //Show Tutorial Fragment for first-time users
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            if (!getBoolean(IntercrossConstants.COMPLETED_TUTORIAL, false)) {
                startActivity(Intent(this@MainActivity, IntroActivity::class.java))
            }
            if (getBoolean(SettingsActivity.PATTERN, false)) {
                mCrossEditText.isEnabled = false
            }
            mAllowBlankMale = getBoolean(SettingsActivity.BLANK_MALE_ID, false)
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
            putBoolean(IntercrossConstants.COMPLETED_TUTORIAL, true)
            apply()
        }

        //single text watcher class to check if all fields are non-empty to enable the save button
        val emptyGuard = object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mSaveButton.isEnabled = isInputValid()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        }

        mSecondEditText.addTextChangedListener(emptyGuard)
        mFirstEditText.addTextChangedListener(emptyGuard)
        mCrossEditText.addTextChangedListener(emptyGuard)

        val focusListener = object : View.OnFocusChangeListener {

            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1 && PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                ?.getString(SettingsActivity.PERSON, "")?.isNotBlank() == false) {
                    askUserForPerson()
                }
            }

        }

        mFirstEditText.onFocusChangeListener = focusListener
        mSecondEditText.onFocusChangeListener = focusListener
        mCrossEditText.onFocusChangeListener = focusListener

        mFirstEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                mSecondEditText.requestFocus()
                return@OnEditorActionListener true
            }
            false
        })

        //if auto generation is enabled save after the second text is submitted
        mSecondEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                        ?.getBoolean(SettingsActivity.PATTERN, false) == false) {
                    mCrossEditText.requestFocus()
                } else {
                    saveToDB()
                }
                return@OnEditorActionListener true
            }
            false
        })

        mCrossEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                saveToDB()
                return@OnEditorActionListener true
            }
            false
        })

        mSaveButton.isEnabled = isInputValid()

        mSaveButton.setOnClickListener {
            saveToDB()
        }

        (findViewById<Button>(R.id.clearButton)).setOnClickListener {
            mCrossEditText.setText("")
            mFirstEditText.setText("")
            mSecondEditText.setText("")
            mFirstEditText.requestFocus()

        }

        when (mCrossOrder) {
            0 -> {
                mFirstEditText.hint = "Female ID:"
                mSecondEditText.hint = "Male ID:"
            }
            1 -> {
                mFirstEditText.hint = "Male ID:"
                mSecondEditText.hint = "Female ID:"
            }
        }

        findViewById<ConstraintLayout>(R.id.constraint_layout_parent).requestFocus()

        loadSQLToLocal()
    }

    private fun isInputValid(): Boolean {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val auto = pref.getBoolean(SettingsActivity.PATTERN, false)

        val male: String
        val female: String
        val cross: String = mCrossEditText.text.toString()
        if (mCrossOrder == 0) {
            female = mFirstEditText.text.toString()
            male = mSecondEditText.text.toString()
        } else {
            male = mFirstEditText.text.toString()
            female = mSecondEditText.text.toString()
        }

        return ((male.isNotEmpty() || mAllowBlankMale) && female.isNotEmpty()
                && (cross.isNotEmpty() || auto))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mNameMap = HashMap()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        mCrossOrder = sharedPref?.getString(SettingsActivity.CROSS_ORDER, "0")?.toInt() ?: 0

        sharedPref.registerOnSharedPreferenceChangeListener(mPrefListener)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    }

    private fun saveToDB() {

        val male: String
        val female: String
        var cross: String = mCrossEditText.text.toString()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val isAutoPattern = pref.getBoolean(SettingsActivity.PATTERN, false)

        if (isAutoPattern) {
            val auto = pref.getBoolean("PATTERN_AUTO", true)
            val prefix = pref.getString("PATTERN_PREFIX", "")
            val suffix = pref.getString("PATTERN_SUFFIX", "")
            val num = pref.getInt("PATTERN_INT", 0)
            val pad = pref.getInt("PATTERN_PAD", 0)
            cross = "$prefix${num.toString().padStart(pad, '0')}$suffix"
            val edit = pref.edit()
            edit.putInt("PATTERN_INT", num + 1)
            edit.apply()
        }


        if (mCrossOrder == 0) {
            female = mFirstEditText.text.toString()
            male = mSecondEditText.text.toString()
        } else {
            male = mFirstEditText.text.toString()
            female = mSecondEditText.text.toString()
        }

        if ((male.isNotEmpty() || mAllowBlankMale) && female.isNotEmpty() && cross.isNotEmpty()) {

            val pollinationType = when {
                male.isBlank() -> PollinationType.OpenPollinated
                male != female -> PollinationType.Biparental
                else -> PollinationType.SelfPollinated
            }

            val crossCount = mDbHelper.getCrosses(female, male).size + 1

            val entry = ContentValues()

            entry.put("male", if(male.isBlank()) "blank" else male)
            entry.put("female", female)
            entry.put("cross_id", cross)
            entry.put("cross_type", pollinationType.toString())
            entry.put("cross_count", crossCount)
            entry.put("cross_name", "$female/$male-$crossCount")

            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val person : String = pref?.getString(SettingsActivity.PERSON, "None") ?: "None"

            entry.put("timestamp", sdf.format(c.time).toString())

            entry.put("person", person)

            mDbHelper.insertEntry(entry)

            //clear fields
            mFirstEditText.text.clear()
            mSecondEditText.text.clear()
            mCrossEditText.text.clear()

            mFirstEditText.requestFocus()

            loadSQLToLocal()
        }
    }

    //reads the SQLite database and displays cross ids and their total counts / timestamps
    private fun loadSQLToLocal() {

        mEntries.clear()

        mDbHelper.getMainPageEntries().forEach { entry ->
            mEntries.add(entry)
        }

        mAdapter.notifyDataSetChanged()
    }

    private fun askIfSamePerson() {

        findViewById<ConstraintLayout>(R.id.constraint_layout_parent).requestFocus()


        val builder = AlertDialog.Builder(this).apply {

            setNegativeButton("Yes") { _, _ ->
                //welcome back
            }

            setPositiveButton("Change Person") { _, _ ->
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        builder.setTitle("Is this still " +
                "${PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                        .getString(SettingsActivity.PERSON, "Guillaume")}?")
        builder.show()
    }

    private fun askUserExportFileName() {

        if (isExternalStorageWritable) {

            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT
            }

            val builder = AlertDialog.Builder(this).apply {

                setView(input)

                setPositiveButton("Export") { _, _ ->
                    val value = input.text.toString()
                    if (value.isNotEmpty()) {
                        try {
                            val dir = File(Environment.getExternalStorageDirectory().path + "/Intercross")
                            dir.mkdir()
                            val output = File(dir, "$value.csv")
                            val fstream = FileOutputStream(output)

                            val colVals = mDbHelper.getExportData()

                            colVals.forEach {
                                fstream.write(it.toByteArray())
                                fstream.write(lineSeparator.toByteArray())
                            }

                            scanFile(this@MainActivity, output)

                            fstream.flush()
                            fstream.close()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (io: IOException) {
                            io.printStackTrace()
                        } finally {
                            Toast.makeText(this@MainActivity, "File write successful!", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@MainActivity,
                                "You must enter a file name.", Toast.LENGTH_SHORT).show()
                    }
                    askUserDeleteEntries()
                }
            }

            builder.setTitle("Choose a name for the exported file.")
            builder.show()
        }
    }

    private fun askUserForPerson() {

        findViewById<ConstraintLayout>(R.id.constraint_layout_parent).requestFocus()

        val builder = AlertDialog.Builder(this).apply {

            setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this@MainActivity,
                        "Person must be set before crosses can be made.", Toast.LENGTH_SHORT).show()
            }

            setPositiveButton("Set Person") { _, _ ->
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        builder.setTitle("Person must be set before crosses can be made.")
        builder.show()
    }

    private fun askUserDeleteEntries() {

        val builder = AlertDialog.Builder(this).apply {

            setNegativeButton("Cancel") { _, _ ->

            }

            setPositiveButton("Yes") { _, _ ->
                val builder = AlertDialog.Builder(this@MainActivity).apply {

                    setNegativeButton("Cancel") { _, _ ->

                    }

                    setPositiveButton("Yes") { _, _ ->
                        mEntries.clear()
                        mDbHelper.onUpgrade(mDbHelper.readableDatabase, 0, 0)
                        mAdapter.notifyDataSetChanged()
                    }
                }

                builder.setTitle("Are you sure you want to delete all entries?")
                builder.show()
            }
        }

        builder.setTitle("Delete all cross entries?")
        builder.show()
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(org.phenoapps.intercross.R.menu.activity_main_toolbar, m)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val dl = findViewById(R.id.drawer_layout) as DrawerLayout
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            android.R.id.home -> dl.openDrawer(GravityCompat.START)
            R.id.action_camera -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val cameraIntent = Intent(this, ScanActivity::class.java)
                    startActivityForResult(cameraIntent, IntercrossConstants.CAMERA_INTENT_REQ)
                } else {
                    Toast.makeText(this,
                            "You must accept camera permissions before using the barcode reader.",
                            Toast.LENGTH_LONG).show()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), IntercrossConstants.PERM_REQ)
                }
            } else { //permission granted on installation
                val cameraIntent = Intent(this, ScanActivity::class.java)
                startActivityForResult(cameraIntent, IntercrossConstants.CAMERA_INTENT_REQ)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {

            intent?.let {

                when (requestCode) {
                    IntercrossConstants.MANAGE_HEADERS_REQ -> {
                        mDbHelper.updateColumns(intent.extras?.getStringArrayList(IntercrossConstants.HEADERS)
                                ?: ArrayList())
                    }
                    IntercrossConstants.USER_INPUT_HEADERS_REQ -> {
                        mDbHelper.updateValues(intent.extras?.getInt(IntercrossConstants.COL_ID_KEY).toString(),
                                intent.extras.getStringArrayList(IntercrossConstants.USER_INPUT_VALUES)
                        )
                    }
                    IntercrossConstants.PATTERN_REQ -> {
                        if (intent.hasExtra(IntercrossConstants.PATTERN)) {
                            val pattern = intent.extras
                                    .getParcelable<AutoGenerationActivity.LabelPattern>(IntercrossConstants.PATTERN)
                            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                            editor.putString("PATTERN_PREFIX", pattern.prefix)
                            editor.putString("PATTERN_SUFFIX", pattern.suffix)
                            editor.putInt("PATTERN_INT", pattern.number)
                            editor.putBoolean("PATTERN_AUTO", pattern.auto)
                            editor.putInt("PATTERN_PAD", pattern.pad)
                            editor.apply()
                        }
                    }
                }

                //barcode text response from Zebra intent
                if (intent.hasExtra(IntercrossConstants.CAMERA_RETURN_ID)) {

                    asList(mFirstEditText, mSecondEditText, mCrossEditText).forEach iter@{ editText ->
                        editText?.let {
                            when (editText.hasFocus()) {
                                true -> {
                                    editText.setText(intent.getStringExtra(IntercrossConstants.CAMERA_RETURN_ID))
                                    when (editText) {
                                        mFirstEditText -> mSecondEditText.requestFocus()
                                        mSecondEditText -> mCrossEditText.requestFocus()
                                        mCrossEditText -> {
                                            saveToDB()
                                        }
                                        else -> Log.d("Focus", "Unexpected request focus.")
                                    }
                                    return
                                }
                                false -> return@iter
                            }
                        }
                    }
                }
            }
        }
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry>, View.OnClickListener {

        private var id: Int = -1
        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.dateTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: AdapterEntry) {

            id = data.id
            firstText.text = data.first
            secondText.text = data.second

            itemView.findViewById<ImageView>(R.id.deleteView).setOnClickListener { _ ->
                mDbHelper.deleteEntry(id)
                loadSQLToLocal()
            }

            itemView.findViewById<ImageView>(R.id.inputImageView).setOnClickListener {
                (currentFocus as? EditText).apply {
                    this?.setText(firstText.text.toString())
                }
            }
        }

        fun startCrossActivity() {

            val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)

            val parents = mDbHelper.getParents(id)

            val intent = Intent(this@MainActivity, AuxValueInputActivity::class.java)

            intent.putExtra(IntercrossConstants.COL_ID_KEY, id)

            intent.putExtra(IntercrossConstants.CROSS_ID, firstText.text)

            intent.putExtra(IntercrossConstants.FEMALE_PARENT, parents[0])

            intent.putExtra(IntercrossConstants.MALE_PARENT, parents[1])

            startActivityForResult(intent, IntercrossConstants.USER_INPUT_HEADERS_REQ)
        }

        override fun onClick(v: View?) {

            v?.let {

                val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val btId = pref.getString(SettingsActivity.BT_ID, "")
                if (btId.isBlank()) {

                    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                    val pairedDevices = mBluetoothAdapter.bondedDevices

                    val input = RadioGroup(this@MainActivity)

                    pairedDevices.forEach {
                        val button = RadioButton(this@MainActivity)
                        button.text = it.name
                        input.addView(button)
                    }

                    val builder = AlertDialog.Builder(this@MainActivity).apply {

                        setTitle("Choose bluetooth device to print from.")

                        setView(input)

                        setNegativeButton("Cancel") { _, _ ->

                        }

                        setPositiveButton("OK") { _, _ ->

                            if (input.checkedRadioButtonId == -1) return@setPositiveButton

                            val edit = pref.edit()
                            edit.putString(SettingsActivity.BT_ID,
                                    input.findViewById<RadioButton>(input.checkedRadioButtonId).text.toString())
                            edit.apply()

                            startCrossActivity()
                        }
                    }

                    builder.show()
                } else startCrossActivity()
            }
        }
    }

    private fun setupDrawer() {

        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        mDrawerToggle = object : ActionBarDrawerToggle(this, dl,
                org.phenoapps.intercross.R.string.drawer_open, org.phenoapps.intercross.R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                val view = this@MainActivity.currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            override fun onDrawerClosed(view: View) {}

        }

        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        dl.addDrawerListener(mDrawerToggle!!)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        when (menuItem.itemId) {

            org.phenoapps.intercross.R.id.nav_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(settingsIntent, IntercrossConstants.SETTINGS_INTENT_REQ)
            }
            org.phenoapps.intercross.R.id.nav_export -> askUserExportFileName()
            org.phenoapps.intercross.R.id.nav_about -> showAboutDialog()
            org.phenoapps.intercross.R.id.cross_count -> {
                val countIntent = Intent(this, CountActivity::class.java)
                countIntent.putExtra("NameMapKey", mNameMap!!.keys.toTypedArray<String>())
                countIntent.putExtra("NameMapValue", mNameMap!!.values.toTypedArray<String>())
                startActivity(countIntent)
            }
            org.phenoapps.intercross.R.id.nav_intro -> {
                //startActivity(Intent(this, IntercrossOnboardingActivity::class.java))
                startActivity(Intent(this, IntroActivity::class.java))
            }
            org.phenoapps.intercross.R.id.nav_delete_entries -> {
                askUserDeleteEntries()
            }
            org.phenoapps.intercross.R.id.nav_auto_generate -> {
                startActivityForResult(Intent(this, AutoGenerationActivity::class.java),
                        IntercrossConstants.PATTERN_REQ)
            }
            org.phenoapps.intercross.R.id.nav_import_zpl -> {
                startActivity(Intent(this, ImportZPL::class.java))
            }
            //org.phenoapps.intercross.R.id.nav_manage_headers -> {
            //    startActivityForResult(Intent(this@MainActivity,
            //            ManageHeadersActivity::class.java), IntercrossConstants.MANAGE_HEADERS_REQ)
            //}
        }

        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        dl.closeDrawers()
    }

    override fun onBackPressed() {
        //do nothing
    }

    private fun showAboutDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        run {
            val personView = this.layoutInflater.inflate(
                    org.phenoapps.intercross.R.layout.about, android.widget.LinearLayout(this),
                    false)

            run {
                assert(personView != null)
                val versionTextView = personView!!.findViewById(org.phenoapps.intercross.R.id.tvVersion) as android.widget.TextView
                try {
                    val packageInfo = this.packageManager.getPackageInfo(this.packageName, 0)
                    assert(packageInfo != null)
                    assert(versionTextView != null)
                    versionTextView.text = this.resources.getString(
                            org.phenoapps.intercross.R.string.versiontitle) +
                            ' '.toString() + packageInfo!!.versionName
                } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                versionTextView.setOnClickListener { this@MainActivity.showChangeLog() }
            }

            builder.setCancelable(true)
            builder.setTitle(this.resources.getString(
                    org.phenoapps.intercross.R.string.about))
            builder.setView(personView)
        }

        builder.setNegativeButton(
                this.resources.getString(org.phenoapps.intercross.R.string.ok)
        ) { dialog, which ->
            assert(dialog != null)
            dialog!!.dismiss()
        }

        builder.show()
    }

    private fun showChangeLog() {

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle?.onConfigurationChanged(newConfig)
    }

    public override fun onDestroy() {
        mDbHelper.close()
        super.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if (PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                        .getString(SettingsActivity.PERSON, "").isNotBlank())
            askIfSamePerson()
    }

    override fun onRequestPermissionsResult(resultCode: Int, permissions: Array<String>, granted: IntArray) {

        if (resultCode == IntercrossConstants.PERM_REQ) {
            for (i in permissions.indices) {
                if (granted[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("Security", permissions[i] + " : " + granted[i])
                }
            }
        }
    }

    companion object {

        private val lineSeparator = System.getProperty("line.separator")

        fun scanFile(ctx: Context, filePath: File) {
            MediaScannerConnection.scanFile(ctx, arrayOf(filePath.absolutePath), null, null)
        }
    }
}
