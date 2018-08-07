package org.phenoapps.intercross

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.*
import java.util.*

class ManageHeadersActivity : AppCompatActivity() {

    private lateinit var mButton: Button
    private lateinit var mEditText: EditText
    private lateinit var mRecyclerView: RecyclerView

    private val mEntries = ArrayList<String>()

    private val mAdapter: ViewAdapter<String> = object : ViewAdapter<String>(mEntries) {

        override fun getViewHolder(view: View, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(view)
        }

        override fun getLayoutId(position: Int, obj: String): Int {
            return R.layout.row
        }
    }

    private val mDbHelper: IdEntryDbHelper = IdEntryDbHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_headers)

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val headerList = sharedPref.getStringSet(SettingsActivity.HEADER_SET, HashSet())

        if (headerList.isNotEmpty()) {

            headerList.forEach { header ->
                header?.let {
                    mEntries.add(header)
                }
            }
        }

        mButton = findViewById(R.id.addHeaderButton) as Button
        mEditText = findViewById(R.id.editTextHeader) as EditText
        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView

        mRecyclerView.adapter = mAdapter

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        mButton.setOnClickListener {

            val newHeaderName = mEditText.text.toString()

            if (!mEntries.contains(newHeaderName)) {

                mEntries.add(newHeaderName)

                val editor = sharedPref.edit()

                editor.putStringSet(SettingsActivity.HEADER_SET, HashSet(mEntries))

                editor.apply()

                val db = mDbHelper.writableDatabase

                db.execSQL("ALTER TABLE ${IdEntryContract.IdEntry.TABLE_NAME} " +
                        "ADD COLUMN $newHeaderName TEXT DEFAULT '';")

                db.close()

                mRecyclerView.adapter.notifyDataSetChanged()

                mEditText.setText("")

            } else {
                Toast.makeText(this@ManageHeadersActivity,
                        "Header already exists in table.", Toast.LENGTH_LONG).show()
            }
        }
    }


    inner class ViewHolder internal constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView), ViewAdapter.Binder<String>,
            View.OnClickListener {

        private var firstText: TextView = itemView.findViewById(R.id.firstTextView) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        override fun bind(data: String) {
            firstText.text = data
        }

        override fun onClick(v: View?) {

            var db = mDbHelper.readableDatabase
            var currentHeaders: Array<String>? = null
            try {
                val cursor = db.query(IdEntryContract.IdEntry.TABLE_NAME,
                        null, null, null,
                        null, null, null)

                currentHeaders = cursor.columnNames

                cursor.close()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }

            db.close()

            val removedCol = ((v as LinearLayout).getChildAt(0) as TextView).text

            val updatedHeaders = ArrayList<String>()

            currentHeaders?.let {
                currentHeaders.forEach { header ->
                    when (header != removedCol) {
                        true -> updatedHeaders.add(header)
                    }
                }
            }

            val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

            val headerSet = prefs.getStringSet(SettingsActivity.HEADER_SET, HashSet())

            headerSet.remove(removedCol)

            val editor = prefs.edit()

            editor.putStringSet(SettingsActivity.HEADER_SET, headerSet)

            editor.apply()

            db = mDbHelper.writableDatabase

            db.execSQL("DROP TABLE IF EXISTS ${IdEntryContract.IdEntry.TABLE_NAME}_OLD")

            db.execSQL("ALTER TABLE ${IdEntryContract.IdEntry.TABLE_NAME} " +
                    "RENAME TO ${IdEntryContract.IdEntry.TABLE_NAME}_OLD")

            var sqlCreateEntries = "CREATE TABLE ${IdEntryContract.IdEntry.TABLE_NAME}( "

            when (updatedHeaders.size) {
                0 -> sqlCreateEntries += ");"
                else -> {
                    updatedHeaders.forEach { header ->
                        sqlCreateEntries += header
                        when (header) {
                            updatedHeaders.last() -> sqlCreateEntries += " TEXT);"
                            else -> sqlCreateEntries += " TEXT, "
                        }
                    }
                }
            }

            Log.d("CREATE", sqlCreateEntries)
            db.execSQL(sqlCreateEntries)

            var sqlInsert = "INSERT INTO ${IdEntryContract.IdEntry.TABLE_NAME} ("

            when (updatedHeaders.size) {
                0 -> sqlInsert += ")"
                else -> {
                    updatedHeaders.forEach { header ->

                        sqlInsert += header
                        when (header == updatedHeaders.last()) {
                            true -> sqlInsert += ")"
                            false -> sqlInsert += ","
                        }

                    }
                }
            }

            sqlInsert += "SELECT "

            when (updatedHeaders.size != 0) {
                true -> updatedHeaders.forEach { header ->
                    sqlInsert += header
                    when (header == updatedHeaders.last()) {
                        false -> sqlInsert += ","
                        else -> sqlInsert +=
                                " FROM ${IdEntryContract.IdEntry.TABLE_NAME}_OLD;"
                    }
                }
            }

            Log.d("INSERT", sqlInsert)

            db.execSQL(sqlInsert)

            mEntries.remove(removedCol)

            mAdapter.notifyDataSetChanged()
        }
    }

}
