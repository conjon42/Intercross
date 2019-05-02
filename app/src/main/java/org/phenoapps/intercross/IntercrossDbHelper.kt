package org.phenoapps.intercross

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_CROSS
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_CROSS_COUNT
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_CROSS_NAME
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_DATE
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_FEMALE
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_ID
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_LOCATION
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_MALE
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_NOTE
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_POLLINATION_TYPE
import org.phenoapps.intercross.IntercrossDbContract.IdEntry.Companion.COLUMN_NAME_USER
import org.phenoapps.intercross.IntercrossDbContract.SQL_CREATE_ENTRIES
import org.phenoapps.intercross.IntercrossDbContract.SQL_CREATE_FAUX_TABLE
import org.phenoapps.intercross.IntercrossDbContract.SQL_CREATE_WISH_TABLE
import org.phenoapps.intercross.IntercrossDbContract.TABLE_NAME

internal class IntercrossDbHelper(ctx: Context) :
        SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_FAUX_TABLE)
        db.execSQL(SQL_CREATE_WISH_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(IntercrossDbContract.SQL_DELETE_ENTRIES)
        db.execSQL(IntercrossDbContract.SQL_DELETE_FAUX_TABLE)
        db.execSQL(IntercrossDbContract.SQL_DELETE_WISH_TABLE)
        onCreate(db)
    }

    fun resetWishList() {
        writableDatabase.execSQL(IntercrossDbContract.SQL_DELETE_WISH_TABLE)
        writableDatabase.execSQL(SQL_CREATE_WISH_TABLE)
    }

    fun getParentCounts(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf("_id", "male", "female"),
                    null, null, null, null, null)

            var male = String()
            var female = String()
            if (cursor.moveToFirst()) {
                do {

                    val entry = AdapterEntry()

                    cursor.columnNames.forEach { header ->
                        header?.let {

                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(it)) ?: String()

                            when (it) {
                                IntercrossDbContract.IdEntry.COLUMN_NAME_MALE -> male = colVal
                                IntercrossDbContract.IdEntry.COLUMN_NAME_FEMALE -> female = colVal
                            }
                        }
                    }

                    val countCursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                            arrayOf("_id"), "male=? AND female=?",
                            arrayOf(male, female), null, null, null)

                    entry.first = female
                    entry.second = male
                    entry.third = countCursor.count.toString()

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

    //query for any row that has male or female parent as this id's cross id
    fun getOffspring(id: Int): Array<Pair<String, String>> {

        val crossId = getCrossId(id)

        val childFromMale = ArrayList<Pair<String, String>>()
        val childFromFemale = ArrayList<Pair<String, String>>()

        try {
            var cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS, IntercrossDbContract.IdEntry.COLUMN_NAME_ID),
                    "${IntercrossDbContract.IdEntry.COLUMN_NAME_FEMALE}=?", arrayOf(crossId), null, null, null)
            var childId = String()
            var childCross = String()
            if (cursor.moveToFirst()) {
                do {
                    cursor.columnNames.forEach { header ->
                        header?.let {
                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(it)) ?: String()
                            when (it) {
                                IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS -> childCross = colVal
                                IntercrossDbContract.IdEntry.COLUMN_NAME_ID -> childId = colVal
                            }
                        }
                    }
                    if (childId.isNotBlank() && childCross.isNotBlank()) {
                        childFromFemale.add(Pair(childCross, childId))
                    }
                } while (cursor.moveToNext())
            }

            cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS, IntercrossDbContract.IdEntry.COLUMN_NAME_ID),
                    "${IntercrossDbContract.IdEntry.COLUMN_NAME_MALE}=?", arrayOf(crossId), null, null, null)

            childId = String()
            childCross = String()
            if (cursor.moveToFirst()) {
                do {
                    cursor.columnNames.forEach { header ->
                        header?.let {
                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(it)) ?: String()
                            when (it) {
                                IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS -> childCross = colVal
                                IntercrossDbContract.IdEntry.COLUMN_NAME_ID -> childId = colVal
                            }
                        }
                    }
                    if (childId.isNotBlank() && childCross.isNotBlank()) {
                        childFromMale.add(Pair(childCross, childId))
                    }
                } while (cursor.moveToNext())
            }

            cursor.close()


            return (childFromFemale + childFromMale).toTypedArray()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return arrayOf()
    }

    fun getParents(id: Int): Array<String> {
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf("male", "female"),
                    "_id=?", arrayOf(id.toString()), null, null, null)
            var male = String()
            var female = String()
            if (cursor.moveToFirst()) {
                cursor.columnNames.forEach { header ->
                    header?.let {
                        val colVal = cursor.getString(
                                cursor.getColumnIndexOrThrow(it)) ?: String()
                        when (it) {
                            IntercrossDbContract.IdEntry.COLUMN_NAME_MALE -> male = colVal
                            IntercrossDbContract.IdEntry.COLUMN_NAME_FEMALE -> female = colVal
                        }
                    }
                }
            }
            cursor.close()
            return arrayOf(female, male)
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return arrayOf()
    }

    fun getSiblings(id: Int): List<String> {
        val parents = getParents(id)
        if (parents.size == 2) {
            try {
                val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                        arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS),
                        "male=?, female=?", arrayOf(parents[1], parents[0]), null, null, null)
                val siblings = ArrayList<String>()
                do {
                    if (cursor.moveToFirst()) {
                        cursor.columnNames.forEach { header ->
                            header?.let {
                                val colVal = cursor.getString(
                                        cursor.getColumnIndexOrThrow(it)) ?: String()
                                if (it == IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS) siblings.add(colVal)
                            }
                        }
                    }
                } while (cursor.moveToNext())
                cursor.close()
                return siblings
            } catch (e: SQLiteException) {
                e.printStackTrace()
            }
            return listOf()
        }
        return listOf()
    }

    fun getRowId(cross: String): Int {
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf("_id"), "${IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS}=?",
                    arrayOf(cross), null, null, null)
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(
                        IntercrossDbContract.IdEntry.COLUMN_NAME_ID))
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return -1
    }

    private fun getCrossId(id: Int): String {
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS),
                    "${IntercrossDbContract.IdEntry.COLUMN_NAME_ID}=?",
                    arrayOf(id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(
                        IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS))
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getPersonById(id: Int): String {
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_USER), "${IntercrossDbContract.IdEntry.COLUMN_NAME_ID}=?",
                    arrayOf(id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(
                        IntercrossDbContract.IdEntry.COLUMN_NAME_USER)) ?: "-1"
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return "-1"
    }

    fun getTimestampById(id: Int): String {
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_DATE), "${IntercrossDbContract.IdEntry.COLUMN_NAME_ID}=?",
                    arrayOf(id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(
                        IntercrossDbContract.IdEntry.COLUMN_NAME_DATE)) ?: "-1"
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return "-1"
    }

    fun getCrosses(female: String, male: String): List<String> {

        val crosses = ArrayList<String>()
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS),
                    "${IntercrossDbContract.IdEntry.COLUMN_NAME_FEMALE}=? AND ${IntercrossDbContract.IdEntry.COLUMN_NAME_MALE}=?",
                    arrayOf(female, male), null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val cross = cursor.getString(cursor.getColumnIndexOrThrow(
                            IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS)) ?: ""
                    crosses.add(cross)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return crosses
    }

    //pragma command to get the header names of the DB
    private fun getColumns(): ArrayList<String> {

        val cols = ArrayList<String>()

        val cursor = readableDatabase.rawQuery(
                "pragma table_info(${IntercrossDbContract.IdEntry.TABLE_NAME});", null)

        if (cursor.moveToFirst()) {
            do {
                cols += cursor.getString(cursor.getColumnIndex("name"))
            } while (cursor.moveToNext())
        }

        cursor.close()

        return cols
    }

    fun insert(table: String, entry: ContentValues) {

        writableDatabase.beginTransaction()

        try {
            writableDatabase.insert(table, null, entry)
            writableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun updateNote(entry: ContentValues) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.update(TABLE_NAME, entry, null, null)
            writableDatabase.setTransactionSuccessful()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun getRow(id: Int): IntercrossDbContract.Columns? {
        val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                null, "_id=?", arrayOf(id.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val columns = IntercrossDbContract.Columns()
            cursor.columnNames.forEach { header ->
                when (header) {
                    COLUMN_NAME_ID -> columns.id = cursor.getInt(cursor.getColumnIndexOrThrow(header))
                    COLUMN_NAME_CROSS_NAME -> columns.crossName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CROSS_NAME))
                    COLUMN_NAME_CROSS -> columns.cross = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CROSS))
                    COLUMN_NAME_CROSS_COUNT -> columns.crossCount = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_CROSS_COUNT))
                    COLUMN_NAME_POLLINATION_TYPE -> columns.polType = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_POLLINATION_TYPE))
                    COLUMN_NAME_MALE -> columns.male = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_MALE))
                    COLUMN_NAME_FEMALE -> columns.female = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FEMALE))
                    COLUMN_NAME_LOCATION -> columns.location = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LOCATION)) ?: ""
                    COLUMN_NAME_DATE -> columns.date = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DATE))
                    COLUMN_NAME_NOTE -> columns.note = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NOTE)) ?: ""
                }
            }
            return columns
        }
        cursor.close()
        return null
    }

    fun getWishList(): List<Array<Any>> {
        val wishes = ArrayList<Array<Any>>()
        try {
            val cursor = readableDatabase.query("WISH",
                    null,null,null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    //femaleID TEXT, femaleName TEXT, maleID TEXT, maleName TEXT, numberCrosses INTEGER)"
                    var femaleID = String()
                    var femaleName = String()
                    var maleID = String()
                    var maleName = String()
                    var numCrosses = 0

                    cursor.columnNames.forEach { header ->
                        when (header) {
                            "femaleID" -> femaleID = cursor.getString(cursor.getColumnIndexOrThrow("femaleID")) ?: ""
                            "femaleName" -> femaleName = cursor.getString(cursor.getColumnIndexOrThrow("femaleName")) ?: ""
                            "maleID" -> maleID = cursor.getString(cursor.getColumnIndexOrThrow("maleID")) ?: ""
                            "maleName" -> maleName = cursor.getString(cursor.getColumnIndexOrThrow("maleName")) ?: ""
                            "numberCrosses" -> numCrosses = cursor.getInt(cursor.getColumnIndexOrThrow("numberCrosses"))

                        }
                    }
                    wishes.add(arrayOf(femaleID, femaleName, maleID, maleName, numCrosses))

                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return wishes
    }

    fun getNamesByGroup(group: String): String {
        var names = String()
        try {
            val cursor = readableDatabase.query("FAUX",
                    arrayOf("names"), "g=?", arrayOf(group), null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val n = cursor.getString(cursor.getColumnIndexOrThrow("names")) ?: ""
                    names = n
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return names
    }

    fun getGroups(): List<String> {
        val groups = ArrayList<String>()
        try {
            val cursor = readableDatabase.query("FAUX",
                    arrayOf("g"), null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val g = cursor.getString(cursor.getColumnIndexOrThrow(
                            "g")) ?: ""
                    groups.add(g)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return groups
    }

    fun insertFauxId(group: String, entry: ContentValues) {
        if(!getGroups().contains(group)) {
            writableDatabase.beginTransaction()
            try {
                writableDatabase.insert("FAUX", null, entry)
                writableDatabase.setTransactionSuccessful()
            } catch (e: SQLiteException) {
                e.printStackTrace()
            } finally {
                writableDatabase.endTransaction()
            }
        } else {
            writableDatabase.beginTransaction()
            try {
                writableDatabase.update("FAUX", entry, "g=?", arrayOf(group))
                writableDatabase.setTransactionSuccessful()
                //writableDatabase.updateWithOnConflict("FAUX", entry, null, null, SQLiteDatabase.CONFLICT_REPLACE)
            } catch (e: SQLiteException) {
                e.printStackTrace()
            } finally {
                writableDatabase.endTransaction()
            }
        }
    }


    fun getPollenGroups(): List<String> {
        val array = ArrayList<String>()
        try {
            val cursor = readableDatabase.query("FAUX_TABLE",
                    arrayOf("name"),
                    null, null, null, null, null)

            if (cursor.moveToFirst()) {
                cursor.columnNames.forEach { header ->
                    header?.let {
                        val colVal = cursor.getString(
                                cursor.getColumnIndexOrThrow(it)) ?: String()
                        if (it == "name") array.add(colVal)
                    }
                }
            }
            cursor.close()
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        return array
    }


    fun getMainPageEntries(): ArrayList<AdapterEntry> {

        val entries = ArrayList<AdapterEntry>()

        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    null, null, null, null, null, IntercrossDbContract.IdEntry.COLUMN_NAME_ID + " DESC")

            if (cursor.moveToFirst()) {
                do {

                    val entry = AdapterEntry()

                    cursor.columnNames.forEach { header ->
                        header?.let {

                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(it)) ?: String()

                            when (it) {
                                IntercrossDbContract.IdEntry.COLUMN_NAME_ID ->
                                    entry.id = cursor.getInt(cursor.getColumnIndexOrThrow(it))
                                IntercrossDbContract.IdEntry.COLUMN_NAME_CROSS -> entry.first = colVal
                                IntercrossDbContract.IdEntry.COLUMN_NAME_DATE ->
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

    fun getPollinationType(id: Int): String {

        var polType = String()
        try {
            val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                    arrayOf(IntercrossDbContract.IdEntry.COLUMN_NAME_POLLINATION_TYPE),
                    "${IntercrossDbContract.IdEntry.COLUMN_NAME_ID}=?", arrayOf(id.toString()),
                    null, null, null)

            if (cursor.moveToFirst()) {
                polType = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                IntercrossDbContract.IdEntry.COLUMN_NAME_POLLINATION_TYPE)) ?: ""
            }
            cursor.close()

        } catch (e: SQLiteException) {
            e.printStackTrace()
        }

        return polType
    }

    fun getExportData(): ArrayList<String> {

        val data = ArrayList<String>()

        val cursor = readableDatabase.query(IntercrossDbContract.IdEntry.TABLE_NAME,
                IntercrossDbContract.IdEntry.COLUMNS.filter { it != IntercrossDbContract.IdEntry.COLUMN_NAME_ID }.toTypedArray(),
                null, null, null, null, null)

        //first write header line
        val headers = arrayOf("cross_id", "male", "female", "person", "timestamp",
                "location", "cross_type", "cross_count", "cross_name")
        data.add(headers.joinToString(","))

        //populate text file with current database values
        if (cursor.moveToFirst()) {
            do {
                val values = ArrayList<String>()
                for (i in headers.indices) {
                    when(headers[i]) {
                        "male" -> {
                            val m = cursor.getString(cursor.getColumnIndexOrThrow("male"))
                            val names = getNamesByGroup(m)
                            if (names.contains(",")) values.add("{${names}}")
                            else if (names.isBlank()) {
                                values.add(m)
                            } else values.add(names)

                        }
                        else -> {
                            val colVal = cursor.getString(
                                    cursor.getColumnIndexOrThrow(headers[i])
                            )
                            values.add(colVal ?: "none")
                        }
                    }
                }
                data.add(values.joinToString(","))
            } while (cursor.moveToNext())
        }

        cursor.close()

        return data
    }

    fun deleteEntry(id: Int) {
        writableDatabase.delete(TABLE_NAME, "_id = ?", arrayOf(id.toString()))
    }

    companion object {
        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "IntercrossReader.db"
    }
}
