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

    fun getParentCounts(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

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

        return entries
    }

    fun updateColumns(inputCols: ArrayList<String>) {

        //TODO add function for just altering / add col
        //TODO split thsi function into an alter col / create new table
        try {

            val colCursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                    null, null, null,
                    null, null, null)
            val knownCols = colCursor.columnNames

            val cols = ArrayList(inputCols.filter { c -> c !in knownCols })

            colCursor.close()

            writableDatabase.execSQL("DROP TABLE IF EXISTS ${IdEntryContract.IdEntry.TABLE_NAME}_OLD")

            writableDatabase.execSQL("ALTER TABLE ${IdEntryContract.IdEntry.TABLE_NAME} " +
                    "RENAME TO ${IdEntryContract.IdEntry.TABLE_NAME}_OLD")

            var sqlCreateEntries = "CREATE TABLE ${IdEntryContract.IdEntry.TABLE_NAME}( "

            knownCols.forEach { col ->
                when (col) {
                    IdEntryContract.IdEntry.COLUMN_NAME_ID -> sqlCreateEntries += "$col PRIMARY KEY"
                    else -> sqlCreateEntries += "$col TEXT"
                }
                if (knownCols.last() != col) sqlCreateEntries += ", "
            }

            when (cols.size) {
                0 -> sqlCreateEntries += ");"
                else -> {
                    cols.forEach { header ->
                        sqlCreateEntries += ", $header"
                        when (header) {
                            cols.last() -> sqlCreateEntries += " TEXT);"
                            else -> sqlCreateEntries += " TEXT "
                        }
                    }
                }
            }

            Log.d("CREATE", sqlCreateEntries)
            writableDatabase.execSQL(sqlCreateEntries)

            var sqlInsert = "INSERT INTO ${IdEntryContract.IdEntry.TABLE_NAME} ("

            knownCols.forEach { col ->

                sqlInsert += col

                when (col) {
                    knownCols.last() -> sqlInsert += ")"
                    else -> sqlInsert += ", "
                }
            }


            sqlInsert += "SELECT "

            knownCols.forEach { col ->

                sqlInsert += col

                when(col) {
                    knownCols.last() -> sqlInsert += " FROM ${IdEntryContract.IdEntry.TABLE_NAME}_OLD;"
                    else -> sqlInsert += ", "
                }
            }

            Log.d("INSERT", sqlInsert)

            writableDatabase.execSQL(sqlInsert)

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
    }

    fun insertEntry(entry: ContentValues) {

        writableDatabase.beginTransaction()

        try {
            writableDatabase.insert(IdEntryContract.IdEntry.TABLE_NAME, null, entry)
            writableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        writableDatabase.endTransaction()

    }

    fun getUserInputHeaders() : ArrayList<String> {

        val headers = ArrayList<String>()

        try {
            val cursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                    null, null, null,
                    null, null, null)

            if (cursor.moveToFirst()) {
                cursor.columnNames.forEach {
                    when (it in arrayOf(IdEntryContract.IdEntry.COLUMN_NAME_CROSS,
                            IdEntryContract.IdEntry.COLUMN_NAME_DATE,
                            IdEntryContract.IdEntry.COLUMN_NAME_FEMALE,
                            IdEntryContract.IdEntry.COLUMN_NAME_ID,
                            IdEntryContract.IdEntry.COLUMN_NAME_MALE,
                            IdEntryContract.IdEntry.COLUMN_NAME_LOCATION,
                            IdEntryContract.IdEntry.COLUMN_NAME_USER)) {
                        false -> headers.add(it)
                    }
                }
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        return headers
    }

    fun updateValues(key: String, values: ArrayList<String>) {

        val headers = getUserInputHeaders()

        writableDatabase.beginTransaction()
        try {
            headers.forEachIndexed { index, header ->
                writableDatabase.execSQL(
                        "UPDATE ${IdEntryContract.IdEntry.TABLE_NAME} " +
                                "SET $header = '${values[index]}' " + "WHERE _id = $key")
            }
            writableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        writableDatabase.endTransaction()
    }

    fun getUserInputValues(key: Int): ArrayList<String?> {

        val userHeaders = getUserInputHeaders().toTypedArray()

        val values = arrayOfNulls<String>(userHeaders.size)

        if (userHeaders.isNotEmpty()) {

            try {

                val cursor = readableDatabase.query(IdEntryContract.IdEntry.TABLE_NAME,
                        userHeaders, "_id=?", arrayOf(key.toString()),
                        null, null, null)

                if (cursor.moveToFirst()) {

                    do {

                        cursor.columnNames.forEachIndexed { index, header ->

                            header?.let { head ->

                                values[index] = cursor.getString(cursor.getColumnIndexOrThrow(head)) ?: ""
                            }
                        }

                    } while (cursor.moveToNext())

                }

                cursor.close()

            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
        }

        return ArrayList(values.toList())
    }

    fun getMainPageEntries(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

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
