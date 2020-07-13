package org.phenoapps.intercross.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color

import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.HeaderAdapter
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.fragments.CrossBlockFragment
import org.phenoapps.intercross.fragments.CrossBlockFragmentDirections


//TODO: chaneylc rendered barcodes are not whats printed

class AsyncLoadCrossblock(val context: Context,
                          val root: View,
                          val block: List<WishlistView>,
                          val events: List<Event>,
                          val table: RecyclerView,
                          val rows: RecyclerView,
                          val cols: RecyclerView) : AsyncTask<Void?, List<CrossBlockFragment.BlockData>?, CrossblockData?>() {

    override fun doInBackground(vararg params: Void?): CrossblockData {

        val maleHeaders = block.map { CrossBlockFragment.HeaderData(it.dadName, it.dadId) }.distinctBy { it.code }

        val femaleHeaders = block.map { CrossBlockFragment.HeaderData(it.momName, it.momId) }.distinctBy { it.code }

        val data = ArrayList<CrossBlockFragment.BlockData>()

        for (f in femaleHeaders) {

            val possibleMales = block.filter { f.code == it.momId }

            for (m in maleHeaders) {

                val res = possibleMales.find { m.code == it.dadId }

                if (res != null) {

                    val stateColor =

                        when {

                            res.wishProgress >= res.wishMax -> ContextCompat.getColor(context, R.color.progressEnd)

                            res.wishProgress >= res.wishMin -> ContextCompat.getColor(context, R.color.progressMid)

                            res.wishProgress > 0 && res.wishProgress < res.wishMin -> ContextCompat.getColor(context, R.color.progressStart)

                            else -> ContextCompat.getColor(context, R.color.progressBlank)
                        }

                    data.add(CrossBlockFragment.CellData(res.wishProgress, res.wishMin, res.wishMax, View.OnClickListener {

                        val children = events.filter { event ->
                            event.femaleObsUnitDbId == res.momId && event.maleObsUnitDbId == res.dadId
                        }

                        Dialogs.list(AlertDialog.Builder(context),
                                context.getString(R.string.click_item_for_child_details),
                                context.getString(R.string.no_child_exists),
                                children) { id ->

                            Navigation.findNavController(root)
                                    .navigate(CrossBlockFragmentDirections.actionToEventDetail(id))
                        }

                    }, stateColor))

                } else data.add(CrossBlockFragment.EmptyCell())
            }
        }

        return CrossblockData(maleHeaders, femaleHeaders, data)
    }

    override fun onPostExecute(data: CrossblockData?) {

        val columns = data?.males?.size

        table.layoutManager = GridLayoutManager(context, columns ?: 0,
                GridLayoutManager.HORIZONTAL, false)

        (rows.adapter as HeaderAdapter).submitList(data?.males)

        (cols.adapter as HeaderAdapter).submitList(data?.females)

        (table.adapter as HeaderAdapter).submitList(data?.data)

    }

}

data class CrossblockData(val males: List<CrossBlockFragment.HeaderData>, val females: List<CrossBlockFragment.HeaderData>, val data: List<CrossBlockFragment.BlockData>)