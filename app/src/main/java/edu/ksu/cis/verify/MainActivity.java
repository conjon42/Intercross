package edu.ksu.cis.verify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.HashMap;
import java.util.Set;

import edu.ksu.cis.mobilevisbarcodechecker.R;

import static edu.ksu.cis.verify.VerifyConstants.*;

public class MainActivity extends AppCompatActivity {

    private Context _ctx;
    private SparseArray<String> _ids;
    private HashMap<String, String> _idMap;
    private String _prevIdLookup;
    private Uri _csvUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);

        _ctx = this;

        _ids = new SparseArray<String>();
        _idMap = new HashMap<>();

        final EditText et = ((EditText) findViewById(R.id.scannerTextView));
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
                    //if (_csvUri != null) {
                        final TextView tv = (TextView) findViewById(R.id.valueView);
                        final String key = s.toString();
                        if (_idMap.containsKey(key)) {
                            tv.setText(_idMap.get(key));
                            _prevIdLookup = key;
                        } else tv.setText("");
                    //}
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
                if (_idMap != null && _idMap.size() > 0) {
                    final Set<String> keys = _idMap.keySet();
                    cameraIntent.putExtra(KEY_ARRAY, _idMap.keySet().toArray(new String[keys.size()]));
                }
                startActivityForResult(cameraIntent, CAMERA_SCAN_KEY);
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
                _csvUri = intent.getData();
                _idMap = (HashMap<String, String>) intent.getSerializableExtra(VerifyConstants.CSV_MAP);

                if (_idMap != null) {
                    final ListView lv = ((ListView) findViewById(R.id.idTable));
                    final ArrayAdapter<String> idAdapter =
                            new ArrayAdapter<String>(_ctx, R.layout.row);
                    for (String id : _idMap.keySet()) {
                        idAdapter.add(id);
                    }
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
