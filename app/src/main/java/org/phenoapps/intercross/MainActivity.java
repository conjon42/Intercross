package org.phenoapps.intercross;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    final static private String line_separator = System.getProperty("line.separator");

    private IdEntryDbHelper mDbHelper;

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    private ArrayList<AdapterEntry> mCrossIds;

    private ActionBarDrawerToggle mDrawerToggle;

    private View focusedTextView;

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mCrossIds = new ArrayList<>();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(mPrefListener);

        if (!sharedPref.getBoolean("onlyLoadTutorialOnce", false)) {
            launchIntro();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("onlyLoadTutorialOnce", true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                editor.apply();
            }
        } else {
            boolean tutorialMode = sharedPref.getBoolean(SettingsActivity.TUTORIAL_MODE, false);

            if (tutorialMode)
                launchIntro();
        }

        ActivityCompat.requestPermissions(this, IntercrossConstants.permissions, IntercrossConstants.PERM_REQ);

        initializeUIVariables();

        mDbHelper = new IdEntryDbHelper(this);

        loadSQLToLocal();

    }

    public static void scanFile(Context ctx, File filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            MediaScannerConnection.scanFile(ctx, new String[] { filePath.getAbsolutePath()}, null, null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initializeUIVariables() {

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);
        setupDrawer();

        EditText maleEditText = ((EditText) findViewById(R.id.editTextMale));
        EditText femaleEditText = ((EditText) findViewById(R.id.editTextFemale));
        EditText crossEditText = ((EditText) findViewById(R.id.editTextCross));
        Button saveButton = ((Button) findViewById(R.id.saveButton));

        //single text watcher class to check if all fields are non-empty to enable the save button
        TextWatcher emptyGuard = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (maleEditText.getText().length() != 0
                        && femaleEditText.getText().length() != 0
                        && crossEditText.getText().length() != 0) {
                    saveButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        View.OnFocusChangeListener focusChanged = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                focusedTextView = view;
            }
        };

        maleEditText.addTextChangedListener(emptyGuard);
        femaleEditText.addTextChangedListener(emptyGuard);
        crossEditText.addTextChangedListener(emptyGuard);

        crossEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    saveToDB();
                    focusedTextView = femaleEditText;
                    focusedTextView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        maleEditText.setOnFocusChangeListener(focusChanged);
        femaleEditText.setOnFocusChangeListener(focusChanged);
        crossEditText.setOnFocusChangeListener(focusChanged);

        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToDB();
                focusedTextView = femaleEditText;
                focusedTextView.requestFocus();
            }
        });

        ((Button) findViewById(R.id.clearButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) findViewById(R.id.editTextCross)).setText("");
                ((EditText) findViewById(R.id.editTextMale)).setText("");
                ((EditText) findViewById(R.id.editTextFemale)).setText("");
                focusedTextView = femaleEditText;
                focusedTextView.requestFocus();
            }
        });

        focusedTextView = femaleEditText;
        focusedTextView.requestFocus();
    }

    private void saveToDB() {

        EditText maleEditText = ((EditText) findViewById(R.id.editTextMale));
        EditText femaleEditText = ((EditText) findViewById(R.id.editTextFemale));
        EditText crossEditText = ((EditText) findViewById(R.id.editTextCross));

        if (maleEditText.getText().length() != 0
                && femaleEditText.getText().length() != 0
                && crossEditText.getText().length() != 0) {

            //database update
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues entry = new ContentValues();

            entry.put("male", String.valueOf(maleEditText.getText()));
            entry.put("female", String.valueOf(femaleEditText.getText()));
            entry.put("cross_id", String.valueOf(crossEditText.getText()));

            final Calendar c = Calendar.getInstance();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

            //String firstName = sharedPref.getString(SettingsActivity.FIRST_NAME, "");

            entry.put("timestamp", String.valueOf(sdf.format(c.getTime())));

            entry.put("person", "not implemented.");

            db.insert("INTERCROSS", null, entry);

            //clear fields
            maleEditText.getText().clear();
            femaleEditText.getText().clear();
            crossEditText.getText().clear();

            loadSQLToLocal();
        }
    }

    //reads the SQLite database and displays cross ids and their total counts / timestamps
    private void loadSQLToLocal() {

        mDbHelper = new IdEntryDbHelper(this);

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

                        AdapterEntry entry = new AdapterEntry(crossId, timestamp);

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

    private void askUserExportFileName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose name for exported file.");
        final EditText input = new EditText(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        builder.setView(input);

        builder.setPositiveButton("Export", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                if (value.length() != 0) {
                    if (isExternalStorageWritable()) {
                        try {
                            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Intercross");
                            final File output = new File(dir, value + ".csv");
                            final FileOutputStream fstream = new FileOutputStream(output);
                            final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                            final String table = IdEntryContract.IdEntry.TABLE_NAME;
                            final Cursor cursor = db.query(table, null, null, null, null, null, null);
                            //final Cursor cursor = db.rawQuery("SElECT * FROM VERIFY", null);

                            //first write header line
                            final String[] headers = cursor.getColumnNames();
                            for (int i = 0; i < headers.length; i++) {
                                if (i != 0) fstream.write(",".getBytes());
                                fstream.write(headers[i].getBytes());
                            }
                            fstream.write(line_separator.getBytes());
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
                                    fstream.write(line_separator.getBytes());
                                } while (cursor.moveToNext());
                            }

                            scanFile(MainActivity.this, output);

                            cursor.close();
                            fstream.flush();
                            fstream.close();
                        } catch (SQLiteException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error exporting file, is your table empty?", Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException io) {
                            io.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "External storage not writable.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this,
                            "Must enter a file name.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.show();

    }

    @Override
    final public boolean onCreateOptionsMenu(Menu m) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(org.phenoapps.intercross.R.menu.activity_main_toolbar, m);
        return true;
    }

    @Override
    final public boolean onOptionsItemSelected(MenuItem item) {

        DrawerLayout dl = (DrawerLayout) findViewById(org.phenoapps.intercross.R.id.drawer_layout);
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                dl.openDrawer(GravityCompat.START);
                break;
            case org.phenoapps.intercross.R.id.action_camera:
                final Intent cameraIntent = new Intent(this, ScanActivity.class);
                startActivityForResult(cameraIntent, IntercrossConstants.CAMERA_INTENT_REQ);
                break;
            case R.id.action_count:
                final Intent countIntent = new Intent(this, CountActivity.class);
                startActivity(countIntent);
                break;
            case R.id.action_print:
                BluetoothAdapter mBluetoothAdapter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() == 1) {
                        BluetoothDevice bd = pairedDevices.toArray(new BluetoothDevice[] {})[0];
                        Log.d("BT", "PAIRED");
                        BluetoothConnection bc = new BluetoothConnection(bd.getAddress());
                        try {
                            bc.open();
                            final ZebraPrinter printer = ZebraPrinterFactory.getInstance(bc);
                            ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
                            PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
                            getPrinterStatus(bc);
                            if (printerStatus.isReadyToPrint) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(MainActivity.this, "Printer Ready", Toast.LENGTH_LONG).show();

                                    }
                                });

                                //printer.sendCommand("! DF RUN.BAT ! UTILITIES JOURNAL SETFF 50 5 PRINT");
                                //printer.printConfigurationLabel();
                                //printer.sendCommand("^XA^FO0,0^ADN,36,20^FDCHANEY^FS^XZ");
                                printer.sendCommand("^XA^FWR^FO150,90^A0N,25,20^FD" + "^FS^FO115,75^A0,25,20^FD0123456789^FS^FO150,115^A0N,25,20^FD333PhenoAppsHackathon^FS^FO400,75^A0,25,20^FDIntercross^FS^XZ");
                                printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                        R.drawable.intercross_small)), 50,50,-1,-1,false);

                            } else if (printerStatus.isHeadOpen) {
                                //helper.showErrorMessage("Error: Head Open \nPlease Close Printer Head to Print");
                            } else if (printerStatus.isPaused) {
                                //helper.showErrorMessage("Error: Printer Paused");
                            } else if (printerStatus.isPaperOut) {
                                //helper.showErrorMessage("Error: Media Out \nPlease Load Media to Print");
                            } else {
                                //helper.showErrorMessage("Error: Please check the Connection of the Printer");
                            }

                            bc.close();

                        } catch (ConnectionException e) {
                            e.printStackTrace();
                        } catch (ZebraPrinterLanguageUnknownException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void getPrinterStatus(BluetoothConnection connection) throws ConnectionException{


        final String printerLanguage = SGD.GET("device.languages", connection); //This command is used to get the language of the printer.

        final String displayPrinterLanguage = "Printer Language is " + printerLanguage;

        SGD.SET("device.languages", "zpl", connection); //This command set the language of the printer to ZPL

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, displayPrinterLanguage + "\n" + "Language set to ZPL", Toast.LENGTH_LONG).show();

            }
        });

    }

    @Override
    final protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {

            if (intent != null) {
                switch (requestCode) {

                    //File data response from import request
                    case IntercrossConstants.DEFAULT_CONTENT_REQ:
                        Intent i = new Intent(this, LoaderDBActivity.class);
                        loadFileToSQL(intent.getData());
                        loadSQLToLocal();
                        break;
                }

                //barcode text response from Zebra intent
                if (intent.hasExtra(IntercrossConstants.CAMERA_RETURN_ID)) {
                    if (focusedTextView == null) {
                        focusedTextView = findViewById(R.id.editTextMale);

                    }
                    ((EditText) focusedTextView)
                            .setText(intent.getStringExtra(IntercrossConstants.CAMERA_RETURN_ID));

                    if (focusedTextView == findViewById(R.id.editTextMale)) {
                        focusedTextView = findViewById(R.id.editTextFemale);
                    } else if (focusedTextView == findViewById(R.id.editTextFemale)) {
                        focusedTextView = findViewById(R.id.editTextCross);
                    } else {
                        focusedTextView = findViewById(R.id.editTextMale);
                    }
                    focusedTextView.requestFocus();
                }
            }
        }
    }

    private void loadFileToSQL(Uri data) {


        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.execSQL(IdEntryContract.SQL_DELETE_ENTRIES);

        db.execSQL(IdEntryContract.SQL_CREATE_ENTRIES);

        try {
            InputStream is = getContentResolver().openInputStream(data);
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String[] headerLine = br.readLine().split(",");
                String temp = null;
                ContentValues entry = new ContentValues();
                db.beginTransaction();
                while ((temp = br.readLine()) != null) {
                    String[] row = temp.split(",");
                    int size = row.length;
                    for (int i = 0; i < size; i++) {
                        entry.put(headerLine[i], row[i]);
                    }
                    long newRowId = db.insert("INTERCROSS", null, entry);
                    entry.clear();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                br.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void buildListView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.crossList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CrossRecyclerViewAdapter adapter = new CrossRecyclerViewAdapter(this, mCrossIds);
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void clearListView() {

        ListView idTable = (ListView) findViewById(R.id.crossList);
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, org.phenoapps.intercross.R.layout.row);

        idTable.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setupDrawer() {

        DrawerLayout dl = (DrawerLayout) findViewById(org.phenoapps.intercross.R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, dl,
                org.phenoapps.intercross.R.string.drawer_open, org.phenoapps.intercross.R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }

            public void onDrawerClosed(View view) {
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(mDrawerToggle);
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

    private void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case org.phenoapps.intercross.R.id.nav_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, IntercrossConstants.SETTINGS_INTENT_REQ);
                break;
            case org.phenoapps.intercross.R.id.nav_export:
                askUserExportFileName();
                break;
            case org.phenoapps.intercross.R.id.nav_about:
                showAboutDialog();
                break;
            case org.phenoapps.intercross.R.id.nav_intro:
                final Intent intro_intent = new Intent(MainActivity.this, IntroActivity.class);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        startActivity(intro_intent);
                    }
                });
                break;
            case org.phenoapps.intercross.R.id.nav_manage_headers:
                Intent nav_manage_headers = new Intent(MainActivity.this, ManageHeadersActivity.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(nav_manage_headers);
                    }
                });
                break;
        }

        DrawerLayout dl = (DrawerLayout) findViewById(org.phenoapps.intercross.R.id.drawer_layout);
        dl.closeDrawers();
    }

    private void showAboutDialog()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        {
            android.view.View personView = this.getLayoutInflater().inflate(
                    org.phenoapps.intercross.R.layout.about, new android.widget.LinearLayout(this),
                    false);

            {
                assert personView != null;
                android.widget.TextView versionTextView = (android.widget.TextView)
                        personView.findViewById(org.phenoapps.intercross.R.id.tvVersion);
                try
                {
                    android.content.pm.PackageInfo packageInfo =
                            this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                    assert packageInfo     != null;
                    assert versionTextView != null;
                    versionTextView.setText(this.getResources().getString(
                            org.phenoapps.intercross.R.string.versiontitle) +
                            ' ' + packageInfo.versionName);
                }
                catch (android.content.pm.PackageManager.NameNotFoundException e)
                { e.printStackTrace(); }
                versionTextView.setOnClickListener(new android.view.View.OnClickListener()
                {
                    @java.lang.Override
                    public void onClick(android.view.View v)
                    { MainActivity.this.showChangeLog(); }
                });
            }

            builder.setCancelable(true);
            builder.setTitle     (this.getResources().getString(
                    org.phenoapps.intercross.R.string.about));
            builder.setView(personView);
        }

        builder.setNegativeButton(
                this.getResources().getString(org.phenoapps.intercross.R.string.ok),
                new android.content.DialogInterface.OnClickListener()
                {
                    @java.lang.Override
                    public void onClick(android.content.DialogInterface dialog, int which)
                    {
                        assert dialog != null;
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    private void showChangeLog() {

    }

    @Override
    final protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    final public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void launchIntro() {

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
    static private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    @Override
    final public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String[] permissions, int[] granted) {

        boolean allGranted = true;
        boolean externalWriteAccept = false;
        if (resultCode == IntercrossConstants.PERM_REQ) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE")
                        && granted[i] == PackageManager.PERMISSION_GRANTED) {
                    externalWriteAccept = true;
                }
                if (granted[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                }
            }
        }
        if (externalWriteAccept && isExternalStorageWritable()) {
            File intercrossDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Intercross");
            if (!intercrossDir.isDirectory()) {
                final boolean makeDirsSuccess = intercrossDir.mkdirs();
                if (!makeDirsSuccess) Log.d("Make Directory", "failed");
            }
            //copyRawToVerify(intercrossDir, "field_sample.csv", R.raw.field_sample);
            //copyRawToVerify(intercrossDir, "verify_pair_sample.csv", R.raw.verify_pair_sample);
        }
        if (!allGranted) {
            Toast.makeText(this, "You must accept all permissions to continue.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            loadSQLToLocal();
        }
    }
}
