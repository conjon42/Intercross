package org.phenoapps.intercross.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.preference.PreferenceManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent


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

                pairedDevices.forEachIndexed { _, t ->
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

    private var template = "^XA^MNA^MMT,N" +
            "^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS" +
            "^FWR" +
            "^FO50,25^A0,20,20^FB200,4,,c,^FN1^FS" +
            "^FO150,30^BQ,,5,H^FN2^FS" +
            "^FO325,15^A0,20,20^FB200,4,,c,^FN3^FS^XZ"

    /*var template = """
        ^XA
        ^MNA
        ^MMT,N
        ^DFR:DEFAULT_INTERCROSS_SAMPLE.GRF^FS
        ^FWR
        ^FO50,25
        ^A0,20,20
        ^FN1^FS
        ^FO150,30
        ^BQ,,5,H
        ^FN2^FS
        ^FO400,25
        ^A0,25,20
        ^FN3^FS
        ^XZ"
    """*/

    fun print(ctx: Context, events: Array<Event>) {
        choose(ctx) {

            val importedZpl = PreferenceManager.getDefaultSharedPreferences(ctx).getString("ZPL_CODE", "") ?: ""

            if (importedZpl.isNotBlank()) {

                PrintThread(ctx, importedZpl, mBtName).printEvents(events)


            } else {

                PrintThread(ctx, template, mBtName).printEvents(events)

            }
        }
    }

    fun print(ctx: Context, parents: Array<Parent>) {

        val importedZpl = PreferenceManager.getDefaultSharedPreferences(ctx).getString("ZPL_CODE", "") ?: ""

        if (importedZpl.isNotBlank()) {

            PrintThread(ctx, importedZpl, mBtName).printParents(parents)


        } else {

            PrintThread(ctx, template, mBtName).printParents(parents)

        }
    }
}