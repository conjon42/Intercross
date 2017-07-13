package edu.ksu.wheatgenetics.verify;

import android.provider.BaseColumns;

/**
 * Created by Chaney on 7/13/2017.
 */

public final class IdEntryContract {

    private IdEntryContract() {}

    public static class IdEntry implements BaseColumns {
        public static final String TABLE_NAME = "id_entry";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_CHECKED = "checked";
        public static final String COLUMN_NAME_VALS = "vals";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + IdEntry.TABLE_NAME + " (" +
                    IdEntry._ID + " INTEGER PRIMARY KEY," +
                    IdEntry.COLUMN_NAME_CHECKED + " INTEGER," +
                    IdEntry.COLUMN_NAME_ID + " TEXT," +
                    IdEntry.COLUMN_NAME_VALS + " TEXT);";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + IdEntry.TABLE_NAME;
}
