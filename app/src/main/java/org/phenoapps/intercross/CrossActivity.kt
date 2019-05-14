package org.phenoapps.intercross

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.text.InputType
import android.transition.Explode
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.IntercrossActivity.Companion.CROSS_ID
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CrossActivity : AppCompatActivity() {

    private val mParentsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parentsRecyclerView)
    }
    private val mChildrenRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.childrenRecyclerView)
    }

    private val mParentEntries = ArrayList<AdapterEntry>()
    private val mChildrenEntries = ArrayList<AdapterEntry>()

    private lateinit var mRow: IntercrossDbContract.Columns

    private val mDbHelper = IntercrossDbHelper(this)

    private var mCrossId = String()
    private var mTimestamp = String()
    private var mPerson = String()

    private var mZplFileName = String()

    private val mCode: String by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
                .getString("ZPL_CODE", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                exitTransition = Explode()
            }
        } else {
            // Swap without transition
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cross)

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        //dynamically set transition name for the shared element transition
        val crossEntry = findViewById<View>(R.id.crossEntry)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            crossEntry.transitionName = "cross"
        }

        mCrossId = intent.getStringExtra(CROSS_ID) ?: ""

        val id = mDbHelper.getRowId(mCrossId)

        mDbHelper.getRow(id)?.let {
            mRow = it
        }

        mTimestamp = mDbHelper.getTimestampById(id)
        mPerson = mDbHelper.getPersonById(id)

        (crossEntry.findViewById(R.id.personTextView) as TextView).apply {
            visibility = View.VISIBLE
            text = "$mPerson\n${mRow.note}"
        }

        val polType = mDbHelper.getPollinationType(id)
        findViewById<ImageView>(R.id.crossTypeImageView)
                .setImageDrawable(when (polType) {
                    "Self-Pollinated" -> ContextCompat.getDrawable(this,
                            R.drawable.ic_cross_self)
                    "Biparental" -> ContextCompat.getDrawable(this,
                            R.drawable.ic_cross_biparental)
                    else -> ContextCompat.getDrawable(this,
                            R.drawable.ic_cross_open_pollinated)
                })
        findViewById<TextView>(R.id.maleTextView).text = mCrossId
        findViewById<TextView>(R.id.dateTextView).text = mTimestamp
        if (mTimestamp == "-1") findViewById<TextView>(R.id.dateTextView).text = ""

        val offspring = mDbHelper.getOffspring(id)

        mChildrenRecyclerView.layoutManager = LinearLayoutManager(this)

        offspring.forEach { child ->
            mChildrenEntries.add(AdapterEntry(child.first))
        }

        val parents = mDbHelper.getParents(id)

        mParentsRecyclerView.layoutManager = LinearLayoutManager(this)

        parents.forEach { parent ->
            if (parent.isNotBlank()) mParentEntries.add(AdapterEntry(parent))
        }

        /*val siblings = mDbHelper.getSiblings(id)

        mSiblingsRecyclerView.layoutManager = LinearLayoutManager(this)

        siblings.forEach {bro ->
            mSiblingsEntries.add(AdapterEntry())
        }*/

        val childrenAdapter = object : ViewAdapter<AdapterEntry>(mChildrenEntries) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.simple_row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view)
            }
        }

        mChildrenRecyclerView.adapter = childrenAdapter

        val parentAdapter = object : ViewAdapter<AdapterEntry>(mParentEntries) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.simple_row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view)
            }
        }

        mParentsRecyclerView.adapter = parentAdapter
    }

    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<AdapterEntry> {

        private val firstView: TextView by lazy {
            itemView.findViewById(R.id.maleTextView) as TextView
        }
        private var mEntry: AdapterEntry = AdapterEntry()

        init {
            itemView.setOnClickListener {
                val cross = firstView.text.toString()
                if (-1 != mDbHelper.getRowId(cross))
                    startActivity(Intent(this@CrossActivity, CrossActivity::class.java).apply {
                        putExtra(CROSS_ID, firstView.text.toString())
                    })
                else Toast.makeText(this@CrossActivity, "Entry not in DB.", Toast.LENGTH_SHORT).show()
            }
        }

        override fun bind(data: AdapterEntry) {
            mEntry = data
            firstView.text = data.first
        }
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.drawer_print_layout, m)
        return true
    }

    private fun askForNote() {

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val builder = AlertDialog.Builder(this).apply {

            setView(input)

            setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                if (value.isNotEmpty()) {
                    val entry = ContentValues()
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS,mRow.cross)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS_COUNT, mRow.crossCount)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS_NAME, mRow.crossName)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_DATE, mRow.date)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_FEMALE, mRow.female)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_ID, mRow.id)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_LOCATION, mRow.location)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_MALE, mRow.male)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_NOTE, value)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_POLLINATION_TYPE, mRow.polType)
                    entry.put(IntercrossDbContract.IdEntry.COLUMN_NAME_USER, mRow.user)

                    mDbHelper.updateNote(entry)

                    val crossEntry = findViewById<View>(R.id.crossEntry)

                    (crossEntry.findViewById(R.id.personTextView) as TextView).apply {
                        visibility = View.VISIBLE
                        text = "$mPerson\n$value"
                    }
                }
            }
        }

        builder.setTitle("Add a note for this cross.")
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_note -> {
                askForNote()
            }
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
                    BluetoothUtil().variablePrint(
                            this@CrossActivity, mCode, "^XA"
                            + "^XFR:$mZplFileName"
                            + "^FN1^FD" + mCrossId + "^FS"
                            + "^FN2^FDQA," + mCrossId + "^FS"
                            + "^FN3^FD" + mTimestamp + "^FS"
                            + "^FN4^FD$mPerson^FS^XZ")
                } else {
                    //uses bluetooth utility to send the default ZPL template and fields
                    BluetoothUtil().variablePrint(this@CrossActivity,
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
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                supportFinishAfterTransition()
            } else finish()
        }
        return true
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition()
        } else finish()
    }
}
