package org.phenoapps.intercross

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import java.util.ArrayList
import java.util.Arrays

import org.phenoapps.intercross.IdEntryContract.SQL_CREATE_ENTRIES

internal class IdEntryDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     *
     *
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(IdEntryContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun onUpdateColumns(db: SQLiteDatabase, cols: Array<String>) {

        var headerNames: List<String>? = null

        try {
            val table = IdEntryContract.IdEntry.TABLE_NAME
            val cursor = db.query(table, null, null, null,
                    null, null, null)

            if (cursor.moveToFirst()) {
                do {
                    headerNames = Arrays.asList(*cursor.columnNames)

                } while (cursor.moveToNext())

            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        for (newCol in cols) {

            if (headerNames != null && !headerNames.contains(newCol)) {
                val newColAlter = "ALTER TABLE INTERCROSS ADD COLUMN $newCol TEXT DEFAULT ''"
                db.execSQL("ALTER TABLE INTERCROSS ADD COLUMN $newCol TEXT DEFAULT '';")
                Log.d("ALTER", newColAlter)
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {

        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "IdEntryReader.db"
    }
}
