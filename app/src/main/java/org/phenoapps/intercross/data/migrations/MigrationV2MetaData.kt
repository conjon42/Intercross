package org.phenoapps.intercross.data.migrations

import android.database.sqlite.SQLiteException
import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * Room database migration class for going from version 1 to 2
 * Version 2 added a metadata json encoded column
 */
class MigrationV2MetaData : Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {

        with (database) {

            try {

                beginTransaction()

                createNewEventsTable()

                setTransactionSuccessful()

            } catch (e: SQLiteException) {

                e.printStackTrace()

            } finally {

                endTransaction()
            }
        }
    }

    //backup technique for deleting columns in SQLite https://www.sqlite.org/faq.html#q11
    private fun SupportSQLiteDatabase.createNewEventsTable() {

        execSQL("CREATE TEMP TABLE events_backup(codeId TEXT NOT NULL, mom TEXT NOT NULL, dad TEXT NOT NULL, name TEXT NOT NULL, date TEXT NOT NULL, person TEXT NOT NULL, experiment TEXT NOT NULL, type INTEGER NOT NULL, sex INTEGER NOT NULL, eid INTEGER PRIMARY KEY AUTOINCREMENT, metadata TEXT NOT NULL)")

        execSQL("INSERT INTO events_backup SELECT codeId, mom, dad, name, date, person, experiment, type, sex, eid, person FROM events")

        migrateStaticToJson()

        execSQL("DROP TABLE events")

        execSQL("""CREATE TABLE events(codeId TEXT NOT NULL, mom TEXT NOT NULL, dad TEXT NOT NULL, name TEXT NOT NULL, date TEXT NOT NULL, person TEXT NOT NULL, experiment TEXT NOT NULL, type INTEGER NOT NULL, sex INTEGER NOT NULL, eid INTEGER PRIMARY KEY AUTOINCREMENT, metadata TEXT NOT NULL)""")

        execSQL("INSERT INTO events SELECT codeId, mom, dad, name, date, person, experiment, type, sex, eid, metadata FROM events_backup")

        execSQL("DROP TABLE events_backup")

        execSQL("CREATE UNIQUE INDEX index_events_codeId ON events (codeId)")
    }

    //second transaction for inserting old static columns into encoded json column
    private fun SupportSQLiteDatabase.migrateStaticToJson() {

        query("SELECT eid, fruits, seeds, flowers FROM events").use {

            with (it) {

                moveToFirst()

                do {

                    //get indices for V1 static column string metadata and unique rowid
                    val idIndex = getColumnIndexOrThrow("eid")
                    val fruitsIndex = getColumnIndexOrThrow("fruits")
                    val flowersIndex = getColumnIndexOrThrow("flowers")
                    val seedsIndex = getColumnIndexOrThrow("seeds")

                    //get actual string values, eid is used to update the row and
                    //static metadata fields fruits/flowers/seeds will be encoded into a json string
                    val eid = getStringOrNull(idIndex) ?: "-1"
                    val fruits = getStringOrNull(fruitsIndex) ?: "0"
                    val flowers = getStringOrNull(flowersIndex) ?: "0"
                    val seeds = getStringOrNull(seedsIndex) ?: "0"

                    //creates a json encoded string from the static columns
                    val json: String = compileJsonFromStaticColumns(fruits, flowers, seeds)

                    //inserts the new json string back into the row
                    execSQL("UPDATE events_backup SET metadata = ? WHERE eid = ?",
                        arrayOf(json, eid))

                } while (moveToNext())
            }
        }
    }

    //simple function that returns an encoded string with the static column data
    //e.g {"fruits": 4, "flowers": 2, "seeds": 18}
    private fun compileJsonFromStaticColumns(fruits: String, flowers: String, seeds: String) = JsonObject().apply {

        add("fruits", JsonArray(2).apply {
            add(fruits.toIntOrNull() ?: 0)
            add(0)
        })

        add("flowers", JsonArray(2).apply {
            add(flowers.toIntOrNull() ?: 0)
            add(0)
        })

        add("seeds", JsonArray(2).apply {
            add(seeds.toIntOrNull() ?: 0)
            add(0)
        })

    }.toString()
}