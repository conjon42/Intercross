package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CrossActivity : AppCompatActivity() {

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

        setContentView(R.layout.activity_cross)

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
                    else Toast.makeText(this, "This id has no DB entry.", Toast.LENGTH_SHORT).show()
                }
                mBpFemaleTextView.setOnClickListener {
                    val femaleId = mDbHelper.getRowId(mFemaleParent)
                    if (femaleId != -1) startCrossActivity(mDbHelper.getRowId(mFemaleParent), mFemaleParent)
                    else Toast.makeText(this, "This id has no DB entry.", Toast.LENGTH_SHORT).show()
                }
            }
            "Open Pollinated", "Self-Pollinated" -> {
                mBpFemaleTextView.visibility = View.INVISIBLE
                mBpMaleTextView.visibility = View.INVISIBLE
                mNotBpParentTextView.visibility = View.VISIBLE
                mNotBpParentTextView.text = mFemaleParent
                mNotBpParentTextView.setOnClickListener {
                    val femaleId = mDbHelper.getRowId(mFemaleParent)
                    if (femaleId != -1) startCrossActivity(mDbHelper.getRowId(mFemaleParent), mFemaleParent)
                    else Toast.makeText(this, "This id has no DB entry.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val offspring = mDbHelper.getOffspring(id)

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        offspring.forEach { child ->
            mEntries.add(AdapterEntry(child.first, child.second))
        }

        /*val siblings = mDbHelper.getSiblings(id)

        mSiblingsRecyclerView.layoutManager = LinearLayoutManager(this)

        siblings.forEach {bro ->
            mSiblingsEntries.add(AdapterEntry())
        }*/

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

        val pref = PreferenceManager.getDefaultSharedPreferences(this@CrossActivity)

        val parents = mDbHelper.getParents(id)

        val timestamp = mDbHelper.getTimestampById(id)

        val intent = Intent(this@CrossActivity, CrossActivity::class.java)

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

                //if a template is saved in the preferences, use it, otherwise try the default print
                if (mCode.isNotBlank() && mZplFileName.isNotBlank()) {
                    BluetoothUtil(this@CrossActivity).variablePrint(
                        mCode, "^XA"
                        + "^XFR:${mZplFileName}"
                        + "^FN1^FD" + mCrossId + "^FS"
                        + "^FN2^FDQA," + mCrossId + "^FS"
                        + "^FN3^FD" + mTimestamp + "^FS^XZ")
                } else {
                    //uses bluetooth utility to send the default ZPL template and fields
                    BluetoothUtil(this@CrossActivity).variablePrint(
                "^XA"
                        + "^MNA"
                        + "^MMT,N"
                        + "^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS"
                        + "^FWR"
                        + "^FO100,25^A0,25,20^FN1^FS"
                        + "^FO200,25^A0N,25,20"
                        + "^BQ,2,6" +
                        "^FN2^FS"
                        + "^FO450,25^A0,25,20^FN3^FS^XZ",

                "^XA"
                        + "^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF"
                        + "^FN1^FD" + mCrossId + "^FS"
                        + "^FN2^FDQA," + mCrossId + "^FS"
                        + "^FN3^FD" + mTimestamp + "^FS^XZ")
                }
            }
            else -> finish()
        }
        return true
    }

    companion object {

        private val line_separator = System.getProperty("line.separator")
    }

}
