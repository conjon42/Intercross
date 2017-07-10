package edu.ksu.wheatgenetics.verify;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
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
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Context _ctx;
    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private String _prevIdLookup;
    private int _matchingOrder;
    private NotificationManager _notificationManager;
    private NotificationCompat.Builder _builder;
    private Timer mTimer = new Timer("user input for suppressing messages", true);
    private TextView valueView;


    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private DrawerLayout mDrawerLayout;
    NavigationView nvDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().getThemedContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Setup drawer view
        setupDrawerContent(nvDrawer);
        setupDrawer();

        ActivityCompat.requestPermissions(this, VerifyConstants.permissions, VerifyConstants.PERM_REQ);

        _ctx = this;

        _matchingOrder = 0;

        if (_ids == null)
            _ids = new SparseArray<>();

        if (_cols == null)
            _cols = new SparseArray<>();

        _notificationManager = (NotificationManager) _ctx.getSystemService(NOTIFICATION_SERVICE);
        _builder = new NotificationCompat.Builder(_ctx.getApplicationContext())
                .setSmallIcon(R.drawable.ic_action_camera)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        final EditText et = ((EditText) findViewById(R.id.scannerTextView));
        final ListView lv = ((ListView) findViewById(R.id.idTable));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                lv.setItemChecked(position, false);
                et.setText(((TextView) view).getText().toString());
            }
        });

        valueView = (TextView) findViewById(R.id.valueView);
        valueView.setMovementMethod(new ScrollingMovementMethod());

        et.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                mTimer.purge();
                mTimer.cancel();

                //check if there are at least 4 digits and an id to check for
                if (s.length() >= 3 && _ids.size() != 0) {

                    final int size = _ids.size();
                    /*
                    scan list of ids for updated text id input
                     */
                    int found = -1;
                    for (int i = 0; i < size; i = i + 1) {
                        if (s.toString().equals(_ids.get(_ids.keyAt(i)))) {
                            found = i;
                            valueView.setText(_cols.get(_cols.keyAt(i)));
                            break;
                        }
                    }

                    if (found == -1) {
                        mTimer.purge();
                        mTimer.cancel();
                        mTimer = new Timer("user input for suppressing messages", true);
                        mTimer.schedule(new SuppressMessageTask(), 3000);
                    } else {
                        //cancel all invalid messages
                        mTimer.purge();
                        mTimer.cancel();
                        updateListView(s.toString(), found);
                    }
                }
            }

            /* DFA for scan state */
            private void updateListView(String id, int found) {

                //get app settings
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_ctx);
                final int scanMode = Integer.valueOf(sharedPref.getString(SettingsActivity.SCAN_MODE_LIST, "-1"));
                final ListView lv = ((ListView) findViewById(R.id.idTable));

                switch(scanMode) {

                    case 0: //matching mode
                        lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                        lv.clearChoices();
                        if (_matchingOrder == found) {
                            _notificationManager.notify(0, _builder.build());
                            _matchingOrder++;
                            _notificationManager.notify(0, _builder.build());
                            Toast.makeText(_ctx, "Order matches id: " + id + " at index: " + found, Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(_ctx, "Scanning out of order!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: //filter mode
                        _matchingOrder = 0;
                        lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                        lv.clearChoices();
                        final ArrayAdapter<String> oldAdapter = (ArrayAdapter<String>) lv.getAdapter();
                        final ArrayAdapter<String> updatedAdapter = new ArrayAdapter<String>(_ctx, R.layout.row);
                        final int oldSize = oldAdapter.getCount();

                        for (int i = 0; i < oldSize; i = i + 1) {
                            if (i != found) {
                                updatedAdapter.add(oldAdapter.getItem(i));
                            }
                        }
                        lv.setAdapter(updatedAdapter);

                        _ids.remove(_ids.keyAt(found));
                        _cols.remove(_cols.keyAt(found));

                        _notificationManager.notify(0, _builder.build());
                        Toast.makeText(_ctx, "Removing scanned item: " + id, Toast.LENGTH_SHORT).show();

                        break;
                    case 2: //color mode
                        _matchingOrder = 0;
                        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                        lv.setItemChecked(found, true);

                        _notificationManager.notify(0, _builder.build());
                        Toast.makeText(_ctx, "Coloring scanned item: " + id, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        _notificationManager.notify(0, _builder.build());
                        Toast.makeText(_ctx, "Scanned id found: " + id, Toast.LENGTH_SHORT).show();
                        _matchingOrder = 0;
                        lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                        lv.clearChoices();
                }
            }
        });

        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });

        if (savedInstanceState != null) {

            buildListViewFromIntent(getIntent());
        }

        final File dir = _ctx.getDir("Verify", Context.MODE_PRIVATE);
        Log.d("directory", dir.getAbsolutePath().toString());
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

                Toast.makeText(_ctx, "Scanned id not found", Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.valueView)).setText("");
            }
        } );
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
                    ((TextView) findViewById(R.id.scannerTextView))
                            .setText(intent.getStringExtra(VerifyConstants.CAMERA_RETURN_ID));
                }
            }
        }
    }

    private void buildListViewFromIntent(Intent intent) {

        //get intent array list messages (columns and keys)
        final ArrayList<String> colMsg = intent.getStringArrayListExtra(VerifyConstants.COL_ARRAY_EXTRA);
        final ArrayList<String> keyMsg = intent.getStringArrayListExtra(VerifyConstants.ID_ARRAY_EXTRA);

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

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerOpened(View drawerView) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            public void onDrawerClosed(View view) {
            }

        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {

            case R.id.nav_import:
                final Intent i = new Intent(this, LoaderActivity.class);
                startActivityForResult(i, VerifyConstants.LOADER_INTENT_REQ);
                break;
            case R.id.nav_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, VerifyConstants.SETTINGS_INTENT_REQ);
                break;
            case R.id.nav_export:
                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                break;
        }

        mDrawer.closeDrawers();
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
}
