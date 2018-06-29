package org.phenoapps.intercross;

import android.provider.BaseColumns;

final class IdEntryContract {

    private IdEntryContract() {}

    static class IdEntry implements BaseColumns {
        static final String TABLE_NAME = "INTERCROSS";
        static final String COLUMN_NAME_ID = "_id";
        static final String COLUMN_NAME_MALE = "male";
        static final String COLUMN_NAME_FEMALE = "female";
        static final String COLUMN_NAME_CROSS = "cross_id";
        static final String COLUMN_NAME_USER = "person";
        static final String COLUMN_NAME_DATE = "timestamp";
        static final String COLUMN_NAME_LOCATION = "location";
    }

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE INTERCROSS( " +
                    IdEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY, "
                    + IdEntry.COLUMN_NAME_MALE + " TEXT,"
                    + IdEntry.COLUMN_NAME_FEMALE + " TEXT,"
                    + IdEntry.COLUMN_NAME_CROSS + " TEXT,"
                    + IdEntry.COLUMN_NAME_USER + " TEXT,"
                    + IdEntry.COLUMN_NAME_DATE + " TEXT,"
                    + IdEntry.COLUMN_NAME_LOCATION + " TEXT)";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + IdEntry.TABLE_NAME;

}
