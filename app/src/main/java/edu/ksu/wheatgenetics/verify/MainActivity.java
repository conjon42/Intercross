package edu.ksu.wheatgenetics.verify;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private IdEntryDbHelper mDbHelper;

    private SparseArray<String> _ids;

    private File mVerifyDirectory;
    private int mMatchingOrder;
    private Timer mTimer = new Timer("user input for suppressing messages", true);
    private TextView valueView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Ringtone mRingtoneNoti;
    private Uri mRingtoneUri;
    private EditText mScannerTextView;
    private ListView mIdTable;

    private String mListId;

    //pair mode vars
    private String mPairCol;
    private String mNextPairVal;

    NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _ids = new SparseArray<>();

        mRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mRingtoneNoti = RingtoneManager.getRingtone(this, mRingtoneUri);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);
        setupDrawer();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean tutorialMode = sharedPref.getBoolean(SettingsActivity.TUTORIAL_MODE, true);

        if (tutorialMode)
            launchIntro();

        ActivityCompat.requestPermissions(this, VerifyConstants.permissions, VerifyConstants.PERM_REQ);

        mNextPairVal = null;
        mMatchingOrder = 0;
        mPairCol = null;

        mIdTable = ((ListView) findViewById(R.id.idTable));
        mIdTable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mIdTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mScannerTextView.setText(((TextView) view).getText().toString());
                checkScannedItem();
            }
        });
        mIdTable.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //get app settings
                insertNoteIntoDb(((TextView) view).getText().toString());
                return true;
            }
        });

        valueView = (TextView) findViewById(R.id.valueView);
        valueView.setMovementMethod(new ScrollingMovementMethod());

        mScannerTextView = ((EditText) findViewById(R.id.scannerTextView));
        mScannerTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {

                        checkScannedItem();
                    }
                }
                return false;
            }
        });

        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mScannerTextView.setText("");
                checkScannedItem();
            }
        });

        if (isExternalStorageWritable()) {
            mVerifyDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Verify");
            if (!mVerifyDirectory.isDirectory()) mVerifyDirectory.mkdirs();
        }

        mDbHelper = new IdEntryDbHelper(this);

        loadSQLToLocal();

        if (mListId != null)
            updateCheckedItems();
    }

    private synchronized void checkScannedItem() {

        final String scannedId = mScannerTextView.getText().toString();
        mTimer.purge();
        mTimer.cancel();

        //update database
        exertModeFunction(scannedId);

        //view updated database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from VERIFY WHERE " + mListId + "='" + scannedId + "'", null);

        final String[] headerTokens = cursor.getColumnNames();
        final StringBuilder values = new StringBuilder();
        if (cursor.moveToFirst()) {
            for (String header : headerTokens) {

                final String val = cursor.getString(
                        cursor.getColumnIndexOrThrow(header)
                );
                values.append(header + " : " + val + "\n");
            }
            cursor.close();
            valueView.setText(values.toString());
        } else resetTimer();
    }

    private synchronized void insertNoteIntoDb(final String id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a note for the given item.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (!value.isEmpty()) {

                    final SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    final String updateNoteQuery = "UPDATE VERIFY SET note = '" + value + "'"
                            + " WHERE " + mListId + " = '" + id + "'";
                    db.execSQL(updateNoteQuery);

                }
            }
        });

        builder.show();
    }

    private synchronized void exertModeFunction(String id) {

        mTimer.purge();
        mTimer.cancel();

        //get app settings
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));

        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        if (scanMode == 0 ) { //default mode ring a notification

            mMatchingOrder = 0;
            ringNotification();
            Toast.makeText(this, "Scanned id found: " + id, Toast.LENGTH_SHORT).show();
        } else if (scanMode == 1) { //order mode

            final int tableIndex = getTableIndexById(id);

            if (tableIndex != -1) {
                if (mMatchingOrder == tableIndex) {
                    ringNotification();
                    mMatchingOrder++;
                    Toast.makeText(this, "Order matches id: " + id + " at index: " + tableIndex, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Scanning out of order!", Toast.LENGTH_SHORT).show();
            }

        } else if (scanMode == 2) { //filter mode, delete rows with given id

            mMatchingOrder = 0;
            final String deleteIdQuery =
                    "DELETE FROM VERIFY WHERE " + mListId + " = '" + id + "'";
            db.execSQL(deleteIdQuery);

            updateFilteredArrayAdapter(id);

        } else if (scanMode == 3) { //if color mode, update the db to highlight the item

            mMatchingOrder = 0;
            final String updateCheckedQuery =
                    "UPDATE VERIFY SET c = 1 WHERE " + mListId + " = '" + id + "'";
            db.execSQL(updateCheckedQuery);
        } else if (scanMode == 4) { //pair mode

            mMatchingOrder = 0;

            if (mPairCol != null) {

                //if next pair id is waiting, check if it matches scanned id and reset mode
                if (mNextPairVal != null) {
                    if (mNextPairVal.equals(id)) {
                        ringNotification();
                        Toast.makeText(this, "Scanned paired item: " + id, Toast.LENGTH_SHORT).show();
                    }
                    mNextPairVal = null;
                } else { //otherwise query for the current id's pair
                    final String getNextPairQuery =
                            "SELECT " + mPairCol + " FROM VERIFY WHERE " + mListId + " = '" + id + "'";
                    final Cursor cursor = db.rawQuery(getNextPairQuery, null);
                    if (cursor.moveToFirst()) {
                        mNextPairVal = cursor.getString(
                                cursor.getColumnIndexOrThrow(mPairCol)
                        );
                    } else mNextPairVal = null;
                    cursor.close();
                }
            }
        }
        //always update user and datetime
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh-mm-ss", Locale.getDefault());
        final String updateUserAndDateQuery =
                "UPDATE VERIFY SET user = " + "'" + sharedPref.getString(SettingsActivity.USER_NAME, "Default") + "'"
                        +             ", d = " + "'" + sdf.format(c.getTime()) + "'"
                        +             ", s = s + 1 WHERE " + mListId + " = '" + id + "'";
        db.execSQL(updateUserAndDateQuery);


        updateCheckedItems();

    }

    private synchronized void updateCheckedItems() {

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        //list of ideas to populate and update the view with
        final HashSet<String> ids = new HashSet<>();

        final String getCheckedItemsQuery =
                "SELECT " + mListId + " FROM VERIFY WHERE c = 1";

        Cursor cursor = db.rawQuery(getCheckedItemsQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(
                        cursor.getColumnIndexOrThrow(mListId)
                );

                ids.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int position = 0; position < mIdTable.getCount(); position++) {

            final String id = (mIdTable.getItemAtPosition(position)).toString();

            if (ids.contains(id)) {
                mIdTable.setItemChecked(position, true);
            } else mIdTable.setItemChecked(position, false);
        }
    }

    private synchronized void loadSQLToLocal() {

        _ids = new SparseArray<>();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mListId = sharedPref.getString(SettingsActivity.LIST_KEY_NAME, null);

        if (mListId != null) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            Cursor cursor = db.rawQuery("select * from VERIFY", null);

            if (cursor.moveToFirst()) {
                do {
                    final String[] headers = cursor.getColumnNames();
                    for (String header : headers) {

                        final String val = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        );

                        if (header.equals(mListId)) {
                            _ids.append(_ids.size(), val);
                        }
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
            buildListView();

        }
    }

    private synchronized void askUserExportFileName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose name for exported file.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (!value.isEmpty()) {
                    if (isExternalStorageWritable()) {
                        try {
                            final File output = new File(mVerifyDirectory, value);
                            final FileOutputStream fstream = new FileOutputStream(output);
                            final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                            final Cursor cursor = db.rawQuery("SElECT * FROM VERIFY", null);

                            //first write header line
                            final String[] headers = cursor.getColumnNames();
                            for (int i = 0; i < headers.length; i++) {
                                if (i != 0) fstream.write(",".getBytes());
                                fstream.write(headers[i].getBytes());
                            }
                            fstream.write("\n".getBytes());
                            //populate text file with current database values
                            if (cursor.moveToFirst()) {
                                do {
                                    for (int i = 0; i < headers.length; i++) {
                                        if (i != 0) fstream.write(",".getBytes());
                                        final String val = cursor.getString(
                                                cursor.getColumnIndexOrThrow(headers[i])
                                        );
                                        if (val == null) fstream.write("null".getBytes());
                                        else fstream.write(val.getBytes());
                                    }
                                    fstream.write("\n".getBytes());
                                } while (cursor.moveToNext());
                            }

                            fstream.flush();
                            fstream.close();

                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } //error toast
                } //use default name
            }
        });

        builder.show();

    }

    private void resetTimer() {

        mTimer.purge();
        mTimer.cancel();
        mTimer = new Timer("user input for suppressing messages", true);
        mTimer.schedule(new SuppressMessageTask(), 0);
    }

    //returns index of table with identifier = id, returns -1 if not found
    private int getTableIndexById(String id) {

        final ArrayAdapter<String> adapter = (ArrayAdapter<String>) mIdTable.getAdapter();
        final int size = adapter.getCount();

        for (int i = 0; i < size; i = i + 1) {
            final String temp = adapter.getItem(i);
            if (temp.equals(id)) return i;
        }

        return -1;
    }

    private void updateFilteredArrayAdapter(String id) {

        //update id table array adapter
        final ArrayAdapter<String> oldAdapter = (ArrayAdapter<String>) mIdTable.getAdapter();
        final ArrayAdapter<String> updatedAdapter = new ArrayAdapter<>(this, R.layout.row);
        final int oldSize = oldAdapter.getCount();

        for (int i = 0; i < oldSize; i = i + 1) {
            final String temp = oldAdapter.getItem(i);
            if (!temp.equals(id)) updatedAdapter.add(temp);
        }
        mIdTable.setAdapter(updatedAdapter);
    }

    private void ringNotification() {

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean audioEnabled = sharedPref.getBoolean(SettingsActivity.AUDIO_ENABLED, true);

        if (audioEnabled) {
            try {
                mRingtoneNoti.play();
            } catch (Exception e) {
                e.printStackTrace();
                mRingtoneNoti.stop();
                mRingtoneNoti = RingtoneManager.getRingtone(this, mRingtoneUri);

            }
        }
    }

    private class SuppressMessageTask extends TimerTask {

        @Override
        public void run() {
            sendIdNotFoundMsg();
        }
    }

    void sendIdNotFoundMsg() {

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Scanned id not found", Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.valueView)).setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_toolbar, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_camera:
                final Intent cameraIntent = new Intent(this, ScanActivity.class);
                startActivityForResult(cameraIntent, VerifyConstants.CAMERA_INTENT_REQ);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {

            if (intent != null) {
                switch (requestCode) {
                    case VerifyConstants.LOADER_INTENT_REQ:

                        mListId = null;
                        mPairCol = null;

                        if (intent.hasExtra(VerifyConstants.LIST_ID_EXTRA))
                            mListId = intent.getStringExtra(VerifyConstants.LIST_ID_EXTRA);
                        if (intent.hasExtra(VerifyConstants.PAIR_COL_EXTRA))
                            mPairCol = intent.getStringExtra(VerifyConstants.PAIR_COL_EXTRA);

                        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        final SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(SettingsActivity.LIST_KEY_NAME, mListId);
                        editor.commit();

                        clearListView();
                        loadSQLToLocal();
                        updateCheckedItems();
                        break;
                }

                if (intent.hasExtra(VerifyConstants.CAMERA_RETURN_ID)) {
                    mScannerTextView.setText(intent.getStringExtra(VerifyConstants.CAMERA_RETURN_ID));
                    checkScannedItem();
                }
            }
        }
    }

    private void buildListView() {

        final ArrayAdapter<String> idAdapter =
                new ArrayAdapter<>(this, R.layout.row);
        final int size = _ids.size();
        for (int i = 0; i < size; i = i + 1)
            idAdapter.add(_ids.get(_ids.keyAt(i)));
        mIdTable.setAdapter(idAdapter);
    }

    private void clearListView() {

        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.row);

        mIdTable.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
       // loadSQLToLocal();
       // buildListView();
       // updateCheckedItems();
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            public void onDrawerClosed(View view) {
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.nav_import:
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));
                final Intent i = new Intent(this, LoaderDBActivity.class);
                if (scanMode == 4) //pair mode
                    i.putExtra(VerifyConstants.PAIR_MODE_LOADER, "");
                startActivityForResult(i, VerifyConstants.LOADER_INTENT_REQ);
                break;
            case R.id.nav_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, VerifyConstants.SETTINGS_INTENT_REQ);
                break;
            case R.id.nav_export:
                askUserExportFileName();
                break;
            case R.id.nav_about:
                showAboutDialog();
                break;
            case R.id.nav_intro:
                final Intent intro_intent = new Intent(MainActivity.this, IntroActivity.class);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        startActivity(intro_intent);
                    }
                });
                break;
        }

        mDrawerLayout.closeDrawers();
    }

    private void showAboutDialog()
    {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        {
            final android.view.View personView = this.getLayoutInflater().inflate(
                    R.layout.about, new android.widget.LinearLayout(this),
                    false);

            {
                assert personView != null;
                final android.widget.TextView versionTextView = (android.widget.TextView)
                        personView.findViewById(R.id.tvVersion);
                try
                {
                    final android.content.pm.PackageInfo packageInfo =
                            this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                    assert packageInfo     != null;
                    assert versionTextView != null;
                    versionTextView.setText(this.getResources().getString(
                            R.string.versiontitle) +
                            " " + packageInfo.versionName);
                }
                catch (final android.content.pm.PackageManager.NameNotFoundException e)
                { }
                versionTextView.setOnClickListener(new android.view.View.OnClickListener()
                {
                    @java.lang.Override
                    public void onClick(final android.view.View v)
                    { MainActivity.this.showChangeLog(); }
                });
            }

            builder.setCancelable(true);
            builder.setTitle     (this.getResources().getString(
                    R.string.about));
            builder.setView(personView);
        }
        builder.setNegativeButton(
                this.getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener()
                {
                    @java.lang.Override
                    public void onClick(final android.content.DialogInterface dialog, final int which)
                    {
                        assert dialog != null;
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void showChangeLog() {

    }

    private void askToSkipOrder() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Skip ordered item?");

        builder.setPositiveButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMatchingOrder++;
            }
        });

        builder.show();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void launchIntro() {

        new Thread(new Runnable() {
            @Override
            public void run() {

            //  Launch app intro
            final Intent i = new Intent(MainActivity.this, IntroActivity.class);

            runOnUiThread(new Runnable() {
                @Override public void run() {
                    startActivity(i);
                }
            });


            }
        }).start();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
