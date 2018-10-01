package org.phenoapps.intercross

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.sqlite.SQLiteException
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList

class MainActivity : AppCompatActivity() {

    private lateinit var mFirstEditText: EditText
    private lateinit var mSecondEditText: EditText
    private lateinit var mCrossEditText: EditText
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mNavView: NavigationView

    private var mCrossOrder: Int = 0

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            return R.layout.row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

    }

    private val mPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        key?.let{
            when (key) {
                SettingsActivity.CROSS_ORDER -> {
                    when (sharedPreferences?.getString(key, "0")) {
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
            }
        }
    }

    private val mDbHelper: IdEntryDbHelper = IdEntryDbHelper(this)

    private var mDrawerToggle: ActionBarDrawerToggle? = null

    private var mNameMap: MutableMap<String, String>? = null

    private val isExternalStorageWritable: Boolean
        get() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Security", "Permission is granted")
                    return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 200)
                }
            } else
                return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

            return false
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mNameMap = HashMap()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
//
        /*val namePrefs = getSharedPreferences("Names", Context.MODE_PRIVATE)
        val encodedNames = namePrefs.getStringSet("EncodedNames", HashSet())
        if (encodedNames.size > 0) {
            val encodings = encodedNames.toTypedArray<String>()
            val size = encodedNames.size
            for (i in 0 until size) {
                val keyVal = encodings[i].split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val key = keyVal[0]
                val `val` = keyVal[1]
                mNameMap!![key] = `val`
            }
        }
*/

        mCrossOrder = sharedPref.getString(SettingsActivity.CROSS_ORDER, "0").toInt()

        sharedPref.registerOnSharedPreferenceChangeListener(mPrefListener)

        initializeUIVariables()

        loadSQLToLocal()

    }

    private fun initializeUIVariables() {

        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)

        supportActionBar?.let {
            it.title = "Intercross"
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView) as NavigationView

        // Setup drawer view
        setupDrawerContent(mNavView)
        setupDrawer()

        mSecondEditText = findViewById(R.id.secondText) as EditText
        mFirstEditText = findViewById(R.id.firstText) as EditText
        mCrossEditText = findViewById(R.id.editTextCross) as EditText
        val saveButton = findViewById(R.id.saveButton) as Button

        //single text watcher class to check if all fields are non-empty to enable the save button
        val emptyGuard = object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                saveButton.isEnabled = mFirstEditText.text.isNotEmpty()
                        && mSecondEditText.text.isNotEmpty()
                        && mCrossEditText.text.isNotEmpty()

            }

            override fun afterTextChanged(editable: Editable) {

            }
        }

        mSecondEditText.addTextChangedListener(emptyGuard)
        mFirstEditText.addTextChangedListener(emptyGuard)
        mCrossEditText.addTextChangedListener(emptyGuard)

        mCrossEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                saveToDB()
                return@OnEditorActionListener true
            }
            false
        })

        saveButton.isEnabled = false
        saveButton.setOnClickListener {
            saveToDB()
        }

        (findViewById(R.id.clearButton) as Button).setOnClickListener {
            mCrossEditText.setText("")
            mFirstEditText.setText("")
            mSecondEditText.setText("")
            mFirstEditText.requestFocus()

        }

        when (mCrossOrder) {
            0 -> {
                mFirstEditText.hint = "Female ID: "
                mSecondEditText.hint = "Male ID: "
            }
            1 -> {
                mFirstEditText.hint = "Male ID: "
                mSecondEditText.hint = "Female ID: "
            }
        }
        mFirstEditText.requestFocus()
    }

    private fun saveToDB() {

        if (mFirstEditText.text.isNotEmpty() && mSecondEditText.text.isNotEmpty()
                && mCrossEditText.text.isNotEmpty()) {

            val entry = ContentValues()

            entry.put("male", if (mCrossOrder == 0) mSecondEditText.text.toString() else mFirstEditText.text.toString())
            entry.put("female", if (mCrossOrder == 0) mFirstEditText.text.toString() else mSecondEditText.text.toString())
            entry.put("cross_id", mCrossEditText.text.toString())

            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            val person : String = pref.getString(SettingsActivity.PERSON, "None")

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

    //TODO Kotlin-ize
    private fun askUserExportFileName() {

        if (isExternalStorageWritable) {

            val builder = AlertDialog.Builder(this)

            builder.setTitle("Choose name for exported file.")

            val input = EditText(this)

            input.inputType = InputType.TYPE_CLASS_TEXT

            builder.setView(input)

            builder.setPositiveButton("Export") { dialog, which ->
                val value = input.text.toString()
                if (value.isNotEmpty()) {
                    try {
                        val dir = File(Environment.getExternalStorageDirectory().path + "/Intercross")
                        val output = File(dir, "$value.csv")
                        val fstream = FileOutputStream(output)
                        val db = mDbHelper.readableDatabase
                        val cursor = db.query(IdEntryContract.IdEntry.TABLE_NAME,
                                null, null, null,
                                null, null, null)

                        //first write header line
                        val headers = cursor.columnNames
                        for (i in headers.indices) {
                            if (i != 0) fstream.write(",".toByteArray())
                            fstream.write(headers[i].toByteArray())
                        }
                        fstream.write(line_separator.toByteArray())
                        //populate text file with current database values
                        if (cursor.moveToFirst()) {
                            do {
                                for (i in headers.indices) {
                                    if (i != 0) fstream.write(",".toByteArray())
                                    val colVal = cursor.getString(
                                            cursor.getColumnIndexOrThrow(headers[i])
                                    )
                                    fstream.write((colVal ?: "null").toByteArray())
                                }
                                fstream.write(line_separator.toByteArray())
                            } while (cursor.moveToNext())
                        }

                        scanFile(this@MainActivity, output)

                        cursor.close()
                        fstream.flush()
                        fstream.close()
                    } catch (e: SQLiteException) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, "Error exporting file, is your table empty?", Toast.LENGTH_SHORT).show()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (io: IOException) {
                        io.printStackTrace()
                    }

                } else {
                    Toast.makeText(this@MainActivity,
                            "Must enter a file name.", Toast.LENGTH_SHORT).show()
                }

                builder.show()
            }
        } else {
            Toast.makeText(this@MainActivity, "External storage not writable.", Toast.LENGTH_SHORT).show()
        }
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
            R.id.action_count -> {
                val countIntent = Intent(this, CountActivity::class.java)
                countIntent.putExtra("NameMapKey", mNameMap!!.keys.toTypedArray<String>())
                countIntent.putExtra("NameMapValue", mNameMap!!.values.toTypedArray<String>())
                startActivity(countIntent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {

            if (intent != null) {
                when (requestCode) {
                    100 -> importListOfReadableNames(intent.data)
                    IntercrossConstants.MANAGE_HEADERS_REQ -> {
                        mDbHelper.updateColumns(intent.extras.getStringArrayList(IntercrossConstants.HEADERS))
                    }
                    IntercrossConstants.USER_INPUT_HEADERS_REQ -> {
                        mDbHelper.updateValues(intent.extras.getInt(IntercrossConstants.COL_ID_KEY).toString(),
                                intent.extras.getStringArrayList(IntercrossConstants.USER_INPUT_VALUES)
                        )
                    }
                }

                //barcode text response from Zebra intent
                if (intent.hasExtra(IntercrossConstants.CAMERA_RETURN_ID)) {

                    asList(mFirstEditText, mSecondEditText, mCrossEditText).forEach iter@ { editText ->
                        editText?.let {
                            when(editText.hasFocus()) {
                                true -> {
                                    editText.setText(intent.getStringExtra(IntercrossConstants.CAMERA_RETURN_ID))
                                    when(editText) {
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

    //TODO Kotlin-ize
    private fun importListOfReadableNames(data: Uri) {

        /*
        mNameMap = HashMap()
        val `is`: InputStream?
        try {
            `is` = contentResolver.openInputStream(data)
            if (`is` != null) {
                val br = BufferedReader(InputStreamReader(`is`))
                val header = br.readLine()
                var temp: String
                while ((temp = br.readLine()) != null) {
                    val map = temp.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (map.size == 2) {
                        mNameMap!![map[0]] = map[1]
                    }
                }
                br.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (mNameMap!!.size > 0) {
            val pref = getSharedPreferences("Names", Context.MODE_PRIVATE)
            val edit = pref.edit()
            val keys = mNameMap!!.keys.toTypedArray<String>()
            val values = mNameMap!!.values.toTypedArray<String>()
            val keyVals = arrayOfNulls<String>(keys.size)
            if (keys.size == values.size) {
                for (i in keys.indices) {
                    val k = keys[i].replace(":".toRegex(), "")
                    val v = values[i].replace(":".toRegex(), "")
                    keyVals[i] = k + ":" + v
                    for (e in mCrossIds!!) {
                        if (e.crossId == k) e.crossName = v
                    }
                }
            }
            edit.putStringSet("EncodedNames", HashSet(Arrays.asList<String>(*keyVals)))
            edit.apply()

            buildListView()
        }*/
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry>, View.OnClickListener {

        private var id: Int = -1
        private var firstText: TextView = itemView.findViewById(R.id.firstTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.secondTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: AdapterEntry) {

            id = data.id
            firstText.text = data.first
            secondText.text = data.second
        }

        override fun onClick(v: View?) {

            v?.let {

                val intent = Intent(this@MainActivity, AuxValueInputActivity::class.java)

                val headers = mDbHelper.getColumns() - IdEntryContract.IdEntry.COLUMNS.toList()

                val values = mDbHelper.getUserInputValues(id)

                intent.putExtra(IntercrossConstants.COL_ID_KEY, id)

                intent.putExtra(IntercrossConstants.CROSS_ID, firstText.text)

                intent.putExtra(IntercrossConstants.TIMESTAMP, secondText.text)

                intent.putStringArrayListExtra(IntercrossConstants.HEADERS, ArrayList(headers))

                intent.putStringArrayListExtra(IntercrossConstants.USER_INPUT_VALUES, ArrayList(values))

                startActivityForResult(intent, IntercrossConstants.USER_INPUT_HEADERS_REQ)
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
            org.phenoapps.intercross.R.id.nav_intro -> {

            }
            org.phenoapps.intercross.R.id.nav_manage_headers -> {
                startActivityForResult(Intent(this@MainActivity,
                        ManageHeadersActivity::class.java), IntercrossConstants.MANAGE_HEADERS_REQ)
            }
            R.id.nav_import_readable_names -> {
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.type = "*/*"
                startActivityForResult(i, 100)
            }
        }

        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        dl.closeDrawers()
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
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    public override fun onDestroy() {
        mDbHelper.close()
        super.onDestroy()
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

        private val line_separator = System.getProperty("line.separator")

        fun scanFile(ctx: Context, filePath: File) {
            MediaScannerConnection.scanFile(ctx, arrayOf(filePath.absolutePath), null, null)
        }
    }
}
