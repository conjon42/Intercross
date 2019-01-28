package org.phenoapps.intercross

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog


//Bluetooth Utility class for printing ZPL code and choosing bluetooth devices to print from.
class BluetoothUtil {

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    //operation that uses the provided context to prompt the user for a paired bluetooth device
    private fun choose(ctx: Context, f: () -> Unit) {

        //check for bluetooth id updates in the settings
        val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
        val btId = pref.getString(SettingsActivity.BT_ID, "")

        //if the user has not previously chosen a bluetooth device, they must choose one
        if ((btId ?: "").isBlank()) {

            /*Filter out some classes of bluetooth devices
            mBluetoothAdapter.bondedDevices.forEach {
                when(it?.bluetoothClass?.majorDeviceClass) {
                    //BluetoothClass.Device.Major.AUDIO_VIDEO -> Log.d("BTAUDIO_VIDEO", it?.bluetoothClass.toString())
                    BluetoothClass.Device.Major.COMPUTER -> Log.d("BTCOMPUTER", it.bluetoothClass.toString())
                    //BluetoothClass.Device.Major.HEALTH -> Log.d("BTHEALTH", it?.bluetoothClass.toString())
                    BluetoothClass.Device.Major.IMAGING -> Log.d("BTIMAGING", it.bluetoothClass.toString())
                    BluetoothClass.Device.Major.MISC -> Log.d("BTMISC", it.bluetoothClass.toString())
                    BluetoothClass.Device.Major.NETWORKING -> Log.d("BTNETWORKING", it.bluetoothClass.toString())
                    BluetoothClass.Device.Major.PERIPHERAL -> Log.d("BTPERIPHERAL", it.bluetoothClass.toString())
                    BluetoothClass.Device.Major.PHONE -> Log.d("BTPHONE", it.bluetoothClass.toString())
                    //BluetoothClass.Device.Major.TOY -> Log.d("BTTOY", it?.bluetoothClass.toString())
                    BluetoothClass.Device.Major.UNCATEGORIZED -> Log.d("BTUNCATEGORIZED", it.bluetoothClass.toString())
                    BluetoothClass.Device.Major.WEARABLE -> Log.d("BTWEARABLE", it.bluetoothClass.toString())
                }
            }*/

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
    fun variablePrint(ctx: Context, template: String, code: String) {

        choose(ctx) {
            Thread(PrintThread(ctx, template, code)).start()
        }
    }
}