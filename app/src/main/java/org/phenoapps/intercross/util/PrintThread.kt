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
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup


class PrintThread(private val ctx: Context, private val template: String,
                  private val btName: String) : Thread() {

    private var mMode = 0
    private lateinit var mEvents: Array<Event>
    private lateinit var mParents: Array<Parent>

    fun printEvents(events: Array<Event>) {
        mEvents = events
        mMode = 0
        start()
    }

    fun printParents(parents: Array<Parent>) {
        mParents = parents
        mMode = 1
        start()
    }

    fun printGroup(groups: Array<PollenGroup>) {
        mParents = groups.map { g -> Parent(g.codeId, 1, g.name) }.toTypedArray()
        mMode = 1
        start()
    }

    override fun run() {

        Looper.prepare()

        if (btName.isBlank()) {

            val notPaired = ctx.getString(R.string.no_device_paired)

            Toast.makeText(ctx, notPaired, Toast.LENGTH_SHORT).show()

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

                    linkOsPrinter?.let { it ->

                        val printerStatus = it.currentStatus

                        getPrinterStatus(bc)

                        val printerOpen = ctx.getString(R.string.printer_open)
                        val printerPaused = ctx.getString(R.string.printer_paused)
                        val noPaper = ctx.getString(R.string.printer_empty)
                        val notConnected = ctx.getString(R.string.printner_not_connected)

                        if (printerStatus.isReadyToPrint) {

                            if (template.isNotBlank()) {

                                printer.sendCommand(template)

                            }

                            when (mMode) {
                                0 -> {
                                    mEvents.forEach {

                                        printer.sendCommand("^XA^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF" +
                                                "^FN1^FD${it.eventDbId}^FS" +
                                                "^FN2^FDQA,${it.eventDbId}^FS" +
                                                "^FN3^FD${it.timestamp}^FS^XZ")
                                    }
                                }
                                1 -> {
                                    mParents.forEach {

                                        val unknown = ctx.getString(R.string.unknown)
                                        printer.sendCommand("^XA^XFR:DEFAULT_INTERCROSS_SAMPLE.GRF" +
                                                "^FN1^FD${it.codeId}^FS" +
                                                "^FN2^FDQA,${it.codeId}^FS" +
                                                "^FN3^FD${unknown}^FS^XZ")
                                    }
                                }
                            }

                            /*printer.printImage(new ZebraImageAndroid(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.intercross_small)), 75,500,-1,-1,false);*/

                        } else if (printerStatus.isHeadOpen) {
                             Toast.makeText(ctx, printerOpen, Toast.LENGTH_LONG).show()
                        } else if (printerStatus.isPaused) {
                             Toast.makeText(ctx, printerPaused, Toast.LENGTH_LONG).show()
                        } else if (printerStatus.isPaperOut) {
                             Toast.makeText(ctx, noPaper, Toast.LENGTH_LONG).show()
                        } else {
                             Toast.makeText(ctx, notConnected, Toast.LENGTH_LONG).show()
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