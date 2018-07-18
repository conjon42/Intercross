package org.phenoapps.intercross;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ManageHeadersActivity extends AppCompatActivity implements HeaderRecyclerViewAdapter.ItemClickListener {

    private ArrayList<String> mHeaderIds;

    private HeaderRecyclerViewAdapter mAdapter;

    private IdEntryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_headers);

        mDbHelper = new IdEntryDbHelper(this);

        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Set<String> headerList = sharedPref.getStringSet(SettingsActivity.HEADER_SET, new HashSet<>());

        mHeaderIds = new ArrayList<>();

        if (!headerList.isEmpty()) {

            for (String header : headerList) {
                mHeaderIds.add(header);
            }

            buildListView();

        }
        Button headerInputButton = (Button) findViewById(R.id.addHeaderButton);

        headerInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newHeaderName = ((EditText) findViewById(R.id.editTextHeader)).getText().toString();

                if (!mHeaderIds.contains(newHeaderName)) {

                    mHeaderIds.add(newHeaderName);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putStringSet(SettingsActivity.HEADER_SET, new HashSet<String>(mHeaderIds));
                    editor.apply();

                    buildListView();

                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    mDbHelper.onUpdateColumns(db, mHeaderIds.toArray(new String[] {}));

                    db.close();

                } else {
                    Toast.makeText(ManageHeadersActivity.this,
                            "Header already exists in table.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void buildListView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.listHeaders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HeaderRecyclerViewAdapter(this, mHeaderIds);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d("CLICK", "C");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] currentHeaders = null;
        try {
            String table = IdEntryContract.IdEntry.TABLE_NAME;
            Cursor cursor = db.query(table, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    currentHeaders = cursor.getColumnNames();

                } while (cursor.moveToNext());

            }
            cursor.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.close();

        String removedCol = mAdapter.getItem(position);

        ArrayList<String> updatedHeaders = new ArrayList<>();
        for (int i = 0; i < currentHeaders.length; i++) {
            if (!currentHeaders[i].equals(removedCol)) {
                updatedHeaders.add(currentHeaders[i]);
            }
        }

        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        Set<String> headerSet = prefs.getStringSet(SettingsActivity.HEADER_SET, new HashSet<>());

        headerSet.remove(removedCol);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putStringSet(SettingsActivity.HEADER_SET, headerSet);

        editor.clear();

        editor.apply();

        db = mDbHelper.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS INTERCROSS_OLD");

        db.execSQL("ALTER TABLE INTERCROSS RENAME TO INTERCROSS_OLD");

        String SQL_CREATE_ENTRIES =
            "CREATE TABLE INTERCROSS( "
                       /* + IdEntryContract.IdEntry.COLUMN_NAME_MALE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_FEMALE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_CROSS + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_USER + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_DATE + " TEXT,"
                        + IdEntryContract.IdEntry.COLUMN_NAME_LOCATION + " TEXT "*/;


        if (updatedHeaders.size() == 0) SQL_CREATE_ENTRIES += ");";
        else {
            for (String colName : updatedHeaders) {
                SQL_CREATE_ENTRIES += colName;
                if (updatedHeaders.indexOf(colName) == updatedHeaders.size() - 1) {
                    SQL_CREATE_ENTRIES += " TEXT);";
                } else SQL_CREATE_ENTRIES += " TEXT, ";

            }
        }
        Log.d("CREATE", SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);

        String SQL_INSERT = "INSERT INTO INTERCROSS (";

        if (updatedHeaders != null && updatedHeaders.size() == 0) SQL_INSERT += ")";
        else {
            for (String colName: updatedHeaders) {
                if (!colName.equals("_id")) {
                    SQL_INSERT += colName;
                    if (updatedHeaders.indexOf(colName) == updatedHeaders.size() - 1) {
                        SQL_INSERT += ")";
                    } else SQL_INSERT += ",";

                }
            }
        }

        SQL_INSERT += "SELECT ";

        if (updatedHeaders != null && updatedHeaders.size() != 0) {
            for (String colName: updatedHeaders) {
                if (!colName.equals("_id")) {
                    SQL_INSERT += colName;
                    if (updatedHeaders.indexOf(colName) != updatedHeaders.size() - 1) {
                        SQL_INSERT += ",";
                    }
                }
            }
        }

        SQL_INSERT += " FROM INTERCROSS_OLD;";

        Log.d("INSERT", SQL_INSERT);

        db.execSQL(SQL_INSERT);


        mHeaderIds.remove(position);

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLongItemClick(View v, int position) {

    }
}
