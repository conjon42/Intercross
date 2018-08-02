package org.phenoapps.intercross

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import java.util.ArrayList
import java.util.HashMap

class CountActivity : AppCompatActivity() {

    private var mCrossIds: ArrayList<AdapterEntry>? = null

    private var mNameMap: MutableMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count)


        val mDbHelper = IdEntryDbHelper(this)

        mNameMap = HashMap()

        val keys = intent.getStringArrayExtra("NameMapKey")
        val values = intent.getStringArrayExtra("NameMapValue")

        if (keys.size == values.size) {
            for (i in keys.indices) {
                mNameMap!![keys[i]] = values[i]
            }
        }

        mCrossIds = ArrayList()

        val db = mDbHelper.readableDatabase

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


                        val entry = AdapterEntry(crossId,
                                countCursor.count.toString(), mNameMap!!.get(crossId))

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

    private fun buildListView() {

        val recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CountRecyclerViewAdapter(this, mCrossIds)
        recyclerView.adapter = adapter
    }

    class CountRecyclerViewAdapter internal constructor(private val mContext: Context, private val mData: List<AdapterEntry>) : RecyclerView.Adapter<CountRecyclerViewAdapter.ViewHolder>() {

        inner class AdapterEntry internal constructor(var crossId: String, var timestamp: String, var crossName: String)

        private val mInflater: LayoutInflater
        private val mDbHelper: IdEntryDbHelper

        init {
            this.mInflater = LayoutInflater.from(mContext)
            mDbHelper = IdEntryDbHelper(mContext)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountRecyclerViewAdapter.ViewHolder {
            val view = mInflater.inflate(R.layout.row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: CountRecyclerViewAdapter.ViewHolder, position: Int) {
            val entry = mData[position]
            if (entry.crossName == null || entry.crossName.length == 0)
                holder.crossView.text = entry.crossId
            else
                holder.crossView.text = entry.crossName
            holder.countView.text = entry.timestamp
        }

        override fun getItemCount(): Int {
            return mData.size
        }


        inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
            internal var crossView: TextView
            internal var countView: TextView
            internal var crossName: String

            init {
                val childCount = (itemView as LinearLayout).childCount
                crossName = (itemView.getChildAt(0) as TextView).text.toString()
                crossView = itemView.findViewById(R.id.crossEntryId) as TextView
                crossView.setSingleLine()
                crossView.ellipsize = TextUtils.TruncateAt.END
                countView = itemView.findViewById(R.id.timestamp) as TextView
                countView.setSingleLine()
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View) {

            }
        }
    }
}
