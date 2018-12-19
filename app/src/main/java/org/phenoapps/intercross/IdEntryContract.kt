package org.phenoapps.intercross

import android.provider.BaseColumns

internal object IdEntryContract {


    const val TABLE_NAME = "INTERCROSS"

    const val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME(${IdEntry.COLUMN_NAME_ID} INTEGER PRIMARY KEY," +
                    "${IdEntry.COLUMN_NAME_CROSS} TEXT, ${IdEntry.COLUMN_NAME_FEMALE}," +
                    "${IdEntry.COLUMN_NAME_MALE} TEXT, ${IdEntry.COLUMN_NAME_USER}," +
                    "${IdEntry.COLUMN_NAME_DATE} TEXT, ${IdEntry.COLUMN_NAME_LOCATION} TEXT," +
                    "${IdEntry.COLUMN_NAME_POLLINATION_TYPE} TEXT, ${IdEntry.COLUMN_NAME_CROSS_COUNT} INTEGER," +
                    "${IdEntry.COLUMN_NAME_CROSS_NAME} TEXT)"

    val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + IdEntry.TABLE_NAME

    internal class IdEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "INTERCROSS"
            const val COLUMN_NAME_ID = "_id"
            const val COLUMN_NAME_MALE = "male"
            const val COLUMN_NAME_FEMALE = "female"
            const val COLUMN_NAME_CROSS = "cross_id"
            const val COLUMN_NAME_USER = "person"
            const val COLUMN_NAME_DATE = "timestamp"
            const val COLUMN_NAME_LOCATION = "location"
            const val COLUMN_NAME_POLLINATION_TYPE = "cross_type"
            const val COLUMN_NAME_CROSS_COUNT = "cross_count"
            const val COLUMN_NAME_CROSS_NAME = "cross_name"

            val COLUMNS = arrayOf(COLUMN_NAME_ID, COLUMN_NAME_MALE, COLUMN_NAME_FEMALE,
                    COLUMN_NAME_CROSS, COLUMN_NAME_USER, COLUMN_NAME_DATE, COLUMN_NAME_LOCATION,
                    COLUMN_NAME_POLLINATION_TYPE, COLUMN_NAME_CROSS_COUNT, COLUMN_NAME_CROSS_NAME)
        }
    }

}
