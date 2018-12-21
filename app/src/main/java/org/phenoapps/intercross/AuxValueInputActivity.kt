package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import org.w3c.dom.Text

//TODO create separate file for async bluetooth task
class AuxValueInputActivity : AppCompatActivity() {

    private val mIdTextView: TextView by lazy {
        findViewById<TextView>(R.id.selfTextView)
    }
    private val mNotBpParentTextView: TextView by lazy {
        findViewById<TextView>(R.id.nbpTextView)
    }
    private val mDateTextView: TextView by lazy {
        findViewById<TextView>(R.id.dateTextView)
    }
    private val mPolTypeTextView: TextView by lazy {
        findViewById<TextView>(R.id.polTypeTextView)
    }
    private val mBpMaleTextView: TextView by lazy {
        findViewById<TextView>(R.id.bpMaleTextView)
    }
    private val mBpFemaleTextView: TextView by lazy {
        findViewById<TextView>(R.id.bpFemaleTextView)
    }
    private val mPersonTextView: TextView by lazy {
        findViewById<TextView>(R.id.personTextView)
    }
    private val mRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.offspringRecyclerView)
    }

    private val mEntries = ArrayList<AdapterEntry>()

    private val mDbHelper = IdEntryDbHelper(this)

    private var mMaleParent = String()
    private var mFemaleParent = String()
    private var mCrossId = String()
    private var mTimestamp = String()
    private var mPerson = String()

    private var mZplFileName = String()
    private val mCode: String by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
                .getString("ZPL_CODE", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manage_values)

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val id = intent.getIntExtra(IntercrossConstants.COL_ID_KEY, -1)
        mCrossId = intent.getStringExtra(IntercrossConstants.CROSS_ID) ?: ""
        mMaleParent = intent.getStringExtra(IntercrossConstants.MALE_PARENT) ?: ""
        mFemaleParent = intent.getStringExtra(IntercrossConstants.FEMALE_PARENT) ?: ""
        mTimestamp = mDbHelper.getTimestampById(id)
        mPerson = mDbHelper.getPersonById(id)

        mIdTextView.text = "Cross ID: $mCrossId"
        mDateTextView.text = "$mTimestamp"
        mPersonTextView.text = "by $mPerson"

        if (mCode.isNotBlank()) {
            if (mCode.contains("DFR:")) {
                val split = mCode.split("DFR:")
                if (split.size > 1) {
                    val second = split[1].split("^FS")
                    if (second.size > 1) {
                        mZplFileName = second[0]
                    }
                }
            }
        }

        val polType = mDbHelper.getPollinationType(id)
        mPolTypeTextView.text = polType

        when (polType) {
            "Biparental" -> {
                mBpFemaleTextView.visibility = View.VISIBLE
                mBpMaleTextView.visibility = View.VISIBLE
                mNotBpParentTextView.visibility = View.INVISIBLE
                mBpMaleTextView.text = mMaleParent
                mBpFemaleTextView.text = mFemaleParent
                mBpMaleTextView.setOnClickListener {
                    val maleId = mDbHelper.getRowId(mMaleParent)
                    if (maleId != -1) startCrossActivity(id, mMaleParent)
                    else Toast.makeText(this, "This id has not DB entry.", Toast.LENGTH_SHORT).show()
                }
                mBpFemaleTextView.setOnClickListener {
                    val femaleId = mDbHelper.getRowId(mFemaleParent)
                    if (femaleId != -1) startCrossActivity(mDbHelper.getRowId(mFemaleParent), mFemaleParent)
                    else Toast.makeText(this, "This id has not DB entry.", Toast.LENGTH_SHORT).show()
                }
            }
            "OpenPollinated", "SelfPollinated" -> {
                mBpFemaleTextView.visibility = View.INVISIBLE
                mBpMaleTextView.visibility = View.INVISIBLE
                mNotBpParentTextView.visibility = View.VISIBLE
                mNotBpParentTextView.text = mFemaleParent
                mNotBpParentTextView.setOnClickListener {
                    val femaleId = mDbHelper.getRowId(mFemaleParent)
                    if (femaleId != -1) startCrossActivity(mDbHelper.getRowId(mFemaleParent), mFemaleParent)
                    else Toast.makeText(this, "This id has not DB entry.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val offspring = mDbHelper.getOffspring(id)

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        offspring.forEach { child ->
            mEntries.add(AdapterEntry(child.first, child.second))
        }

        val adapter = object : ViewAdapter<AdapterEntry>(mEntries) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view)
            }
        }

        mRecyclerView.adapter = adapter
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView

        private var mEntry: AdapterEntry = AdapterEntry()

        init {
            firstText.setOnClickListener {
                startCrossActivity(mEntry.second.toInt(), mEntry.first)
            }
        }

        override fun bind(data: AdapterEntry) {
            mEntry = data
            firstText.text = data.first
        }
    }

    private fun startCrossActivity(id: Int, crossId: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this@AuxValueInputActivity)

        val parents = mDbHelper.getParents(id)

        val timestamp = mDbHelper.getTimestampById(id)

        val intent = Intent(this@AuxValueInputActivity, AuxValueInputActivity::class.java)

        intent.putExtra(IntercrossConstants.COL_ID_KEY, id)

        intent.putExtra(IntercrossConstants.CROSS_ID, crossId)

        intent.putExtra(IntercrossConstants.TIMESTAMP, timestamp)

        intent.putExtra(IntercrossConstants.FEMALE_PARENT, parents[0])

        intent.putExtra(IntercrossConstants.MALE_PARENT, parents[1])

        intent.putExtra(IntercrossConstants.PERSON, pref.getString(SettingsActivity.PERSON, ""))

        startActivityForResult(intent, IntercrossConstants.USER_INPUT_HEADERS_REQ)
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

                        val pref = PreferenceManager.getDefaultSharedPreferences(this@AuxValueInputActivity)
                        val btId = pref.getString(SettingsActivity.BT_ID, "")

                        if (btId.isBlank()) {
                            run { Toast.makeText(this@AuxValueInputActivity,
                                    "No bluetooth device paired.", Toast.LENGTH_SHORT) }
                        } else {
                            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                            val pairedDevices = mBluetoothAdapter.bondedDevices.filter {
                                it.name == btId
                            }

                            if (pairedDevices.isNotEmpty()) {
                                val bc = BluetoothConnection(pairedDevices[0].address)

                                try {
                                    bc.open()
                                    val printer = ZebraPrinterFactory.getInstance(bc)
                                    val linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer)
                                    linkOsPrinter?.let {
                                        val printerStatus = it.currentStatus
                                        getPrinterStatus(bc)
                                        if (printerStatus.isReadyToPrint) {

                                            if (mCode.isNotBlank() && mZplFileName.isNotBlank()) {
                                                printer.sendCommand(mCode)
                                                printer.sendCommand("^XA"
                                                        + "^XFR:${mZplFileName}"
                                                        + "^FN1^FD" + mCrossId + "^FS"
                                                        + "^FN2^FDQA," + mCrossId + "^FS"
                                                        + "^FN3^FD" + mTimestamp + "^FS^XZ")
                                            } else {
                                                printer.sendCommand("^XA"
                                                        + "^MNA"
                                                        + "^MMT,N"
                                                        + "^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS"
                                                        + "^FWR"
                                                        + "^FO100,25^A0,25,20^FN1^FS"
                                                        + "^FO200,25^A0N,25,20"
                                                        + "^BQ,2,6" +
                                                        "^FN2^FS"
                                                        + "^FO450,25^A0,25,20^FN3^FS^XZ")

                                                printer.sendCommand("^XA"
                                                        + "^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF"
                                                        + "^FN1^FD" + mCrossId + "^FS"
                                                        + "^FN2^FDQA," + mCrossId + "^FS"
                                                        + "^FN3^FD" + mTimestamp + "^FS^XZ")
                                            }
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
                        }

                        return String()
                    }

                }.execute()


            }
            else -> finish()
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
