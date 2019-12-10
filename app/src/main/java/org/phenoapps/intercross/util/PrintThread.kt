package org.phenoapps.intercross.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.printer.SGD
import com.zebra.sdk.printer.ZebraPrinterFactory
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.PollenGroup


class PrintThread(private val ctx: Context, private val template: String,
                  private val btName: String) : Thread() {

    var mMode = 0
    lateinit var mEvents: Array<Events>
    lateinit var mGroup: PollenGroup

    fun printEvents(events: Array<Events>) {
        mEvents = events
        mMode = 0
        start()
    }

    fun printGroup(group: PollenGroup) {
        mGroup = group
        mMode = 1
        start()
    }

    override fun run() {

        Looper.prepare()

        if (btName.isBlank()) {
            Toast.makeText(ctx, "No bluetooth device paired.", Toast.LENGTH_SHORT).show()
        } else {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            val pairedDevices = mBluetoothAdapter.bondedDevices.filter {
                it.name == btName
            }

            if (pairedDevices.isNotEmpty()) {
                val bc = BluetoothConnection(pairedDevices[0].address)

                try {
                    bc.open()
                    val printer = ZebraPrinterFactory.getInstance(bc)
                    val linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer)
                    linkOsPrinter?.let {
                        val printerStatus = it.currentStatus
                        getPrinterStatus(bc)
                        //println((it.allSettings["head.resolution.in_dpi"] as Setting).value)

                        if (printerStatus.isReadyToPrint) {

                            when (mMode) {
                                0 -> {
                                    mEvents.forEach {
                                        if (template.isNotBlank()) {
                                            printer.sendCommand(template)
                                        }
                                        printer.sendCommand("^XA^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF" +
                                                "^FN1^FD${it.eventDbId}^FS" +
                                                "^FN2^FDQA,${it.eventDbId}^FS" +
                                                "^FN3^FD${it.timestamp}^FS^XZ")
                                    }
                                }
                                1 -> {
                                    if (template.isNotBlank()) {
                                        printer.sendCommand(template)
                                    }
                                    printer.sendCommand("^XA^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF" +
                                            "^FN1^FD${mGroup.name}^FS" +
                                            "^FN2^FDQA,${mGroup.uuid}^FS" +
                                            "^FN3^FD${mGroup.uuid}^FS^XZ")
                                }
                            }


                            /*printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.intercross_small)), 75,500,-1,-1,false);*/

                        } else if (printerStatus.isHeadOpen) {
                             Toast.makeText(ctx, "Printer is open.", Toast.LENGTH_LONG).show()
                        } else if (printerStatus.isPaused) {
                             Toast.makeText(ctx, "Printer is paused.", Toast.LENGTH_LONG).show()
                        } else if (printerStatus.isPaperOut) {
                             Toast.makeText(ctx, "No paper.", Toast.LENGTH_LONG).show()
                        } else {
                             Toast.makeText(ctx, "Please check the printer's connection.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: ConnectionException) {
                    e.printStackTrace()
                } catch (e: ZebraPrinterLanguageUnknownException) {
                    e.printStackTrace()
                } finally {
                    bc.close()
                }
            }
        }
    }

    @Throws(ConnectionException::class)
    private fun getPrinterStatus(connection: BluetoothConnection) {

        val printerLanguage = SGD.GET("device.languages", connection)

        val displayPrinterLanguage = "Printer Language is $printerLanguage"

        SGD.SET("device.languages", "zpl", connection)

        Toast.makeText(ctx,
                "$displayPrinterLanguage\nLanguage set to ZPL", Toast.LENGTH_LONG).show()

    }
}