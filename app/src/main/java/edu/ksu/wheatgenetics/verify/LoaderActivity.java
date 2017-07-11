package edu.ksu.wheatgenetics.verify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static edu.ksu.wheatgenetics.verify.VerifyConstants.*;

public class LoaderActivity extends AppCompatActivity {

    private Context _ctx;
    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private Uri _csvUri;
    private String mDelimiter;

    private Button finishButton, openButton, doneButton;
    private ListView headerList;
    private TextView tutorialText;
    private EditText separatorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_file);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ActivityCompat.requestPermissions(this, VerifyConstants.permissions, VerifyConstants.PERM_REQ);

        _ctx = this;

        if (_ids == null)
            _ids = new SparseArray<>();

        if (_cols == null)
            _cols = new SparseArray<>();

        headerList = ((ListView) findViewById(R.id.headerList));

        tutorialText = (TextView) findViewById(R.id.tutorialTextView);
        separatorText = (EditText) findViewById(R.id.separatorTextView);

        openButton = ((Button) findViewById(R.id.openButton));
        doneButton = ((Button) findViewById(R.id.doneButton));
        finishButton = ((Button) findViewById(R.id.finishButton));

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "Open CSV"), VerifyConstants.DEFAULT_CONTENT_REQ);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        tutorialText.setText(R.string.choose_header_tutorial);
                        doneButton.setVisibility(View.GONE);
                        mDelimiter = separatorText.getText().toString();
                        separatorText.setVisibility(View.GONE);
                        finishButton.setVisibility(View.VISIBLE);
                        finishButton.setEnabled(false);
                        headerList.setVisibility(View.VISIBLE);
                        new AsyncCSVParse().execute(_csvUri);
                    }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
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
                tutorialText.setText(R.string.choose_separator_tutorial);
                openButton.setVisibility(View.GONE);
                separatorText.setVisibility(View.VISIBLE);
                doneButton.setVisibility(View.VISIBLE);

                //inner async class to get header line
                new AsyncTask<Uri, Void, String>() {

                    @Override
                    protected String doInBackground(Uri[] params) {

                        if (params.length > 0 && params[0] != null) try {
                            final InputStream is = getContentResolver().openInputStream(params[0]);
                            if (is != null) {
                                final BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                final String header = br.readLine();
                                br.close();
                                return header;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    protected void onPostExecute(String header) {

                        tutorialText.setText(R.string.choose_separator_tutorial);
                        tutorialText.append("First line of uploaded file:");
                        tutorialText.append(header);
                    }
                }.execute(_csvUri);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (_csvUri != null)
            outState.putString(CSV_URI, _csvUri.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

                    if (mDelimiter != null) {
                        final BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        final String[] headers = br.readLine().split(mDelimiter);
                        String temp;

                        while ((temp = br.readLine()) != null) {
                            final String[] id_line = temp.split(mDelimiter);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] headers) {

            headerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (headers != null && headers.length > 0 && headers[0] != null) {
                final ArrayAdapter<String> idAdapter =
                        new ArrayAdapter<>(_ctx, R.layout.row);
                for (String h : headers) {
                    idAdapter.add(h);
                }
                headerList.setAdapter(idAdapter);
            } else {
                headerList.setAdapter(new ArrayAdapter<String>(_ctx, R.layout.row));
                tutorialText.setText(getString(R.string.import_error));

            }

            headerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    finishButton.setEnabled(true);
                    tutorialText.setText(R.string.finish_tutorial);
                }
            });
        }
    }
}
