package org.phenoapps.intercross;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuxValueInputActivity extends AppCompatActivity {

    final static private String line_separator = System.getProperty("line.separator");

    private String mTimestamp;
    private String mCrossId;

    private IdEntryDbHelper mDbHelper;

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

    private ArrayList<AdapterEntry> mCrossIds;

    private ActionBarDrawerToggle mDrawerToggle;

    private View focusedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        String crossId = getIntent().getStringExtra("crossId");
       // String[] headers = getIntent().getStringArrayExtra("headers");

        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        mCrossId = crossId;

        String[] headers = prefs.getStringSet(SettingsActivity.HEADER_SET, new HashSet<>()).toArray(new String[] {});
        RecyclerView recyclerView = new RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDbHelper = new IdEntryDbHelper(this);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        HashMap<String, String> colMap = new HashMap<>();

        try {
            String table = IdEntryContract.IdEntry.TABLE_NAME;

            Cursor cursor = db.query(table, headers, "cross_id=?", new String[] {crossId}, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    final String[] headerCols = cursor.getColumnNames();

                    for (String header : headerCols) {


                        final String val = cursor.getString(
                                cursor.getColumnIndexOrThrow(header)
                        );

                        if (header.equals("timestamp")) mTimestamp = val;


                        colMap.put(header, val);

                    }

                } while (cursor.moveToNext());

            }
            cursor.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        List<EditTextAdapterEntry> entries = new ArrayList<>();
        List<EditText> editTexts = new ArrayList<>();

        for (String header : headers) {
            EditText editText = new EditText(this);
            editText.setText(colMap.get(header));
            entries.add(new EditTextAdapterEntry(editText, header));
            editTexts.add(editText);
        }

        EditTextRecyclerViewAdapter adapter = new EditTextRecyclerViewAdapter(this, entries);
        recyclerView.setAdapter(adapter);

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.addView(recyclerView);

        Button submitButton = new Button(this);// = entry.editText;
        submitButton.setText("Update");
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int next = 0;

                for (String header: headers) {
                    db.execSQL("UPDATE INTERCROSS SET " +
                            header + " = '" + editTexts.get(next++).getText().toString() + //adapter.getItem(next++).editText.getText().toString() + //entries.get(next++).editText.getText() +
                            "' WHERE cross_id = '" + crossId + "'");
                }
            }
        });

        view.addView(submitButton);
        setContentView(view);

    }

    @Override
    final public boolean onCreateOptionsMenu(Menu m) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_print_layout, m);
        return true;
    }

    @Override
    final public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_print:
                BluetoothAdapter mBluetoothAdapter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    //TODO allow multiple pairs
                    //TODO wrap in async task
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

                                        Toast.makeText(AuxValueInputActivity.this, "Printer Ready", Toast.LENGTH_LONG).show();

                                    }
                                });

                                //printer.sendCommand("! DF RUN.BAT ! UTILITIES JOURNAL SETFF 50 5 PRINT");
                                //printer.printConfigurationLabel();
                                //printer.sendCommand("^XA^FO0,0^ADN,36,20^FDCHANEY^FS^XZ");
                                printer.sendCommand("^XA"
                                        + "^FWR"
                                        + "^FO100,75^A0,25,20^FD" + mCrossId + "^FS"
                                        + "^FO200,75^A0N,25,20"
                                        + "^BQN,2,10^FDMA" + mCrossId + "^FS"
                                        + "^FO450,75^A0,25,20^FD" + mTimestamp + "^FS^XZ");
                                /*printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                        R.drawable.intercross_small)), 75,500,-1,-1,false);*/

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

        AuxValueInputActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(AuxValueInputActivity.this,
                        displayPrinterLanguage + "\n" + "Language set to ZPL", Toast.LENGTH_LONG).show();

            }
        });

    }

}
