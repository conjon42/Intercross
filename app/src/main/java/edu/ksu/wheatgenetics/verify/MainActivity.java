package edu.ksu.wheatgenetics.verify;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static edu.ksu.wheatgenetics.verify.VerifyConstants.*;

public class MainActivity extends AppCompatActivity {

    private Context _ctx;
    private SparseArray<String> _ids;
    private SparseArray<String> _cols;
    private String _prevIdLookup;
    private int _matchingOrder;
    private NotificationManager _notificationManager;
    private NotificationCompat.Builder _builder;
    private Timer mTimer = new Timer("user input for suppressing messages", true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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
                if (s.length() >= 4 && _ids.size() != 0) {

                    final int size = _ids.size();
                    final TextView tv = (TextView) findViewById(R.id.valueView);
                    /*
                    scan list of ids for updated text id input
                     */
                    int found = -1;
                    for (int i = 0; i < size; i = i + 1) {
                        if (s.toString().equals(_ids.get(_ids.keyAt(i)))) {
                            found = i;
                            tv.setText(_cols.get(_cols.keyAt(i)));
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
            case R.id.action_open:
                final Intent i = new Intent(this, LoaderActivity.class);
                startActivityForResult(i, VerifyConstants.LOADER_INTENT_REQ);
                return true;
            case R.id.action_camera:
                final Intent cameraIntent = new Intent(this, CaptureActivity.class);
                startActivityForResult(cameraIntent, VerifyConstants.CAMERA_INTENT_REQ);
                return true;
            case R.id.action_settings:
                final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, VerifyConstants.SETTINGS_INTENT_REQ);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case VerifyConstants.LOADER_INTENT_REQ:
                    buildListViewFromIntent(intent);
                    break;
                case CAMERA_INTENT_REQ:
                    if (intent.hasExtra(VerifyConstants.CAMERA_RETURN_ID)) {
                        ((TextView) findViewById(R.id.scannerTextView))
                                .setText(intent.getStringExtra(VerifyConstants.CAMERA_RETURN_ID));
                    }
                    break;
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
}
