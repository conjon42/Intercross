package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.graphics.internal.ZebraImageAndroid
import com.zebra.sdk.printer.PrinterStatus
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinter
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import com.zebra.sdk.printer.ZebraPrinterLinkOs

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

//TODO create separate file for async bluetooth task
//TODO create bitmap preview of barcode print
class AuxValueInputActivity : AppCompatActivity() {

    private var mTimestamp: String? = null
    private var mCrossId: String? = null

    private var mDbHelper: IdEntryDbHelper? = null

    private val mPrefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val mCrossIds: ArrayList<AdapterEntry>? = null

    private val mDrawerToggle: ActionBarDrawerToggle? = null

    private val focusedTextView: View? = null

    class EditTextAdapterEntry internal constructor(var editText: EditText, var headerTextValue: String)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val crossId = intent.getStringExtra("crossId")
        // String[] headers = getIntent().getStringArrayExtra("headers");

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        mCrossId = crossId

        val headers = prefs.getStringSet(SettingsActivity.HEADER_SET, HashSet()).toTypedArray<String>()
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mDbHelper = IdEntryDbHelper(this)

        val db = mDbHelper!!.writableDatabase
        val colMap = HashMap<String, String>()

        try {
            val table = IdEntryContract.IdEntry.TABLE_NAME

            val cursor = db.query(table, headers, "cross_id=?", arrayOf(crossId), null, null, null)

            if (cursor.moveToFirst()) {
                do {
                    val headerCols = cursor.columnNames

                    for (header in headerCols) {


                        val `val` = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        )

                        if (header == "timestamp") mTimestamp = `val`


                        colMap[header] = `val`

                    }

                } while (cursor.moveToNext())

            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        val entries = ArrayList<EditTextAdapterEntry>()
        val editTexts = ArrayList<EditText>()

        for (header in headers) {
            val editText = EditText(this)
            editText.setText(colMap[header])
            entries.add(EditTextAdapterEntry(editText, header))
            editTexts.add(editText)
        }

        val adapter = object : ViewAdapter<EditTextAdapterEntry>(entries.toList()) {
            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                RecyclerView.ViewHolder {
                    return RecyclerView.ViewHolder(view)
                }
            }

            override fun getLayoutId(position: Int, obj: EditTextAdapterEntry): Int {
                return R.layout.value_input_row
            }

        }
        recyclerView.adapter = adapter

        val view = LinearLayout(this)
        view.orientation = LinearLayout.VERTICAL
        view.addView(recyclerView)

        val tv = TextView(this)
        tv.textSize = 24.0f
        tv.text = "Cross ID: $crossId"
        val tv2 = TextView(this)
        tv2.textSize = 24.0f
        tv2.text = "Timestamp: " + mTimestamp!!
        view.addView(tv)
        view.addView(tv2)

        val submitButton = Button(this)// = entry.editText;
        submitButton.text = "Update"
        submitButton.setOnClickListener {
            var next = 0

            for (header in headers) {
                db.execSQL("UPDATE INTERCROSS SET " +
                        header + " = '" + editTexts[next++].text.toString() + //adapter.getItem(next++).editText.getText().toString() + //entries.get(next++).editText.getText() +

                        "' WHERE cross_id = '" + crossId + "'")
            }
        }

        view.addView(submitButton)
        setContentView(view)

    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.drawer_print_layout, m)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_print ->

                object : AsyncTask<Void, Void, Void>() {

                    override fun doInBackground(voids: Array<Void>): Void? {
                        var mBluetoothAdapter: BluetoothAdapter? = null
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                        val pairedDevices = mBluetoothAdapter!!.bondedDevices

                        //TODO allow multiple pairs
                        //TODO wrap in async task
                        if (pairedDevices.size == 1) {
                            val bd = pairedDevices.toTypedArray<BluetoothDevice>()[0]
                            Log.d("BT", "PAIRED")
                            val bc = BluetoothConnection(bd.getAddress())
                            try {
                                bc.open()
                                val printer = ZebraPrinterFactory.getInstance(bc)
                                val linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer)
                                val printerStatus = if (linkOsPrinter != null) linkOsPrinter.currentStatus else printer.currentStatus
                                getPrinterStatus(bc)
                                if (printerStatus.isReadyToPrint) {
                                    runOnUiThread { Toast.makeText(this@AuxValueInputActivity, "Printer Ready", Toast.LENGTH_LONG).show() }

                                    //printer.sendCommand("! DF RUN.BAT ! UTILITIES JOURNAL SETFF 50 5 PRINT");
                                    //printer.printConfigurationLabel();
                                    //printer.sendCommand("^XA^FO0,0^ADN,36,20^FDCHANEY^FS^XZ");
                                    printer.sendCommand("^XA"
                                            + "^FWR"
                                            + "^FO100,75^A0,25,20^FD" + mCrossId + "^FS"
                                            + "^FO200,75^A0N,25,20"
                                            + "^BQN,2,10^FDMA" + mCrossId + "^FS"
                                            + "^FO450,75^A0,25,20^FD" + mTimestamp + "^FS^XZ")
                                    /*printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.intercross_small)), 75,500,-1,-1,false);*/

                                } else if (printerStatus.isHeadOpen) {
                                    //helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print");
                                } else if (printerStatus.isPaused) {
                                    //helper.showErrorMessage("Error: Printer Paused");
                                } else if (printerStatus.isPaperOut) {
                                    //helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print");
                                } else {
                                    //helper.showErrorMessage("Error: Please check the Connection of the Printer");
                                }

                                bc.close()

                            } catch (e: ConnectionException) {
                                e.printStackTrace()
                            } catch (e: ZebraPrinterLanguageUnknownException) {
                                e.printStackTrace()
                            }

                            return null
                        }
                        return null
                    }
                }.execute()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    @Throws(ConnectionException::class)
    private fun getPrinterStatus(connection: BluetoothConnection) {


        val printerLanguage = SGD.GET("device.languages", connection) //This command is used to get the language of the printer.

        val displayPrinterLanguage = "Printer Language is $printerLanguage"

        SGD.SET("device.languages", "zpl", connection) //This command set the language of the printer to ZPL

        this@AuxValueInputActivity.runOnUiThread {
            Toast.makeText(this@AuxValueInputActivity,
                    "$displayPrinterLanguage\nLanguage set to ZPL", Toast.LENGTH_LONG).show()
        }

    }

    companion object {

        private val line_separator = System.getProperty("line.separator")
    }

}
