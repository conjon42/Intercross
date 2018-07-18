package org.phenoapps.intercross;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.phenoapps.intercross.IdEntryContract.SQL_CREATE_ENTRIES;

class IdEntryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "IdEntryReader.db";

    IdEntryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(IdEntryContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onUpdateColumns(SQLiteDatabase db, String[] cols) {

        List<String> headerNames = null;

        try {
            String table = IdEntryContract.IdEntry.TABLE_NAME;
            Cursor cursor = db.query(table, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    headerNames = Arrays.asList(cursor.getColumnNames());

                } while (cursor.moveToNext());

            }
            cursor.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        for (String newCol : cols) {

            if (headerNames != null && !headerNames.contains(newCol)) {
                db.execSQL("ALTER TABLE INTERCROSS ADD COLUMN " + newCol + " TEXT DEFAULT ''");
            }
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
