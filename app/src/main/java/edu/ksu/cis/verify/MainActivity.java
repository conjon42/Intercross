package edu.ksu.cis.verify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import edu.ksu.cis.mobilevisbarcodechecker.R;

import static edu.ksu.cis.verify.VerifyConstants.*;

public class MainActivity extends AppCompatActivity {

    private Context _ctx;
    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private String _prevIdLookup;
    private Uri _csvUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);

        _ctx = this;

        if (_ids == null)
            _ids = new SparseArray<>();

        if (_cols == null)
            _cols = new SparseArray<>();

        final EditText et = ((EditText) findViewById(R.id.scannerTextView));
        final ListView lv = ((ListView) findViewById(R.id.idTable));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
                boolean colorList = sharedPref.getBoolean(SettingsActivity.COLOR_LIST, false);

                if (!colorList) {
                    //llv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                    //lv.clearChoices();
                } else {
                    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }

                lv.setItemChecked(position, false);

                           /*final EditText et = ((EditText) findViewById(R.id.scannerTextView));
                            et.setText(((TextView) view).getText().toString());*/
                final String key = ((TextView) view).getText().toString();
                final int size = _ids.size();
                final TextView tv = (TextView) findViewById(R.id.valueView);
                for (int i = 0; i < size; i = i + 1) {
                    if (key.equals(_ids.get(_ids.keyAt(i)))) {
                        tv.setText(_cols.get(_cols.keyAt(i)));
                        break;
                    }
                }
                            /*final SparseBooleanArray checkedPositions = ((ListView) parent).getCheckedItemPositions();
                            if (checkedPositions != null) {
                                final int checkedSize = checkedPositions.size();
                                for (int i = 0; i < checkedSize; i = i + 1) {
                                    lv.setItemChecked(i, checkedPositions.get(checkedPositions.keyAt(i)));
                                }
                            }*/
            }
        });

        final TextWatcher tw = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() > 0) {
                    //get app settings
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
                    boolean filterList = sharedPref.getBoolean(SettingsActivity.FILTER_LIST, false);
                    boolean colorList = sharedPref.getBoolean(SettingsActivity.COLOR_LIST, false);

                    final int size = _ids.size();
                    final TextView tv = (TextView) findViewById(R.id.valueView);
                    final ListView lv = ((ListView) findViewById(R.id.idTable));
                    int found = -1;
                    for (int i = 0; i < size; i = i + 1) {
                        if (s.toString().equals(_ids.get(_ids.keyAt(i)))) {
                            tv.setText(_cols.get(_cols.keyAt(i)));
                            found = i;
                            break;
                        }
                    }
                    if (found == -1) {
                        tv.setText("");
                    } else if (filterList) {
                        final ArrayAdapter<String> oldAdapter = (ArrayAdapter<String>) lv.getAdapter();
                        final ArrayAdapter<String> updatedAdapter = new ArrayAdapter<String>(_ctx, R.layout.row);
                        final int oldSize = oldAdapter.getCount();
                        
                        for (int i = 0; i < oldSize; i = i + 1) {
                            if (i != found) {
                                updatedAdapter.add(oldAdapter.getItem(i));
                            }
                        }
                        lv.setAdapter(updatedAdapter);
                        final int childCount = lv.getChildCount();

                        for (int i = 0; i < childCount; i = i + 1) {
                            if (lv.getChildAt(i).isSelected()) {
                                lv.setItemChecked(i, true);
                            }
                        }
                        _ids.remove(_ids.keyAt(found));
                        _cols.remove(_cols.keyAt(found));
                    } else if (colorList) {
                        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        lv.setItemChecked(found, true);
                    } else {
                       // lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                    }
                }
            }
        };
        et.addTextChangedListener(tw);

        if (savedInstanceState != null) {

            //load csv uri with saved content, invoke a csv parse
            //if a previous id was selected, update the table with a csv lookup
            if (savedInstanceState.containsKey(CSV_URI)) {
                _csvUri = Uri.parse(savedInstanceState.getString(CSV_URI));

                if (savedInstanceState.containsKey(PREV_ID_LOOKUP)) {
                    final String id = savedInstanceState.getString(PREV_ID_LOOKUP);
                    //new AsyncCSVLookup().execute(id); TODO SAVE ID MAP 
                }
            }
        }

        final File dir = _ctx.getDir("Verify", Context.MODE_PRIVATE);
        Log.d("directory", dir.getAbsolutePath().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_open:
                final Intent i = new Intent(this, LoaderActivity.class);
                startActivityForResult(i, OPEN_CSV_FILE);
                return true;
            case R.id.action_camera:
                final Intent cameraIntent = new Intent(this, CameraActivity.class);
                if (_ids != null && _ids.size() > 0) {
                    final ArrayList<String> keys = new ArrayList<>();
                    final int size = _ids.size();
                    for (int j = 0; j < size; j = j + 1)
                        keys.add(j, _ids.get(_ids.keyAt(j)));

                    cameraIntent.putExtra(KEY_ARRAY, keys);
                }
                startActivityForResult(cameraIntent, CAMERA_SCAN_KEY);
                return true;
            case R.id.action_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, VerifyConstants.SETTINGS_INTENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == OPEN_CSV_FILE) {
            if (resultCode == RESULT_OK) {

                //get intent array list messages (columns and keys)
                final ArrayList<String> colMsg = intent.getStringArrayListExtra(VerifyConstants.COL_ARRAY);
                final ArrayList<String> keyMsg = intent.getStringArrayListExtra(VerifyConstants.ID_ARRAY);

                //convert messages to global sparse arrays
                final int kMsgSize = keyMsg.size();
                for (int j = 0; j < kMsgSize; j = j + 1)
                    _ids.append(j, keyMsg.get(j));

                final int cMsgSize = colMsg.size();
                for (int j = 0; j < cMsgSize; j = j + 1)
                    _cols.append(j, colMsg.get(j));

                if (_ids != null) {

                    final ListView lv = ((ListView) findViewById(R.id.idTable));
                    final ArrayAdapter<String> idAdapter =
                            new ArrayAdapter<String>(_ctx, R.layout.row);
                    final int size = _ids.size();
                    for (int i = 0; i < size; i = i + 1)
                        idAdapter.add(_ids.get(i));
                    lv.setAdapter(idAdapter);
                }
            }
        } else if (requestCode == CAMERA_SCAN_KEY) {
            if (resultCode == RESULT_OK) {
                if (intent.hasExtra(PREV_ID_LOOKUP)) {
                    _prevIdLookup = intent.getStringExtra(PREV_ID_LOOKUP);
                    ((TextView) findViewById(R.id.scannerTextView))
                            .setText(_prevIdLookup);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (_csvUri != null)
            outState.putString(CSV_URI, _csvUri.toString());

        if (_prevIdLookup != null)
            outState.putString(PREV_ID_LOOKUP, _prevIdLookup);
    }
}
