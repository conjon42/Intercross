package org.phenoapps.intercross.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.PollenGroup


//Bluetooth Utility class for printing ZPL code and choosing bluetooth devices to print from.
class BluetoothUtil {

    private var mBtName: String = String()

    private val mBluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    //operation that uses the provided context to prompt the user for a paired bluetooth device
    private fun choose(ctx: Context, f: () -> Unit) {

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

        //val btId = pref.getString(SettingsActivity.BT_ID, "")

        if (mBtName.isBlank()) {

            mBluetoothAdapter?.let {
                val pairedDevices = it.bondedDevices

                val map = HashMap<Int, BluetoothDevice>()

                val input = RadioGroup(ctx)

                pairedDevices.forEachIndexed { i, t ->
                    val button = RadioButton(ctx)
                    button.text = t.name
                    input.addView(button)
                    map[button.id] = t
                }

                val builder = AlertDialog.Builder(ctx).apply {

                    setTitle("Choose bluetooth device to print from.")

                    setView(input)

                    setNegativeButton("Cancel") { _, _ ->

                    }

                    setPositiveButton("OK") { _, _ ->

                        if (input.checkedRadioButtonId == -1) return@setPositiveButton
                        else {
                            //              PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                            //                    .putString(SettingsActivity.BT_ID, map[input.checkedRadioButtonId]?.name)
                            //                  .apply()
                            mBtName = map[input.checkedRadioButtonId]?.name ?: ""
                        }
                        f()
                    }
                }

                builder.show()
            }

        } else f()
    }

    var template = "^XA^MNA^MMT,N^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS^FWR^FO100,25^A0,25,20^FN1^FS^FO200,25^A0N,25,20^BQ,2,6^FN2^FS^FO450,25^A0,25,20^FN3^FS^XZ"

    fun print(ctx: Context, events: Array<Events>) {
        choose(ctx) {
            PrintThread(ctx, template, mBtName).printEvents(events)
        }
    }

    fun print(ctx: Context, group: PollenGroup) {
        choose(ctx) {
            PrintThread(ctx, template, mBtName).printGroup(group)
        }
    }

    fun templatePrint(ctx: Context, events: Array<Events>) {
        choose(ctx) {
            val pref = PreferenceManager.getDefaultSharedPreferences(ctx)
            PrintThread(ctx, pref.getString("ZPL_CODE", template) ?: template, mBtName).printEvents(events)
        }
    }
}