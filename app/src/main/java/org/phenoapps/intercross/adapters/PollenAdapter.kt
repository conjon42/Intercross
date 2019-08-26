package org.phenoapps.intercross.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.zebra.sdk.graphics.ZebraImageFactory
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.Pollen
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.databinding.ListItemEventsBinding
import org.phenoapps.intercross.databinding.ListItemPollenBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.viewmodels.EventsViewModel

class PollenAdapter(val ctx: Context)
    : ListAdapter<PollenGroup, PollenAdapter.ViewHolder>(PollenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_pollen, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { pollen ->
            with(holder) {
                itemView.tag = pollen
                bind(Navigation.createNavigateOnClickListener(
                      R.id.action_to_pollen_fragment,
                        Bundle().apply { putParcelable("pollen", pollen) }), pollen)
            }
        }
    }

    inner class ViewHolder(
            private val binding: ListItemPollenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, p: PollenGroup) {

            with(binding) {
                barcodeImage.setImageBitmap(BarcodeEncoder().createBitmap(
                        MultiFormatWriter().encode(p.name,
                            BarcodeFormat.QR_CODE, barcodeImage.width, barcodeImage.height)))
                pollenClick = listener
                barcodeClick = View.OnClickListener {
                    //BluetoothUtil().templatePrint(ctx, p)
                }
                model = p
                executePendingBindings()
            }
        }
    }
}

private class PollenDiffCallback : DiffUtil.ItemCallback<PollenGroup>() {

    override fun areItemsTheSame(oldItem: PollenGroup, newItem: PollenGroup): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PollenGroup, newItem: PollenGroup): Boolean {
        return oldItem.id == newItem.id
    }
}