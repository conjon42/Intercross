package edu.ksu.wheatgenetics.verify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.util.Log;
import android.util.SparseArray;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private SparseArray<String> _checkedIds;

    private File mVerifyDirectory;
    private int _matchingOrder;
    private Timer mTimer = new Timer("user input for suppressing messages", true);
    private TextView valueView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Ringtone mRingtoneNoti;
    private Uri mRingtoneUri;
    private EditText mScannerTextView;
    private ListView mIdTable;
    NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final boolean tutorialMode = sharedPref.getBoolean(SettingsActivity.TUTORIAL_MODE, true);

        if (tutorialMode)
            launchIntro();

        ActivityCompat.requestPermissions(this, VerifyConstants.permissions, VerifyConstants.PERM_REQ);

        _matchingOrder = 0;
        _ids = new SparseArray<>();
        _cols = new SparseArray<>();
        _checkedIds = new SparseArray<>();

        mIdTable = ((ListView) findViewById(R.id.idTable));
        mIdTable.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mIdTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mScannerTextView.setText(((TextView) view).getText().toString());
                checkScannedItem();
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

        //final File dir = this.getDir("Verify", Context.MODE_PRIVATE);
        //Log.d("directory", dir.getAbsolutePath());
    }

    private void checkScannedItem() {

        final String scannedId = mScannerTextView.getText().toString();
        mTimer.purge();
        mTimer.cancel();

        final int size = _ids.size();
                /*
                scan list of ids for updated text id input
                 */
        int found = -1;
        for (int i = 0; i < size; i = i + 1) {
            if (scannedId.equals(_ids.get(_ids.keyAt(i)))) {
                found = i;
                valueView.setText(_cols.get(_cols.keyAt(i)));
                break;
            }
        }

        //get app settings
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));

        for (int position = 0; position < mIdTable.getCount(); position++) {

            final String id = (mIdTable.getItemAtPosition(position)).toString();
            if (id.equals(scannedId)) {
                switch (scanMode) {

                    case 0: //default

                    case 1: //matching mode

                    case 2: //filter mode
                        mIdTable.setItemChecked(position, !mIdTable.isItemChecked(position));
                        break;
                    case 3: //color mode
                        _checkedIds.append(_checkedIds.size(), scannedId);
                        mIdTable.setItemChecked(position, true);
                        break;
                }
            }
        }

        if (found == -1) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = new Timer("user input for suppressing messages", true);
            mTimer.schedule(new SuppressMessageTask(), 0);
        } else {
            //cancel all invalid messages
            mTimer.purge();
            mTimer.cancel();
            updateListView(scannedId, found);
        }
    }

    /* DFA for scan state */
    private void updateListView(String id, int found) {

        //get app settings
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));

        switch (scanMode) {

            case 0: //default
                ringNotification();
                Toast.makeText(this, "Scanned id found: " + id, Toast.LENGTH_SHORT).show();
                _matchingOrder = 0;
                break;
            case 1: //matching mode
                if (_matchingOrder == found) {
                    ringNotification();
                    _matchingOrder++;
                    Toast.makeText(this, "Order matches id: " + id + " at index: " + found, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Scanning out of order!", Toast.LENGTH_SHORT).show();
                break;
            case 2: //filter mode
                _matchingOrder = 0;

                final ArrayAdapter<String> oldAdapter = (ArrayAdapter<String>) mIdTable.getAdapter();
                final ArrayAdapter<String> updatedAdapter = new ArrayAdapter<>(this, R.layout.row);
                final int oldSize = oldAdapter.getCount();

                for (int i = 0; i < oldSize; i = i + 1) {
                    if (i != found) {
                        updatedAdapter.add(oldAdapter.getItem(i));
                    }
                }
                mIdTable.setAdapter(updatedAdapter);

                for (int i = 0; i < mIdTable.getCount(); i = i + 1) {
                    for (int j = 0; j < _checkedIds.size(); j = j + 1) {
                        final String checkedJ = _checkedIds.get(_checkedIds.keyAt(j));
                        final String checkedI = ((TextView) mIdTable.getAdapter().getView(i, null, null)).getText().toString();
                        if (checkedI.equals(checkedJ)) {
                            mIdTable.setItemChecked(i, true);
                        }
                    }
                }
                _ids.remove(_ids.keyAt(found));
                _cols.remove(_cols.keyAt(found));

                ringNotification();
                Toast.makeText(this, "Removing scanned item: " + id, Toast.LENGTH_SHORT).show();

                break;
            case 3: //color mode
                _matchingOrder = 0;
                ringNotification();
                Toast.makeText(this, "Coloring scanned item: " + id, Toast.LENGTH_SHORT).show();
                break;
        }
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
                        buildListViewFromIntent(intent);
                        break;
                }

                if (intent.hasExtra(VerifyConstants.CAMERA_RETURN_ID)) {
                    mScannerTextView.setText(intent.getStringExtra(VerifyConstants.CAMERA_RETURN_ID));
                    checkScannedItem();
                }
            }
        }
    }

    private void buildListViewFromIntent(Intent intent) {

        //get intent array list messages (columns and keys)
        final ArrayList<String> colMsg = intent.getStringArrayListExtra(VerifyConstants.COL_ARRAY_EXTRA);
        final ArrayList<String> keyMsg = intent.getStringArrayListExtra(VerifyConstants.ID_ARRAY_EXTRA);

        buildListView(colMsg, keyMsg);
    }

    private void buildListView(ArrayList<String> colMsg, ArrayList<String> keyMsg) {

        //convert messages to global sparse arrays
        final int kMsgSize = keyMsg.size();
        for (int j = 0; j < kMsgSize; j = j + 1)
            _ids.append(j, keyMsg.get(j));

        final int cMsgSize = colMsg.size();
        for (int j = 0; j < cMsgSize; j = j + 1)
            _cols.append(j, colMsg.get(j));


        final ArrayAdapter<String> idAdapter =
                new ArrayAdapter<>(this, R.layout.row);
        final int size = _ids.size();
        for (int i = 0; i < size; i = i + 1)
            idAdapter.add(_ids.get(i));
        mIdTable.setAdapter(idAdapter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ArrayList<String> cols, keys;
        cols = new ArrayList<>();
        keys = new ArrayList<>();
        final int size = _ids.size();
        for (int i = 0; i < size; i = i + 1) {
            keys.add(i, _ids.get(_ids.keyAt(i)));
            cols.add(i, _cols.get(_cols.keyAt(i)));
        }
        outState.putStringArrayList(VerifyConstants.ID_ARRAY_EXTRA, keys);
        outState.putStringArrayList(VerifyConstants.COL_ARRAY_EXTRA, cols);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final ArrayList<String> colMsg = savedInstanceState.getStringArrayList(VerifyConstants.COL_ARRAY_EXTRA);
        final ArrayList<String> keyMsg = savedInstanceState.getStringArrayList(VerifyConstants.ID_ARRAY_EXTRA);
        buildListView(colMsg, keyMsg);
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
                final Intent i = new Intent(this, LoaderActivity.class);
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
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
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

    private void askUserExportFileName() {

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
                            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));
                            final File output = new File(mVerifyDirectory, value);
                            final FileOutputStream fstream = new FileOutputStream(output);

                            if (_ids != null && _ids.size() > 0) {
                                //write header line
                                fstream.write("id".getBytes());
                                if (scanMode == 3) fstream.write(",checked\n".getBytes());
                                for (int i = 0; i < mIdTable.getCount(); i = i + 1) {
                                    final String id = ((TextView) mIdTable.getAdapter().getView(i, null, null)).getText().toString();
                                    fstream.write(id.getBytes());
                                    if (scanMode == 3) {
                                        for (int j = 0; j < _checkedIds.size(); j = j + 1) {
                                            final String checked = _checkedIds.get(_checkedIds.keyAt(j));
                                            if (id.equals(checked)) {
                                                fstream.write(",checked".getBytes());
                                            }
                                        }
                                    }
                                    fstream.write("\n".getBytes());
                                }
                                fstream.flush();
                                fstream.close();
                            }
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } //error toast
                } //use default name
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

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
