package org.phenoapps.intercross;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class CountActivity extends AppCompatActivity {

    private ArrayList<AdapterEntry> mCrossIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);


        IdEntryDbHelper mDbHelper = new IdEntryDbHelper(this);

        mCrossIds = new ArrayList<>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        try {
            String table = IdEntryContract.IdEntry.TABLE_NAME;
            Cursor cursor = db.query(table, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    final String[] headers = cursor.getColumnNames();
                    String male = null;
                    String female = null;
                    String crossId = null;
                    String timestamp = null;

                    for (String header : headers) {


                        final String val = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        );

                        if (header.equals("male")) {
                            male = val;
                        }

                        if (header.equals("female")) {
                            female = val;
                        }

                        if (header.equals("cross_id")) crossId = val;

                        if (header.equals("timestamp")) timestamp = val.split(" ")[0];

                    }

                    if (male != null && female != null) {
                        Cursor countCursor =
                                db.query(table, new String[]{"male, female"},
                                        "male=? and female=?",
                                        new String[]{male, female}, null, null, null);


                        AdapterEntry entry = new AdapterEntry(crossId, String.valueOf(countCursor.getCount()));

                        mCrossIds.add(entry);

                        countCursor.close();
                    }

                } while (cursor.moveToNext());

            }
            cursor.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        buildListView();

    }

    private void buildListView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CountRecyclerViewAdapter adapter = new CountRecyclerViewAdapter(this, mCrossIds);
        recyclerView.setAdapter(adapter);
    }
}
