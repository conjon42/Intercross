package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException

//TODO create separate file for async bluetooth task
//TODO create bitmap preview of barcode print
class AuxValueInputActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mIdTextView: TextView
    private lateinit var mTimeTextView: TextView
    private lateinit var mUpdateButton: Button

    private val mEntries = ArrayList<AdapterEntry>()
    private var mCrossId = String()
    private var mTimestamp = String()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manage_values)

        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView
        mIdTextView = findViewById(R.id.textView2) as TextView
        mTimeTextView = findViewById(R.id.textView3) as TextView
        mUpdateButton = findViewById(R.id.button) as Button

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val id = intent.getIntExtra(IntercrossConstants.COL_ID_KEY, -1)
        mCrossId = intent.getStringExtra(IntercrossConstants.CROSS_ID) ?: ""
        mTimestamp = intent.getStringExtra(IntercrossConstants.TIMESTAMP) ?: ""
        val headers = intent.getStringArrayListExtra(IntercrossConstants.HEADERS)
        val values = intent.getStringArrayListExtra(IntercrossConstants.USER_INPUT_VALUES)

        headers.forEachIndexed { index, header ->
            mEntries.add(AdapterEntry(header, values[index] ?: ""))
        }

        val adapter = object : ViewAdapter<AdapterEntry>(mEntries) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.value_input_row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view)
            }
        }

        mRecyclerView.adapter = adapter

        mIdTextView.text = "Cross ID: $mCrossId"
        mTimeTextView.text = "Timestamp: $mTimestamp"

        mUpdateButton.setOnClickListener { _ ->

            val updatedValues = ArrayList<String>()
            val intent = Intent()

            mEntries.forEach { entry ->
                updatedValues.add(entry.second)
            }

            intent.putExtra(IntercrossConstants.USER_INPUT_VALUES, updatedValues)
            intent.putExtra(IntercrossConstants.COL_ID_KEY, id)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.firstView) as TextView
        private var secondText: EditText = itemView.findViewById(R.id.secondView) as EditText
        private var mEntry: AdapterEntry = AdapterEntry()

        init {
            secondText.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable?) {
                    mEntries[mEntries.indexOf(mEntry)].second = secondText.text.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

        override fun bind(data: AdapterEntry) {
            mEntry = data
            firstText.text = data.first
            secondText.setText(data.second)
        }
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
