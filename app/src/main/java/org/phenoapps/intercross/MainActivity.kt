package org.phenoapps.intercross

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
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
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.graphics.internal.ZebraImageAndroid
import com.zebra.sdk.printer.PrinterStatus
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import com.zebra.sdk.printer.ZebraPrinterLinkOs

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.HashMap
import java.util.HashSet
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var mDbHelper: IdEntryDbHelper? = null

    private var mPrefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var mCrossIds: ArrayList<AdapterEntry>? = null

    private var mDrawerToggle: ActionBarDrawerToggle? = null

    private var focusedTextView: View? = null

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

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mCrossIds = ArrayList()

        mNameMap = HashMap()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val namePrefs = getSharedPreferences("Names", Context.MODE_PRIVATE)
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

        mPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s -> }

        sharedPref.registerOnSharedPreferenceChangeListener(mPrefListener)

        if (!sharedPref.getBoolean("onlyLoadTutorialOnce", false)) {
            launchIntro()
            val editor = sharedPref.edit()
            editor.putBoolean("onlyLoadTutorialOnce", true)
            editor.apply()
        } else {
            val tutorialMode = sharedPref.getBoolean(SettingsActivity.TUTORIAL_MODE, false)

            if (tutorialMode)
                launchIntro()
        }

        var allGranted = true
        for (perm in IntercrossConstants.permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, IntercrossConstants.permissions,
                    IntercrossConstants.PERM_REQ)
        }

        if (isExternalStorageWritable) {
            val intercrossDir = File(Environment.getExternalStorageDirectory().path + "/Intercross")
            if (!intercrossDir.isDirectory) {
                val makeDirsSuccess = intercrossDir.mkdirs()
                if (!makeDirsSuccess) Log.d("Make Directory", "failed")
            }
        }

        initializeUIVariables()

        mDbHelper = IdEntryDbHelper(this)

        loadSQLToLocal()

    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private fun initializeUIVariables() {

        if (supportActionBar != null) {
            supportActionBar!!.title = null
            supportActionBar!!.themedContext
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
        }

        val nvDrawer = findViewById(R.id.nvView) as NavigationView

        // Setup drawer view
        setupDrawerContent(nvDrawer)
        setupDrawer()

        val maleEditText = findViewById(R.id.editTextMale) as EditText
        val femaleEditText = findViewById(R.id.editTextFemale) as EditText
        val crossEditText = findViewById(R.id.editTextCross) as EditText
        val saveButton = findViewById(R.id.saveButton) as Button

        //single text watcher class to check if all fields are non-empty to enable the save button
        val emptyGuard = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (maleEditText.text.length != 0
                        && femaleEditText.text.length != 0
                        && crossEditText.text.length != 0) {
                    saveButton.isEnabled = true
                } else {
                    saveButton.isEnabled = false
                }
            }

            override fun afterTextChanged(editable: Editable) {

            }
        }

        val focusChanged = View.OnFocusChangeListener { view, b -> focusedTextView = view }

        maleEditText.addTextChangedListener(emptyGuard)
        femaleEditText.addTextChangedListener(emptyGuard)
        crossEditText.addTextChangedListener(emptyGuard)

        crossEditText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                saveToDB()
                focusedTextView = femaleEditText
                focusedTextView!!.requestFocus()
                return@OnEditorActionListener true
            }
            false
        })

        maleEditText.onFocusChangeListener = focusChanged
        femaleEditText.onFocusChangeListener = focusChanged
        crossEditText.onFocusChangeListener = focusChanged

        saveButton.isEnabled = false
        saveButton.setOnClickListener {
            saveToDB()
            focusedTextView = femaleEditText
            focusedTextView!!.requestFocus()
        }

        (findViewById(R.id.clearButton) as Button).setOnClickListener {
            (findViewById(R.id.editTextCross) as EditText).setText("")
            (findViewById(R.id.editTextMale) as EditText).setText("")
            (findViewById(R.id.editTextFemale) as EditText).setText("")
            focusedTextView = femaleEditText
            focusedTextView!!.requestFocus()
        }

        focusedTextView = femaleEditText
        focusedTextView!!.requestFocus()
    }

    private fun saveToDB() {

        val maleEditText = findViewById(R.id.editTextMale) as EditText
        val femaleEditText = findViewById(R.id.editTextFemale) as EditText
        val crossEditText = findViewById(R.id.editTextCross) as EditText

        if (maleEditText.text.length != 0
                && femaleEditText.text.length != 0
                && crossEditText.text.length != 0) {

            //database update
            val db = mDbHelper!!.writableDatabase

            val entry = ContentValues()

            entry.put("male", maleEditText.text.toString())
            entry.put("female", femaleEditText.text.toString())
            entry.put("cross_id", crossEditText.text.toString())

            val c = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

            //String firstName = sharedPref.getString(SettingsActivity.FIRST_NAME, "");

            entry.put("timestamp", sdf.format(c.time).toString())

            entry.put("person", "not implemented.")

            db.insert("INTERCROSS", null, entry)

            //clear fields
            maleEditText.text.clear()
            femaleEditText.text.clear()
            crossEditText.text.clear()

            loadSQLToLocal()
        }
    }

    //reads the SQLite database and displays cross ids and their total counts / timestamps
    private fun loadSQLToLocal() {

        mDbHelper = IdEntryDbHelper(this)

        mCrossIds = ArrayList()

        val db = mDbHelper!!.readableDatabase
        try {
            val table = IdEntryContract.IdEntry.TABLE_NAME
            val cursor = db.query(table, null, null, null, null, null, null)

            if (cursor.moveToFirst()) {
                do {
                    val headers = cursor.columnNames
                    var male: String? = null
                    var female: String? = null
                    var crossId: String? = null
                    var timestamp: String? = null

                    for (header in headers) {


                        val `val` = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        )

                        if (header == "male") {
                            male = `val`
                        }

                        if (header == "female") {
                            female = `val`
                        }

                        if (header == "cross_id") crossId = `val`

                        if (header == "timestamp") timestamp = `val`.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]

                    }

                    if (male != null && female != null) {
                        val countCursor = db.query(table, arrayOf("male, female"),
                                "male=? and female=?",
                                arrayOf(male, female), null, null, null)

                        val entry = AdapterEntry(crossId, timestamp, mNameMap!!.get(crossId))

                        mCrossIds!!.add(entry)

                        countCursor.close()
                    }

                } while (cursor.moveToNext())

            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        buildListView()
    }

    private fun askUserExportFileName() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose name for exported file.")
        val input = EditText(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }
        builder.setView(input)

        builder.setPositiveButton("Export") { dialog, which ->
            val value = input.text.toString()
            if (value.length != 0) {
                if (isExternalStorageWritable) {
                    try {
                        val dir = File(Environment.getExternalStorageDirectory().path + "/Intercross")
                        val output = File(dir, "$value.csv")
                        val fstream = FileOutputStream(output)
                        val db = mDbHelper!!.readableDatabase
                        val table = IdEntryContract.IdEntry.TABLE_NAME
                        val cursor = db.query(table, null, null, null, null, null, null)
                        //final Cursor cursor = db.rawQuery("SElECT * FROM VERIFY", null);

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
                                    val `val` = cursor.getString(
                                            cursor.getColumnIndexOrThrow(headers[i])
                                    )
                                    if (`val` == null)
                                        fstream.write("null".toByteArray())
                                    else
                                        fstream.write(`val`.toByteArray())
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
                            "External storage not writable.", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this@MainActivity,
                        "Must enter a file name.", Toast.LENGTH_SHORT).show()
            }
        }

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
                }

                //barcode text response from Zebra intent
                if (intent.hasExtra(IntercrossConstants.CAMERA_RETURN_ID)) {
                    if (focusedTextView == null) {
                        focusedTextView = findViewById(R.id.editTextMale)

                    }
                    (focusedTextView as EditText)
                            .setText(intent.getStringExtra(IntercrossConstants.CAMERA_RETURN_ID))

                    if (focusedTextView === findViewById(R.id.editTextMale)) {
                        focusedTextView = findViewById(R.id.editTextFemale)
                    } else if (focusedTextView === findViewById(R.id.editTextFemale)) {
                        focusedTextView = findViewById(R.id.editTextCross)
                    } else {
                        focusedTextView = findViewById(R.id.editTextMale)
                    }
                    focusedTextView!!.requestFocus()
                }
            }
        }
    }

    private fun importListOfReadableNames(data: Uri) {

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
        }
    }

    private fun loadFileToSQL(data: Uri) {


        val db = mDbHelper!!.writableDatabase

        db.execSQL(IdEntryContract.SQL_DELETE_ENTRIES)

        db.execSQL(IdEntryContract.SQL_CREATE_ENTRIES)

        try {
            val `is` = contentResolver.openInputStream(data)
            if (`is` != null) {
                val br = BufferedReader(InputStreamReader(`is`))
                val headerLine = br.readLine().split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                var temp: String? = null
                val entry = ContentValues()
                db.beginTransaction()
                while ((temp = br.readLine()) != null) {
                    val row = temp!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    val size = row.size
                    for (i in 0 until size) {
                        entry.put(headerLine[i], row[i])
                    }
                    val newRowId = db.insert("INTERCROSS", null, entry)
                    entry.clear()
                }
                db.setTransactionSuccessful()
                db.endTransaction()
                br.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

    }

    private fun buildListView() {

        val recyclerView = findViewById(R.id.crossList) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CrossRecyclerViewAdapter(this, mCrossIds)
        //adapter.setClickListener(this);
        recyclerView.adapter = adapter
    }

    private fun clearListView() {

        val idTable = findViewById(R.id.crossList) as ListView
        val adapter = ArrayAdapter<String>(this, org.phenoapps.intercross.R.layout.row)

        idTable.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun setupDrawer() {

        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        mDrawerToggle = object : ActionBarDrawerToggle(this, dl,
                org.phenoapps.intercross.R.string.drawer_open, org.phenoapps.intercross.R.string.drawer_close) {

            override fun onDrawerOpened(drawerView: View?) {
                val view = this@MainActivity.currentFocus
                if (view != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }

            override fun onDrawerClosed(view: View?) {}

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
                val intro_intent = Intent(this@MainActivity, IntroActivity::class.java)
                runOnUiThread { startActivity(intro_intent) }
            }
            org.phenoapps.intercross.R.id.nav_manage_headers -> {
                val nav_manage_headers = Intent(this@MainActivity, ManageHeadersActivity::class.java)
                runOnUiThread { startActivity(nav_manage_headers) }
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

    private fun launchIntro() {

        Thread(Runnable {
            //  Launch app intro
            val i = Intent(this@MainActivity, IntroActivity::class.java)

            runOnUiThread { startActivity(i) }
        }).start()
    }

    public override fun onDestroy() {
        mDbHelper!!.close()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                MediaScannerConnection.scanFile(ctx, arrayOf(filePath.absolutePath), null, null)
            }
        }
    }
}
