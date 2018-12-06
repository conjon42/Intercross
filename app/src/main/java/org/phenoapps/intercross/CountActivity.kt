package org.phenoapps.intercross

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import java.util.*
import kotlin.collections.HashMap

class CountActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            return R.layout.count_row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

    }

    private val mNameMap = HashMap<String, String>()

    private val mDbHelper = IdEntryDbHelper(this)

    private lateinit var mNavView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("Unique Crosses")

        setContentView(R.layout.activity_count)

        supportActionBar?.let {
            it.title = "Unique Crosses"
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView) as NavigationView

        // Setup drawer view
        setupDrawer()

        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mRecyclerView.adapter = mAdapter

        /*val keys = intent.getStringArrayExtra("NameMapKey")
        val values = intent.getStringArrayExtra("NameMapValue")

        if (keys.size == values.size) {
            for (i in keys.indices) {
                mNameMap[keys[i]] = values[i]
            }
        }*/

        var result = mDbHelper.getParentCounts()
        //sort
        result.sortBy { it.third }
        //remove dups
        //result.distinctBy {it.first + it.second}.
        result.asReversed().forEach {
            var unique = true
            mEntries.forEach { entry ->
                if (entry.first == it.first && entry.second == it.second) {
                    unique = false
                }
            }
            if (unique) mEntries.add(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                when (requestCode) {
                    IntercrossConstants.USER_INPUT_HEADERS_REQ -> {
                        mDbHelper.updateValues(intent.extras.getInt(IntercrossConstants.COL_ID_KEY).toString(),
                                intent.extras.getStringArrayList(IntercrossConstants.USER_INPUT_VALUES)
                        )
                    }
                }
            }
        }
    }
    inner class InnerViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
            ViewAdapter.Binder<AdapterEntry>, View.OnClickListener {

        override fun onClick(v: View?) {
            val crossName = (v as? TextView)?.text.toString()
            val id = mDbHelper.getRowId(crossName)
            val timestamp = mDbHelper.getTimestampById(id)
            val intent = Intent(this@CountActivity, AuxValueInputActivity::class.java)
            val headers = mDbHelper.getColumns() - IdEntryContract.IdEntry.COLUMNS.toList()
            val values = mDbHelper.getUserInputValues(id)
            val parents = mDbHelper.getParents(id)
            intent.putExtra(IntercrossConstants.COL_ID_KEY, id)
            intent.putExtra(IntercrossConstants.CROSS_ID, crossName)
            intent.putExtra(IntercrossConstants.TIMESTAMP, timestamp)
            intent.putExtra(IntercrossConstants.FEMALE_PARENT, parents[0])
            intent.putExtra(IntercrossConstants.MALE_PARENT, parents[1])
            intent.putStringArrayListExtra(IntercrossConstants.HEADERS, ArrayList(headers))
            intent.putStringArrayListExtra(IntercrossConstants.USER_INPUT_VALUES, ArrayList(values))
            startActivityForResult(intent, IntercrossConstants.USER_INPUT_HEADERS_REQ)
        }
        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView
        init {
            firstText.setOnClickListener(this)
        }
        override fun bind(data: AdapterEntry) {
            firstText.text = data.first
        }
    }

    private fun setupDrawer() {
        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            //else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
            ViewAdapter.Binder<AdapterEntry>, View.OnClickListener {

        private var firstText: TextView = itemView.findViewById(R.id.crossTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.secondTextView) as TextView
        private var countText: TextView = itemView.findViewById(R.id.thirdTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
            secondText.text = data.second
            countText.text = data.third
        }
        override fun onClick(v: View) {
            val builder = AlertDialog.Builder(this@CountActivity)
            builder.setTitle("List of children (click for more information)")
            val layout = RecyclerView(this@CountActivity)
            builder.setView(layout)
            val crosses = mDbHelper.getCrosses(firstText.text.toString(), secondText.text.toString())
            val entries = ArrayList<AdapterEntry>()
            crosses.forEach {
                entries.add(AdapterEntry(it, ""))
            }
            val adapter: ViewAdapter<AdapterEntry> = object : ViewAdapter<AdapterEntry>(entries) {
                override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                    return R.layout.row
                }

                override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                    return InnerViewHolder(view)
                }
            }
            layout.adapter = adapter
            layout.layoutManager = LinearLayoutManager(this@CountActivity)
            builder.show()
        }
    }
}
