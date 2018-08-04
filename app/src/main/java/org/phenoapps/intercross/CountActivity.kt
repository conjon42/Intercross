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
import android.widget.Adapter
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

        val crossIds = ArrayList<AdapterEntry>()

        val db = mDbHelper.readableDatabase
        try {
            val cursor = db.query( IdEntryContract.IdEntry.TABLE_NAME, arrayOf("cross_id", "male", "female"),
                    null, null, null, null, null)

            val entry = AdapterEntry()
            var male: String? = null
            var female: String? = null
            if (cursor.moveToFirst()) {
                do {
                    cursor.columnNames.forEach { header ->
                        header?.let {

                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(it)) ?: String()

                            when (it) {
                                "male" -> male = colVal
                                "female" -> female = colVal
                                "cross_id" -> entry.first = colVal
                            }
                        }
                    }

                    val countCursor = db.query(IdEntryContract.IdEntry.TABLE_NAME, arrayOf("male", "female"),
                            "male=?, female=?", arrayOf(male, female), null, null, null)

                    entry.second = countCursor.columnCount.toString()

                    countCursor.close()

                    crossIds.add(entry)

                } while (cursor.moveToNext())
            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        db.close()

        crossIds.clear()

        buildListView(crossIds)

    }

    inner class ViewHolder internal constructor(itemView: RecyclerView) : RecyclerView.ViewHolder(itemView) {
        internal var firstText: TextView
        internal var secondText: TextView

        init {
            firstText = itemView.findViewById(R.id.firstTextView) as TextView
            secondText = itemView.findViewById(R.id.secondTextView) as TextView
        }
    }

    private fun buildListView(crossIds: ArrayList<AdapterEntry>) {

        val recyclerView = findViewById(R.id.crossList) as RecyclerView

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = object : ViewAdapter<AdapterEntry>(crossIds) {

            override fun getLayoutId(position: Int, obj: AdapterEntry): Int {
                return R.layout.row
            }

            override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
                return ViewHolder(view as RecyclerView)
            }

        }

        recyclerView.adapter = adapter
    }
}
