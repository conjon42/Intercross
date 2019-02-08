package org.phenoapps.intercross

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
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
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList

internal class IntercrossActivity : AppCompatActivity(), LifecycleObserver {

    private val mFirstEditText: EditText by lazy {
        findViewById<EditText>(R.id.firstText)
    }
    private val mSecondEditText: EditText by lazy {
        findViewById<EditText>(R.id.secondText)
    }
    private val mCrossEditText: EditText by lazy {
        findViewById<EditText>(R.id.editTextCross)
    }
    private val mRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }
    private val mSaveButton: Button by lazy {
        findViewById<Button>(R.id.saveButton)
    }

    private lateinit var mNavView: NavigationView

    private var mAllowBlankMale: Boolean = false

    private var mCrossOrder: Int = 0

    private val mEntries = ArrayList<AdapterEntry>()

    private lateinit var mObj: AdapterEntry

    private val mAdapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            mObj = obj
            return R.layout.animated_row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.dateTextView) as TextView

        init {
            itemView.setOnClickListener {
                startCrossActivity()
            }
        }

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
            secondText.text = data.second

            val cross = mDbHelper.getRowId(mObj.first)
            val pol = mDbHelper.getPollinationType(cross)
            itemView.findViewById<ImageView>(R.id.crossTypeImageView)
                    .setImageDrawable(when (pol) {
                        "Self-Pollinated" -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_human_female)
                        "Biparental" -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_human_male_female)
                        else -> ContextCompat.getDrawable(this@IntercrossActivity,
                                R.drawable.ic_human_female_female)
                    })
        }

        private fun startCrossActivity() {

            val intent = Intent(this@IntercrossActivity, CrossActivity::class.java)

            intent.putExtra(CROSS_ID, firstText.text)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                        this@IntercrossActivity, itemView, "cross")
                        .toBundle())
            } else startActivity(intent)

        }
    }

    private val mPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { pref, key ->

        key?.let {
            when (key) {
                "org.phenoapps.intercross.LABEL_PATTERN_CREATED" -> {

                }
                SettingsActivity.PATTERN -> {
                    when (pref?.getBoolean(key, true)) {
                        true -> {
                            val prefix = pref.getString("LABEL_PATTERN_PREFIX", "")
                            val suffix = pref.getString("LABEL_PATTERN_SUFFIX", "")
                            val num = pref.getInt("LABEL_PATTERN_MID", 0)
                            val pad = pref.getInt("LABEL_PATTERN_PAD", 0)
                            mCrossEditText.setText("$prefix${num.toString().padStart(pad, '0')}$suffix")
                            mCrossEditText.isEnabled = false
                        }
                        false -> {
                            mCrossEditText.setText("")
                            mCrossEditText.isEnabled = true
                        }
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

    //callback for swiping on recycler view items
    private val mItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            val builder = AlertDialog.Builder(this@IntercrossActivity).apply {

                setTitle("Delete cross entry?")

                setNegativeButton("Cancel") { _, _ ->
                    mAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    mRecyclerView.scrollToPosition(viewHolder.adapterPosition)
                }

                setPositiveButton("Yes") { _, _ ->
                    mDbHelper.deleteEntry(
                            mDbHelper.getRowId(mAdapter.listItems[viewHolder.adapterPosition].first))
                    mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    loadSQLToLocal()
                }
            }
            builder.show()
        }
    }

    private val mDbHelper: IntercrossDbHelper = IntercrossDbHelper(this)

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private fun isCameraAllowed(requestCode: Int): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) return true
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), requestCode)

        return false
    }

    private fun isExternalStorageWritable(): Boolean {
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

        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        ItemTouchHelper(mItemTouchCallback).attachToRecyclerView(mRecyclerView)

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView)

        // Setup drawer view
        setupDrawerContent(mNavView)
        setupDrawer()

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

        val focusListener = View.OnFocusChangeListener { _, p1 ->
            if (p1 && PreferenceManager.getDefaultSharedPreferences(this@IntercrossActivity)
                            ?.getString(SettingsActivity.PERSON, "")?.isNotBlank() == false) {
                askUserForPerson()
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
                if (PreferenceManager.getDefaultSharedPreferences(this@IntercrossActivity)
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

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val isAutoPattern = pref.getBoolean(SettingsActivity.PATTERN, false)

        if (isAutoPattern) {
            val prefix = pref.getString("LABEL_PATTERN_PREFIX", "")
            val suffix = pref.getString("LABEL_PATTERN_SUFFIX", "")
            val num = pref.getInt("LABEL_PATTERN_MID", 0)
            val pad = pref.getInt("LABEL_PATTERN_PAD", 0)
            mCrossEditText.setText("$prefix${num.toString().padStart(pad, '0')}$suffix")
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

        //calculate how how full the save button should be
        var numFilled = 0
        if (pref.getBoolean(SettingsActivity.BLANK_MALE_ID, false)) numFilled++
        else if (male.isNotBlank()) numFilled++
        if (female.isNotBlank()) numFilled++
        if (cross.isNotBlank()) numFilled++

        //change save button fill percentage using corresponding xml shapes
        mSaveButton.background = ContextCompat.getDrawable(this,
                when (numFilled) {
                    0 -> R.drawable.save_button_empty
                    1 -> R.drawable.save_button_third
                    2 -> R.drawable.save_button_two_thirds
                    else -> R.drawable.save_button_full
                })

        return ((male.isNotEmpty() || mAllowBlankMale) && female.isNotEmpty()
                && (cross.isNotEmpty() || auto))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                //exitTransition  = Explode()
            }
        } else {
            // Swap without transition
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        mCrossOrder = sharedPref?.getString(SettingsActivity.CROSS_ORDER, "0")?.toInt() ?: 0

        sharedPref.registerOnSharedPreferenceChangeListener(mPrefListener)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        //Show Tutorial Fragment for first-time users
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            if (!getBoolean(COMPLETED_TUTORIAL, false)) {
                startActivity(Intent(this@IntercrossActivity, IntroActivity::class.java))
            }
            if (getBoolean(SettingsActivity.PATTERN, false)) {
                mCrossEditText.isEnabled = false
            }
            mAllowBlankMale = getBoolean(SettingsActivity.BLANK_MALE_ID, false)
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
            putBoolean(COMPLETED_TUTORIAL, true)
            apply()
        }
    }

    private fun saveToDB() {

        val male: String
        val female: String
        var cross: String = mCrossEditText.text.toString()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val isAutoPattern = pref.getBoolean(SettingsActivity.PATTERN, false)

        var prefix = String()
        var suffix = String()
        var num = 0
        var pad = 0
        if (isAutoPattern) {
            prefix = pref.getString("LABEL_PATTERN_PREFIX", "") ?: ""
            suffix = pref.getString("LABEL_PATTERN_SUFFIX", "") ?: ""
            num = pref.getInt("LABEL_PATTERN_MID", 0)
            pad = pref.getInt("LABEL_PATTERN_PAD", 0)
            cross = "$prefix${num.toString().padStart(pad, '0')}$suffix"
            val edit = pref.edit()
            edit.putInt("LABEL_PATTERN_MID", num + 1)
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
                male.isBlank() -> "Open Pollinated"
                male != female -> "Biparental"
                else -> "Self-Pollinated"
            }

            val crossCount = mDbHelper.getCrosses(female, male).size + 1

            val entry = ContentValues()

            entry.put("male", if (male.isBlank()) "blank" else male)
            entry.put("female", female)
            entry.put("cross_id", cross)
            entry.put("cross_type", pollinationType)
            entry.put("cross_count", crossCount)
            entry.put("cross_name", "$female/$male-$crossCount")

            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.getDefault())
            val sdfShort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val person: String = pref?.getString(SettingsActivity.PERSON, "None") ?: "None"

            entry.put("timestamp", sdf.format(c.time).toString())

            entry.put("person", person)

            mDbHelper.insertEntry(entry)

            //clear fields
            mFirstEditText.text.clear()
            mSecondEditText.text.clear()
            if (isAutoPattern) {
                mCrossEditText.setText("$prefix${(num + 1).toString().padStart(pad, '0')}$suffix")
            } else mCrossEditText.text.clear()

            mFirstEditText.requestFocus()

            //add new items to the top
            val index = 0
            mEntries.add(index, AdapterEntry().apply {
                first = cross
                second = sdfShort.format(c.time).toString()
            })

            mAdapter.notifyItemInserted(index)

            mRecyclerView.scrollToPosition(0)

            ringNotification(success = true)
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

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun askIfSamePerson() {

        val v = findViewById<ConstraintLayout>(R.id.constraint_layout_parent)
        v.requestFocus()

        val view = layoutInflater.inflate(R.layout.person_check_layout, null)
        val person = PreferenceManager.getDefaultSharedPreferences(this@IntercrossActivity)
                .getString(SettingsActivity.PERSON, "Guillaume")

        view.findViewById<TextView>(R.id.textView).text = "Is this still $person?"

        val builder = AlertDialog.Builder(this).apply {

            setNegativeButton("Yes") { _, _ ->
                //welcome back
            }

            setPositiveButton("Change Person") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(Intent(this@IntercrossActivity, SettingsActivity::class.java))
                } else startActivity(Intent(this@IntercrossActivity, SettingsActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            setView(view)

            setCancelable(false)

        }

        builder.show()
    }

    private fun askUserExportFileName() {

        if (isExternalStorageWritable()) {

            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd_hh:mm:ss", Locale.getDefault())

            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val person = pref?.getString(SettingsActivity.PERSON, "None") ?: "None"
            val date = sdf.format(c.time).toString()

            val input = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                setText("crosses_${person}_$date")
            }

            val builder = AlertDialog.Builder(this).apply {

                setView(input)

                setPositiveButton("Export CSV") { _, _ ->
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
                                fstream.write(lineSeparator?.toByteArray())
                            }

                            scanFile(this@IntercrossActivity, output)

                            fstream.flush()
                            fstream.close()
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (io: IOException) {
                            io.printStackTrace()
                        } finally {
                            Toast.makeText(this@IntercrossActivity, "File write successful!", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@IntercrossActivity,
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
                Toast.makeText(this@IntercrossActivity,
                        "Person must be set before crosses can be made.", Toast.LENGTH_SHORT).show()
            }

            setPositiveButton("Set Person") { _, _ ->
                startActivity(Intent(this@IntercrossActivity, SettingsActivity::class.java))
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
                val builder = AlertDialog.Builder(this@IntercrossActivity).apply {

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

        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            android.R.id.home -> dl.openDrawer(GravityCompat.START)
            R.id.action_camera -> if (isCameraAllowed(CAMERA_SCAN_REQ)) {
                startActivityForResult(Intent(this, ScanActivity::class.java),
                        CAMERA_INTENT_SCAN)
            }
            R.id.action_search -> if (isCameraAllowed(CAMERA_SEARCH_REQ)) {
                startActivityForResult(Intent(this, ScanActivity::class.java),
                        CAMERA_INTENT_SEARCH)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {

            intent?.let {

                //barcode text response from Zebra intent
                if (intent.hasExtra(CAMERA_RETURN_ID)) {

                    when (requestCode) {
                        CAMERA_INTENT_SCAN -> {
                            asList(mFirstEditText, mSecondEditText, mCrossEditText)
                                    .forEach { et ->
                                        if (et.hasFocus()) {
                                            et.setText(intent.getStringExtra(CAMERA_RETURN_ID))
                                            when (it) {
                                                mFirstEditText -> mSecondEditText.requestFocus()
                                                mSecondEditText -> mCrossEditText.requestFocus()
                                                mCrossEditText -> {
                                                    saveToDB()
                                                }
                                                else -> Log.d("Focus", "Unexpected request focus.")
                                            }
                                            return
                                        }
                                    }
                        }
                        CAMERA_INTENT_SEARCH -> {

                            val cross = intent.getStringExtra(CAMERA_RETURN_ID)
                            val id = mDbHelper.getRowId(cross)

                            if (id == -1) Toast.makeText(this,
                                    "This cross ID is not in the database.", Toast.LENGTH_LONG).show()
                            else {
                                startActivity(Intent(this@IntercrossActivity, CrossActivity::class.java).apply {
                                    putExtra(CROSS_ID, cross)
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupDrawer() {

        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)
        mDrawerToggle = object : ActionBarDrawerToggle(this, dl,
                org.phenoapps.intercross.R.string.drawer_open, org.phenoapps.intercross.R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                //update the person viewed under "Intercross" each time the drawer opens
                mNavView.getHeaderView(0).apply {
                    findViewById<TextView>(R.id.navUserTextView)
                            .text = PreferenceManager
                            .getDefaultSharedPreferences(this@IntercrossActivity)
                            .getString(SettingsActivity.PERSON, "Trevor")
                }

                val view = this@IntercrossActivity.currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            override fun onDrawerClosed(view: View) {}

        }

        mDrawerToggle.isDrawerIndicatorEnabled = true
        dl.addDrawerListener(mDrawerToggle)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_summary -> {
                startActivity(Intent(this@IntercrossActivity, SummaryActivity::class.java))
            }
            R.id.nav_settings ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(Intent(this, SettingsActivity::class.java),
                            ActivityOptions.makeSceneTransitionAnimation(this@IntercrossActivity).toBundle())
                } else startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_export -> askUserExportFileName()
            R.id.nav_about -> showAboutDialog()
            R.id.nav_simple_print ->
                startActivity(Intent(this, SimplePrintActivity::class.java))
            R.id.nav_intro ->
                startActivity(Intent(this, IntroActivity::class.java))
            R.id.nav_delete_entries -> askUserDeleteEntries()
        }

        val dl = findViewById<DrawerLayout>(R.id.drawer_layout)
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

                    packageInfo.let {
                        versionTextView.let {
                            versionTextView.text = "${resources.getString(R.string.versiontitle)} ${packageInfo.versionName}"
                        }
                    }

                } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                versionTextView.setOnClickListener { this@IntercrossActivity.showChangeLog() }
            }

            builder.setCancelable(true)
            builder.setTitle(this.resources.getString(
                    org.phenoapps.intercross.R.string.about))
            builder.setView(personView)
        }

        builder.setNegativeButton(
                this.resources.getString(org.phenoapps.intercross.R.string.ok)
        ) { dialog, _ ->
            assert(dialog != null)
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showChangeLog() {

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    public override fun onDestroy() {
        mDbHelper.close()
        super.onDestroy()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        if ((PreferenceManager.getDefaultSharedPreferences(this@IntercrossActivity)
                        .getString(SettingsActivity.PERSON, "") ?: "").isNotBlank())
            askIfSamePerson()
    }

    //Control flow is sent here when a user is prompted for permission access
    override fun onRequestPermissionsResult(resultCode: Int, permissions: Array<String>, granted: IntArray) {
        //Control flow only continues if that permission has been accepted.
        permissions.forEachIndexed { index, perm ->
            if (granted[index] == PackageManager.PERMISSION_GRANTED) {
                when (perm) {
                    "android.permission.WRITE_EXTERNAL_STORAGE" -> {
                        if (resultCode == REQUEST_WRITE_PERMISSION) {
                            askUserExportFileName()
                        }
                    }
                    "android.permission.CAMERA" -> {
                        when (resultCode) {
                            CAMERA_SCAN_REQ -> {
                                startActivityForResult(Intent(this, ScanActivity::class.java),
                                        CAMERA_INTENT_SCAN)
                            }
                            CAMERA_SEARCH_REQ -> {
                                startActivityForResult(Intent(this, ScanActivity::class.java),
                                        CAMERA_INTENT_SEARCH)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ringNotification(success: Boolean) {
        if (PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(SettingsActivity.AUDIO_ENABLED, false)) {
            try {
                when (success) {
                    true -> {
                        val chimePlayer = MediaPlayer.create(this,
                                resources.getIdentifier("plonk", "raw", packageName))
                        chimePlayer.start()
                        chimePlayer.setOnCompletionListener {
                            chimePlayer.release()
                        }
                    }
                    false -> {
                        val chimePlayer = MediaPlayer.create(this,
                                resources.getIdentifier("error", "raw", packageName))
                        chimePlayer.start()
                        chimePlayer.setOnCompletionListener {
                            chimePlayer.release()
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        private val lineSeparator = System.getProperty("line.separator")

        //request
        private const val CAMERA_SCAN_REQ = 100
        private const val CAMERA_SEARCH_REQ = 101
        private const val CAMERA_INTENT_SCAN = 102
        private const val CAMERA_INTENT_SEARCH = 105

        const val REQUEST_WRITE_PERMISSION = 200
        const val IMPORT_ZPL = 500

        //extras
        const val CROSS_ID = "org.phenoapps.intercross.CROSS_ID"
        const val CAMERA_RETURN_ID = "org.phenoapps.intercross.CAMERA_RETURN_ID"
        const val COMPLETED_TUTORIAL = "org.phenoapps.intercross.COMPLETED_TUTORIAL"

        fun scanFile(ctx: Context, filePath: File) {
            MediaScannerConnection.scanFile(ctx, arrayOf(filePath.absolutePath), null, null)
        }
    }
}
