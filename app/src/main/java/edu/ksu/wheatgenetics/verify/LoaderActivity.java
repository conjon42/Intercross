package edu.ksu.wheatgenetics.verify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static edu.ksu.wheatgenetics.verify.VerifyConstants.*;

public class LoaderActivity extends AppCompatActivity {

    private Context _ctx;
    private String _headerValue;
    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private Uri _csvUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_loader);

        ActivityCompat.requestPermissions(this, VerifyConstants.permissions, VerifyConstants.PERM_REQ);

        _ctx = this;

        if (_ids == null)
            _ids = new SparseArray<>();

        if (_cols == null)
            _cols = new SparseArray<>();

        ((Button) findViewById(R.id.openButton))
            .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("text/*");
                startActivityForResult(Intent.createChooser(i, "Open CSV"), VerifyConstants.DEFAULT_CONTENT_REQ);
            }
        });

        ((Button) findViewById(R.id.finishButton))
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //initialize intent
                final Intent intent = new Intent();

                //convert global sparse arrays to arraylists to pass as extra
                final int idSize = _ids.size();
                final ArrayList<String> idArray = new ArrayList<>();
                for (int i = 0; i < idSize; i = i + 1) {
                    idArray.add(i, _ids.get(_ids.keyAt(i)));
                }

                final int colSize = _cols.size();
                final ArrayList<String> colArray = new ArrayList<>();
                for (int i = 0; i < colSize; i = i + 1) {
                    colArray.add(i, _cols.get(_cols.keyAt(i)));
                }

                //pass array lists into extra, end activity
                intent.putExtra(VerifyConstants.COL_ARRAY_EXTRA, colArray);
                intent.putExtra(VerifyConstants.ID_ARRAY_EXTRA, idArray);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == DEFAULT_CONTENT_REQ) {
            if (resultCode == RESULT_OK) {
                _csvUri = intent.getData();
                new AsyncCSVParse().execute(_csvUri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (_csvUri != null)
            outState.putString(CSV_URI, _csvUri.toString());
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

                    int row_count = 1;
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
                            final int next = _ids.size();
                            _ids.append(next, id);
                            _cols.append(next, sb.toString());
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

            final ListView lv = ((ListView) findViewById(R.id.headerList));
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (headers != null && headers.length > 0 && headers[0] != null) {
                final ArrayAdapter<String> idAdapter =
                        new ArrayAdapter<>(_ctx, R.layout.row);
                for (String h : headers) {
                    idAdapter.add(h);
                }
                lv.setAdapter(idAdapter);
            } else lv.setAdapter(new ArrayAdapter<String>(_ctx, R.layout.row));

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    _headerValue = ((TextView) view).getText().toString();
                }
            });
        }
    }
}
