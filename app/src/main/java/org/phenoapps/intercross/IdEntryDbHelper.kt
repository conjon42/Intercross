package org.phenoapps.intercross

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import org.phenoapps.intercross.IdEntryContract.SQL_CREATE_ENTRIES
import java.util.*

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

    fun getParentCounts(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

        synchronized(readableDatabase) {

            try {
                val cursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                        arrayOf("cross_id", "male", "female"),
                        null, null, null, null, null)

                var male: String? = null
                var female: String? = null
                if (cursor.moveToFirst()) {
                    do {

                        val entry = AdapterEntry()

                        cursor.columnNames.forEach { header ->
                            header?.let {

                                val colVal = cursor.getString(
                                        cursor.getColumnIndexOrThrow(it)) ?: String()

                                when (it) {
                                    IdEntryContract.IdEntry.COLUMN_NAME_MALE -> male = colVal
                                    IdEntryContract.IdEntry.COLUMN_NAME_FEMALE -> female = colVal
                                    IdEntryContract.IdEntry.COLUMN_NAME_CROSS -> entry.first = colVal
                                }
                            }
                        }

                        val countCursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                                arrayOf("_id"), "male=? AND female=?",
                                arrayOf(male, female), null, null, null)

                        entry.second = countCursor.count.toString()

                        countCursor.close()

                        entries.add(entry)

                    } while (cursor.moveToNext())
                }
                cursor.close()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
        }
        return entries
    }

    fun insertEntry(entry: ContentValues) {

        synchronized(writableDatabase) {

            writableDatabase.beginTransaction()

            try {
                writableDatabase.insert("INTERCROSS", null, entry)
                writableDatabase.setTransactionSuccessful()
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }

            writableDatabase.endTransaction()
        }
    }

    fun updateUserColumns(key: Int, entries: ArrayList<AdapterEntry>) {

        synchronized(writableDatabase) {
            writableDatabase.beginTransaction()
            try {
                entries.forEach { entry ->
                    writableDatabase.execSQL(
                            "UPDATE INTERCROSS " +
                                    "SET ${entry.first} = '${entry.second}' " + "WHERE _id = '$key'")
                }
                writableDatabase.setTransactionSuccessful()
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
            writableDatabase.endTransaction()
        }
    }

    fun getUserInputHeaders(headers: Array<String>, key: Int):
            Triple<ArrayList<AdapterEntry>, String, String> {

        val entries = ArrayList<AdapterEntry>()
        var timestamp = String()
        var crossId = String()

        synchronized(writableDatabase) {

            try {

                val cursor = writableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME, null,
                        "_id=?", arrayOf(key.toString()), null, null, null)

                if (cursor.moveToFirst()) {

                    do {

                        cursor.columnNames.forEach iter@ { header ->

                            header?.let { head ->

                                val colVal: String =
                                        cursor.getString(cursor.getColumnIndexOrThrow(head)) ?: "None"

                                when (head) {
                                    IdEntryContract.IdEntry.COLUMN_NAME_DATE -> timestamp = colVal
                                    IdEntryContract.IdEntry.COLUMN_NAME_CROSS -> crossId = colVal
                                }

                                when (head in headers) {
                                    true -> entries.add(AdapterEntry(head, colVal, key))
                                    false -> return@iter
                                }
                            }
                        }

                    } while (cursor.moveToNext())

                }

                cursor.close()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
        }

        return Triple(entries, timestamp, crossId)
    }

    fun getMainPageEntries(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

        synchronized(readableDatabase) {
            try {
                val cursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                        null, null, null, null, null, null)

                if (cursor.moveToFirst()) {
                    do {

                        val entry = AdapterEntry()

                        cursor.columnNames.forEach { header ->
                            header?.let {

                                val colVal = cursor.getString(
                                        cursor.getColumnIndexOrThrow(it)) ?: String()

                                when (it) {
                                    IdEntryContract.IdEntry.COLUMN_NAME_ID ->
                                        entry.id = cursor.getInt(cursor.getColumnIndexOrThrow(it))
                                    IdEntryContract.IdEntry.COLUMN_NAME_CROSS -> entry.first = colVal
                                    IdEntryContract.IdEntry.COLUMN_NAME_DATE ->
                                        entry.second = formatDatetime(colVal)
                                }
                            }
                        }

                        entries.add(entry)

                    } while (cursor.moveToNext())
                }
                cursor.close()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }

        }

        return entries
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun formatDatetime(input: String): String {
        return input.split(" ".toRegex())
                .dropLastWhile { token -> token.isEmpty() }
                .toTypedArray()[0]
    }

    companion object {

        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "IdEntryReader.db"
    }
}
