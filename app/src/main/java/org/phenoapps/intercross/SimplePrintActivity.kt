package org.phenoapps.intercross

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.util.*
import kotlin.collections.HashMap

class SimplePrintActivity : AppCompatActivity() {

    private val mPrintButton: Button by lazy {
        findViewById<Button>(R.id.printButton)
    }

    private val mEditText: EditText by lazy {
        findViewById<EditText>(R.id.editText)
    }

    private lateinit var mNavView: NavigationView

    override fun onStart() {
        super.onStart()

        mPrintButton.setOnClickListener {
            if (mEditText.text.toString().isNotBlank()) {
                    BluetoothUtil(this@SimplePrintActivity)
                            .variablePrint("^XA"
                                    + "^MNA"
                                    + "^MMT,N"
                                    + "^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS"
                                    + "^FWR"
                                    + "^FO100,25^A0,25,20^FN1^FS"
                                    + "^FO200,25^A0N,25,20"
                                    + "^BQ,2,6" +
                                    "^FN2^FS"
                                    + "^FO450,25^A0,25,20^FN3^FS^XZ",
                                    "^XA"
                                            + "^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF"
                                            + "^FN1^FD" + mEditText.text.toString() + "^FS"
                                            + "^FN2^FDQA," + mEditText.text.toString() + "^FS^XZ")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle("Simple Print")

        setContentView(R.layout.activity_simple_print)

        supportActionBar?.let {
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        mNavView = findViewById(R.id.nvView) as NavigationView

        // Setup drawer view
        setupDrawer()

    }

    private fun setupDrawer() {
        val dl = findViewById(org.phenoapps.intercross.R.id.drawer_layout) as DrawerLayout
        dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            //else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun prepareBluetooth(f: () -> Unit) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val btId = pref.getString(SettingsActivity.BT_ID, "")
        if ((btId ?: "").isBlank()) {

            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


            val pairedDevices = mBluetoothAdapter.bondedDevices

            val input = RadioGroup(this)

            pairedDevices.forEach {
                val button = RadioButton(this)
                button.text = it.name
                input.addView(button)
            }

            val builder = androidx.appcompat.app.AlertDialog.Builder(this.applicationContext).apply {

                setTitle("Choose bluetooth device to print from.")

                setView(input)

                setNegativeButton("Cancel") { _, _ ->

                }

                setPositiveButton("OK") { _, _ ->

                    if (input.checkedRadioButtonId == -1) return@setPositiveButton

                    val edit = pref.edit()
                    edit.putString(SettingsActivity.BT_ID,
                            input.findViewById<RadioButton>(input.checkedRadioButtonId).text.toString())
                    edit.apply()

                    f()
                }
            }

            builder.show()
        }
    }

    //main function to be called, will ask user to choose bluetooth device from radio group, and then print
    fun variablePrint(template: String, code: String) {

        prepareBluetooth {
            Thread(PrintThread(this, template, code)).start()
        }
    }
}
