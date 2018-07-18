package org.phenoapps.intercross;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuxValueInputActivity extends AppCompatActivity {

    final static private String line_separator = System.getProperty("line.separator");

    private IdEntryDbHelper mDbHelper;

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    private ArrayList<AdapterEntry> mCrossIds;

    private ActionBarDrawerToggle mDrawerToggle;

    private View focusedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        String crossId = getIntent().getStringExtra("crossId");
       // String[] headers = getIntent().getStringArrayExtra("headers");

        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        String[] headers = prefs.getStringSet(SettingsActivity.HEADER_SET, new HashSet<>()).toArray(new String[] {});
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDbHelper = new IdEntryDbHelper(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        HashMap<String, String> colMap = new HashMap<>();

        try {
            String table = IdEntryContract.IdEntry.TABLE_NAME;

            Cursor cursor = db.query(table, headers, "cross_id=?", new String[] {crossId}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    final String[] headerCols = cursor.getColumnNames();

                    for (String header : headerCols) {

                        final String val = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        );

                        colMap.put(header, val);

                    }

                } while (cursor.moveToNext());

            }
            cursor.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        List<EditTextAdapterEntry> entries = new ArrayList<>();
        List<EditText> editTexts = new ArrayList<>();

        for (String header : headers) {
            EditText editText = new EditText(this);
            editText.setText(colMap.get(header));
            entries.add(new EditTextAdapterEntry(editText, header));
            editTexts.add(editText);
        }

        EditTextRecyclerViewAdapter adapter = new EditTextRecyclerViewAdapter(this, entries);
        recyclerView.setAdapter(adapter);

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.addView(recyclerView);

        Button submitButton = new Button(this);// = entry.editText;
        submitButton.setText("Update");
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int next = 0;

                for (String header: headers) {
                    db.execSQL("UPDATE INTERCROSS SET " +
                            header + " = '" + editTexts.get(next++).getText().toString() + //adapter.getItem(next++).editText.getText().toString() + //entries.get(next++).editText.getText() +
                            "' WHERE cross_id = '" + crossId + "'");
                }
            }
        });

        view.addView(submitButton);
        setContentView(view);

    }

}
