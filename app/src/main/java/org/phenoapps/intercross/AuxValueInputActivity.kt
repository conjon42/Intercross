package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var mMaleParent = String()
    private var mFemaleParent = String()
    private val mEntries = ArrayList<AdapterEntry>()
    private var mCrossId = String()
    private var mTimestamp = String()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manage_values)

        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView
        mIdTextView = findViewById(R.id.textView2) as TextView
        mTimeTextView = findViewById(R.id.textView3) as TextView
        //mUpdateButton = findViewById(R.id.button) as Button

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val id = intent.getIntExtra(IntercrossConstants.COL_ID_KEY, -1)
        mCrossId = intent.getStringExtra(IntercrossConstants.CROSS_ID) ?: ""
        mMaleParent = intent.getStringExtra(IntercrossConstants.MALE_PARENT) ?: ""
        mFemaleParent = intent.getStringExtra(IntercrossConstants.FEMALE_PARENT) ?: ""
        mTimestamp = intent.getStringExtra(IntercrossConstants.TIMESTAMP) ?: ""
        val headers = intent.getStringArrayListExtra(IntercrossConstants.HEADERS)
        val values = intent.getStringArrayListExtra(IntercrossConstants.USER_INPUT_VALUES)

        findViewById<TextView>(R.id.maleParentTextView).text = "M: $mMaleParent"
        findViewById<TextView>(R.id.femaleParentTextView).text = "F: $mFemaleParent"

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

        /*mUpdateButton.setOnClickListener { _ ->

            val updatedValues = ArrayList<String>()
            val intent = Intent()

            mEntries.forEach { entry ->
                updatedValues.add(entry.second)
            }

            intent.putExtra(IntercrossConstants.USER_INPUT_VALUES, updatedValues)
            intent.putExtra(IntercrossConstants.COL_ID_KEY, id)
            setResult(RESULT_OK, intent)
            finish()
        }*/
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
            R.id.action_print -> {

                object : AsyncTask<Void, Void, String>() {

                    override fun doInBackground(vararg params: Void?): String {

                        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return String()

                        val pairedDevices = mBluetoothAdapter.bondedDevices

                        val builder = AlertDialog.Builder(this@AuxValueInputActivity)

                        builder.setTitle("Choose bluetooth device to print from.")

                        val input = RadioGroup(this@AuxValueInputActivity)

                        pairedDevices.forEach {
                            val button = RadioButton(this@AuxValueInputActivity)
                            button.text = it.name
                            input.addView(button)
                        }

                        builder.setView(input)

                        builder.setPositiveButton("OK") { dialog, which ->
                            if (input.checkedRadioButtonId == -1) return@setPositiveButton
                            val value = input.findViewById(input.checkedRadioButtonId) as RadioButton
                            val bc = BluetoothConnection(pairedDevices.toTypedArray()[input.indexOfChild(value)].address)

                            try {
                                bc.open()
                                val printer = ZebraPrinterFactory.getInstance(bc)
                                val linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer)
                                linkOsPrinter?.let {
                                    val printerStatus = it.currentStatus
                                    getPrinterStatus(bc)
                                    if (printerStatus.isReadyToPrint) {

                                        printer.sendCommand("^XA"
                                                + "^FWR"
                                                + "^FO100,75^A0,25,20^FD" + mCrossId + "^FS"
                                                + "^FO200,75^A0N,25,20"
                                                + "^BQN,2,6" +
                                                "^FDQA," + mCrossId + "^FS"
                                                + "^FO450,75^A0,25,20^FD" + mTimestamp + "^FS^XZ")
                                        /*printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                        R.drawable.intercross_small)), 75,500,-1,-1,false);*/

                                    } else if (printerStatus.isHeadOpen) {
                                        runOnUiThread { Toast.makeText(this@AuxValueInputActivity, "Printer is open.", Toast.LENGTH_LONG).show() }
                                    } else if (printerStatus.isPaused) {
                                        runOnUiThread { Toast.makeText(this@AuxValueInputActivity, "Printer is paused.", Toast.LENGTH_LONG).show() }
                                    } else if (printerStatus.isPaperOut) {
                                        runOnUiThread { Toast.makeText(this@AuxValueInputActivity, "No paper.", Toast.LENGTH_LONG).show() }
                                    } else {
                                        runOnUiThread { Toast.makeText(this@AuxValueInputActivity, "Please check the printer's connection.", Toast.LENGTH_LONG).show() }
                                    }
                                }
                            } catch (e: ConnectionException) {
                                e.printStackTrace()
                            } catch (e: ZebraPrinterLanguageUnknownException) {
                                e.printStackTrace()
                            } finally {
                                bc.close()
                            }
                        }

                        runOnUiThread { builder.show() }

                        return String()
                    }

                }.execute()


            }
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
