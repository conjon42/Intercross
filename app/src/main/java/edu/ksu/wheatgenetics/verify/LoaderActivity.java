package edu.ksu.wheatgenetics.verify;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
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

import static edu.ksu.wheatgenetics.verify.VerifyConstants.CSV_URI;
import static edu.ksu.wheatgenetics.verify.VerifyConstants.DEFAULT_CONTENT_REQ;

public class LoaderActivity extends AppCompatActivity {

    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private Uri _csvUri;
    private String mDelimiter;

    private Button finishButton, doneButton;
    private ListView headerList;
    private TextView tutorialText;
    private EditText separatorText;
    private String mHeader;
    private int mIdHeaderIndex;

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

        _ids = new SparseArray<>();

        _cols = new SparseArray<>();

        headerList = ((ListView) findViewById(R.id.headerList));

        headerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                finishButton.setEnabled(true);
                mIdHeaderIndex = position;
                tutorialText.setText(R.string.finish_tutorial);
            }
        });

        tutorialText = (TextView) findViewById(R.id.tutorialTextView);
        separatorText = (EditText) findViewById(R.id.separatorTextView);

        doneButton = ((Button) findViewById(R.id.doneButton));
        finishButton = ((Button) findViewById(R.id.finishButton));

        final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Choose file to import."), VerifyConstants.DEFAULT_CONTENT_REQ);


        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                doneButton.setVisibility(View.GONE);
                separatorText.setVisibility(View.GONE);
                mDelimiter = separatorText.getText().toString();
                displayHeaderList();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AsyncCSVParse().execute(_csvUri);

            }
        });

    }

    private void displayHeaderList() {

        tutorialText.setText(R.string.choose_header_tutorial);
        finishButton.setVisibility(View.VISIBLE);
        finishButton.setEnabled(false);
        headerList.setVisibility(View.VISIBLE);

        if (mHeader == null) {
            headerList.setAdapter(new ArrayAdapter<String>(this, R.layout.row));
            tutorialText.setText("Error reading file.");
            return;
        }

        final String[] headers = mHeader.split(mDelimiter);
        if (headers.length > 0 && headers[0] != null) {
            final ArrayAdapter<String> idAdapter =
                    new ArrayAdapter<>(this, R.layout.row);
            for (String h : headers) {
                idAdapter.add(h);
            }
            headerList.setAdapter(idAdapter);
        } else {
            headerList.setAdapter(new ArrayAdapter<String>(this, R.layout.row));
            tutorialText.setText("Error reading file.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == DEFAULT_CONTENT_REQ) {
            if (resultCode == RESULT_OK) {
                _csvUri = intent.getData();

                //inner async class to get header line
                new AsyncTask<Uri, Void, String>() {

                    @Override
                    protected String doInBackground(Uri[] params) {

                        if (params.length > 0 && params[0] != null) try {

                            //query file path type
                            String fileType = getPath(params[0]);
                            final String[] pathSplit = fileType.split("\\.");
                            final String fileExtension = pathSplit[pathSplit.length - 1];

                            if (fileExtension.equals("csv")) { //files ending in .csv
                                mDelimiter = ",";
                            } else if (fileExtension.equals("tsv") || fileExtension.equals("txt")) { //fiels ending in .txt
                                mDelimiter = "\t";
                            } else mDelimiter = null; //non-supported file type, display header for user to choose delimiter

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

                        mHeader = header;

                        //if unsupported file type, start delimiter tutorial
                        if (mDelimiter == null) {
                            separatorText.setVisibility(View.VISIBLE);
                            doneButton.setVisibility(View.VISIBLE);
                            tutorialText.setText(R.string.choose_separator_tutorial);
                            tutorialText.append(header);
                        } else { //display header list
                            displayHeaderList();
                        }
                    }

                }.execute(_csvUri);
            } else finish();
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

                        String temp = null;
                        while ((temp = br.readLine()) != null) {
                            final String[] id_line = temp.split(mDelimiter);
                            final int size = id_line.length;
                            if (size != 0) {
                                final String id = id_line[mIdHeaderIndex];
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
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] data) {

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
    }


    //based on https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
    public String getPath(Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(LoaderActivity.this, uri)) {

                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    final String[] doc =  DocumentsContract.getDocumentId(uri).split(":");
                    final String documentType = doc[0];

                    if ("primary".equalsIgnoreCase(documentType)) {
                        return Environment.getExternalStorageDirectory() + "/" + doc[1];
                    }
                }
                else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(LoaderActivity.this, contentUri, null, null);
                }
            }
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
