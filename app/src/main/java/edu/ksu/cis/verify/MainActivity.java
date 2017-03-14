package edu.ksu.cis.verify;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
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
                    if (_csvUri != null) {
                        final TextView tv = (TextView) findViewById(R.id.valueView);
                        final String key = s.toString();
                        if (_idMap.containsKey(key)) {
                            tv.setText(_idMap.get(key));
                            _prevIdLookup = key;
                        } else tv.setText("");
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
                new AsyncCSVParse().execute(_csvUri);

                if (savedInstanceState.containsKey(PREV_ID_LOOKUP)) {
                    final String id = savedInstanceState.getString(PREV_ID_LOOKUP);
                    new AsyncCSVLookup().execute(id);
                }
            }
        }
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
                final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("text/*");
                startActivityForResult(Intent.createChooser(i, "Open CSV"), OPEN_CSV_FILE);
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
                new AsyncCSVParse().execute(_csvUri);
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

    private class AsyncCSVLookup extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            if (params.length > 0 && params[0] != null) try {
                final InputStream is = getContentResolver().openInputStream(_csvUri);
                if (is != null) {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    final String[] headers = br.readLine().split(","); //assume first line contains headers
                    String temp = null;
                    while ((temp = br.readLine()) != null) {
                        final String[] id_line = temp.split(",");
                        if (id_line.length != 0) {
                            if (id_line[0].equals(params[0])) { //assume first column is always the id column
                                final String[] cols = new String[id_line.length];
                                for (int i = 0; i < id_line.length; i = i + 1) {
                                    cols[i] = headers[i] + ": " + id_line[i];
                                }
                                return cols;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] cols) {
            final TextView tv = ((TextView) findViewById(R.id.valueView));
            //final ListView lv = ((ListView) findViewById(R.id.valueTable));
            if (cols != null && cols.length > 0 && cols[0] != null) {
                //final ArrayAdapter<String> valueAdapter =
                  //      new ArrayAdapter<>(_ctx, R.layout.row);
                int size = cols.length;
                for (int i = 0; i < size; i = i + 1) {
                   // valueAdapter.add(cols[i]);
                    tv.append(cols[i]);
                    tv.append("\n");
                }
               // lv.setAdapter(valueAdapter);
            } else tv.setText(""); // else lv.setAdapter(null);
        }
    }

    private class AsyncCSVParse extends AsyncTask<Uri, Void, String[]> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected String[] doInBackground(Uri[] params) {

            if (params.length > 0 && params[0] != null) try {
                final InputStream is = getContentResolver().openInputStream(params[0]);
                if (is != null) {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    final String[] headers = br.readLine().split(",");
                    String temp = null;

                    while ((temp = br.readLine()) != null) {
                        final String[] id_line = temp.split(",");
                        final int size = id_line.length;
                        if (size != 0) {
                            final String id = id_line[0];
                            final StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < size; i = i + 1) {
                                sb.append(headers[i]);
                                sb.append(": ");
                                sb.append(id_line[i]);
                                sb.append("\n");
                            }
                            _idMap.put(id, sb.toString());
                        }
                    }
                    return headers;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] headers) {

            final ListView lv = ((ListView) findViewById(R.id.idTable));
            if (headers != null && headers.length > 0 && headers[0] != null) {
                final ArrayAdapter<String> idAdapter = new ArrayAdapter<String>(_ctx, R.layout.row);
                for (String id : _idMap.keySet()) {
                    idAdapter.add(id);
                }
                lv.setAdapter(idAdapter);
            } else lv.setAdapter(new ArrayAdapter<String>(_ctx, R.layout.row));

        }
    }
}
