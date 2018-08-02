package org.phenoapps.intercross

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet

class ManageHeadersActivity : AppCompatActivity(), HeaderRecyclerViewAdapter.ItemClickListener {

    private var mHeaderIds: ArrayList<String>? = null

    private var mAdapter: HeaderRecyclerViewAdapter? = null

    private var mDbHelper: IdEntryDbHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_headers)

        mDbHelper = IdEntryDbHelper(this)

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val headerList = sharedPref.getStringSet(SettingsActivity.HEADER_SET, HashSet())

        mHeaderIds = ArrayList()

        if (!headerList.isEmpty()) {

            for (header in headerList) {
                mHeaderIds!!.add(header)
            }

            buildListView()

        }
        val headerInputButton = findViewById(R.id.addHeaderButton) as Button

        headerInputButton.setOnClickListener {
            val newHeaderName = (findViewById(R.id.editTextHeader) as EditText).text.toString()

            if (!mHeaderIds!!.contains(newHeaderName)) {

                mHeaderIds!!.add(newHeaderName)

                val editor = sharedPref.edit()

                editor.putStringSet(SettingsActivity.HEADER_SET, HashSet(mHeaderIds!!))

                editor.apply()

                buildListView()

                val db = mDbHelper!!.writableDatabase

                db.execSQL("ALTER TABLE INTERCROSS ADD COLUMN $newHeaderName TEXT DEFAULT '';")

                mDbHelper!!.onUpdateColumns(db, mHeaderIds!!.toTypedArray<String>())

                db.close()

            } else {
                Toast.makeText(this@ManageHeadersActivity,
                        "Header already exists in table.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buildListView() {

        val recyclerView = findViewById(R.id.listHeaders) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = HeaderRecyclerViewAdapter(this, mHeaderIds)
        mAdapter!!.setClickListener(this)
        recyclerView.adapter = mAdapter

    }

    override fun onItemClick(view: View, position: Int) {
        Log.d("CLICK", "C")

        var db = mDbHelper!!.readableDatabase
        var currentHeaders: Array<String>? = null
        try {
            val table = IdEntryContract.IdEntry.TABLE_NAME
            val cursor = db.query(table, null, null, null, null, null, null)

            if (cursor.moveToFirst()) {
                do {
                    currentHeaders = cursor.columnNames

                } while (cursor.moveToNext())

            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        db.close()

        val removedCol = mAdapter!!.getItem(position)

        val updatedHeaders = ArrayList<String>()
        for (i in currentHeaders!!.indices) {
            if (currentHeaders[i] != removedCol) {
                updatedHeaders.add(currentHeaders[i])
            }
        }

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val headerSet = prefs.getStringSet(SettingsActivity.HEADER_SET, HashSet())

        headerSet.remove(removedCol)

        val editor = prefs.edit()

        editor.putStringSet(SettingsActivity.HEADER_SET, headerSet)

        //editor.clear();

        editor.apply()

        db = mDbHelper!!.writableDatabase

        db.execSQL("DROP TABLE IF EXISTS INTERCROSS_OLD")

        db.execSQL("ALTER TABLE INTERCROSS RENAME TO INTERCROSS_OLD")

        var SQL_CREATE_ENTRIES = "CREATE TABLE INTERCROSS( "/* + IdEntryContract.IdEntry.COLUMN_NAME_MALE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_FEMALE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_CROSS + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_USER + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_DATE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_LOCATION + " TEXT "*/


        if (updatedHeaders.size == 0)
            SQL_CREATE_ENTRIES += ");"
        else {
            for (colName in updatedHeaders) {
                SQL_CREATE_ENTRIES += colName
                if (updatedHeaders.indexOf(colName) == updatedHeaders.size - 1) {
                    SQL_CREATE_ENTRIES += " TEXT);"
                } else
                    SQL_CREATE_ENTRIES += " TEXT, "

            }
        }
        Log.d("CREATE", SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_ENTRIES)

        var SQL_INSERT = "INSERT INTO INTERCROSS ("

        if (updatedHeaders != null && updatedHeaders.size == 0)
            SQL_INSERT += ")"
        else {
            for (colName in updatedHeaders) {
                if (colName != "_id") {
                    SQL_INSERT += colName
                    if (updatedHeaders.indexOf(colName) == updatedHeaders.size - 1) {
                        SQL_INSERT += ")"
                    } else
                        SQL_INSERT += ","

                }
            }
        }

        SQL_INSERT += "SELECT "

        if (updatedHeaders != null && updatedHeaders.size != 0) {
            for (colName in updatedHeaders) {
                if (colName != "_id") {
                    SQL_INSERT += colName
                    if (updatedHeaders.indexOf(colName) != updatedHeaders.size - 1) {
                        SQL_INSERT += ","
                    }
                }
            }
        }

        SQL_INSERT += " FROM INTERCROSS_OLD;"

        Log.d("INSERT", SQL_INSERT)

        db.execSQL(SQL_INSERT)


        mHeaderIds!!.removeAt(position)

        mAdapter!!.notifyDataSetChanged()

    }

    override fun onLongItemClick(v: View, position: Int) {

    }
}
