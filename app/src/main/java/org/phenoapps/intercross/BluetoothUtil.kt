package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.preference.PreferenceManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog


//Bluetooth Utility class for printing ZPL code and choosing bluetooth devices to print from.
class BluetoothUtil(val ctx: Context) {

    fun prepareBluetooth(f: () -> Unit) {

        val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
        val btId = pref.getString(SettingsActivity.BT_ID, "")
        if ((btId ?: "").isBlank()) {

            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


            val pairedDevices = mBluetoothAdapter.bondedDevices

            val input = RadioGroup(ctx)

            pairedDevices.forEach {
                val button = RadioButton(ctx)
                button.text = it.name
                input.addView(button)
            }

            val builder = AlertDialog.Builder(ctx).apply {

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
        } else f()
    }

    //main function to be called, will ask user to choose bluetooth device from radio group, and then print
    fun variablePrint(template: String, code: String) {

        prepareBluetooth {
            Thread(PrintThread(ctx, template, code)).start()
        }
    }
}