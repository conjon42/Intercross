package org.phenoapps.intercross

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import java.util.*
import kotlin.collections.HashMap

class CountActivity : AppCompatActivity() {

    private lateinit var mRecyclerView: RecyclerView

    private val mEntries = ArrayList<AdapterEntry>()

    private val mAdapter = object : ViewAdapter<AdapterEntry>(mEntries) {

        override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
            return R.layout.row
        }

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

    }

    private val mNameMap = HashMap<String, String>()

    private val mDbHelper = IdEntryDbHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_count)

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

        mDbHelper.getParentCounts().forEach { entry ->
            mEntries.add(entry)
        }

    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
            ViewAdapter.Binder<AdapterEntry> {

        private var firstText: TextView = itemView.findViewById(R.id.firstTextView) as TextView
        private var secondText: TextView = itemView.findViewById(R.id.secondTextView) as TextView

        override fun bind(data: AdapterEntry) {

            firstText.text = data.first
            secondText.text = data.second
        }
    }
}
