package org.phenoapps.intercross

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.transition.Explode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.IntercrossActivity.Companion.COL_ID_KEY
import org.phenoapps.intercross.IntercrossActivity.Companion.CROSS_ID
import org.phenoapps.intercross.IntercrossActivity.Companion.FEMALE_PARENT
import org.phenoapps.intercross.IntercrossActivity.Companion.MALE_PARENT
import org.phenoapps.intercross.IntercrossActivity.Companion.PERSON
import org.phenoapps.intercross.IntercrossActivity.Companion.TIMESTAMP

class CrossActivity : AppCompatActivity() {

    private val mParentsRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parentsRecyclerView)
    }
    private val mChildrenRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.childrenRecyclerView)
    }

    private val mParentEntries = ArrayList<AdapterEntry>()
    private val mChildrenEntries =  ArrayList<AdapterEntry>()

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
                exitTransition  = Explode()
            }
        } else {
            // Swap without transition
        }

        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_cross)

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val id = intent.getIntExtra(COL_ID_KEY, -1)
        mCrossId = intent.getStringExtra(CROSS_ID) ?: ""

        mTimestamp = mDbHelper.getTimestampById(id)
        mPerson = mDbHelper.getPersonById(id)

        val polType = mDbHelper.getPollinationType(id)

        findViewById<ImageView>(R.id.crossTypeImageView)
                .setImageDrawable(when(polType) {
                    "Self-Pollinated" -> ContextCompat.getDrawable(this,
                            R.drawable.ic_human_female)
                    "Biparental" -> ContextCompat.getDrawable(this,
                            R.drawable.ic_human_male_female)
                    else -> ContextCompat.getDrawable(this,
                            R.drawable.ic_human_female_female)
                })
        findViewById<TextView>(R.id.crossTextView)
                .setText(mCrossId)
        findViewById<TextView>(R.id.dateTextView)
                .setText("$mTimestamp")

        findViewById<ImageView>(R.id.deleteView).setOnClickListener {
            if (mDbHelper.getRowId(mCrossId) != -1) {
                val builder = AlertDialog.Builder(this@CrossActivity).apply {

                    setTitle("Delete cross entry?")

                    setNegativeButton("Cancel") { _, _ -> }

                    setPositiveButton("Yes") { _, _ ->
                        mDbHelper.deleteEntry(id)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAfterTransition()
                        } else finish()
                    }
                }
                builder.show()
            }
        }

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
                return R.layout.row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view)
            }
        }

        mChildrenRecyclerView.adapter = childrenAdapter

        val parentAdapter = object : ViewAdapter<AdapterEntry>(mParentEntries) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.row
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
            itemView.findViewById<TextView>(R.id.crossTextView)
        }
        private val dateView: TextView by lazy {
            itemView.findViewById<TextView>(R.id.dateTextView)
        }
        private val deleteView: ImageView by lazy {
            itemView.findViewById<ImageView>(R.id.deleteView)
        }
        private var mEntry: AdapterEntry = AdapterEntry()

        init {
            itemView.setOnClickListener {
                startCrossActivity(firstView.text.toString())
            }
            deleteView.setOnClickListener {
                val id = mDbHelper.getRowId(firstView.text.toString())
                if (id != -1) {
                    val builder = AlertDialog.Builder(this@CrossActivity).apply {

                        setTitle("Delete cross entry?")

                        setNegativeButton("Cancel") { _, _ -> }

                        setPositiveButton("Yes") { _, _ ->
                            mDbHelper.deleteEntry(id)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                finishAfterTransition()
                            } else finish()
                        }
                    }
                    builder.show()
                }
            }
        }

        override fun bind(data: AdapterEntry) {
            mEntry = data
            firstView.setText(data.first)
            val date = mDbHelper.getTimestampById(
                    mDbHelper.getRowId(data.first))
            if (dateView.text.toString() == "-1") dateView.setText("No record.")
            else dateView.setText(date)
        }
    }

    private fun startCrossActivity(crossId: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this@CrossActivity)

        val id = mDbHelper.getRowId(crossId)
        val parents = mDbHelper.getParents(id)
        val timestamp = mDbHelper.getTimestampById(id)
        val intent = Intent(this@CrossActivity, CrossActivity::class.java)

        intent.putExtra(COL_ID_KEY, id)
        intent.putExtra(CROSS_ID, crossId)
        intent.putExtra(TIMESTAMP, timestamp)
        intent.putExtra(FEMALE_PARENT, parents[0])
        intent.putExtra(MALE_PARENT, parents[1])
        intent.putExtra(PERSON, pref.getString(SettingsActivity.PERSON, ""))

        startActivity(intent)
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
                    BluetoothUtil().variablePrint(
                        this@CrossActivity, mCode, "^XA"
                        + "^XFR:${mZplFileName}"
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
                finishAfterTransition()
            } else finish()
        }
        return true
    }
}
