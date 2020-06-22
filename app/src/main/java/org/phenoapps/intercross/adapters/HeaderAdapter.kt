package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.ListItemHeaderBinding
import org.phenoapps.intercross.fragments.CrossBlockFragment

typealias Data = CrossBlockFragment.BlockData
typealias Header = CrossBlockFragment.HeaderData
typealias Cell = CrossBlockFragment.CellData

class HeaderAdapter()
    : ListAdapter<Data, HeaderAdapter.ViewHolder>(HeaderDiffCallback()) {

    override fun onCreateViewHolder(vg: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(vg.context),
                        R.layout.list_item_header, vg, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { data ->

            with(holder) {

                if (data is Cell) {

                    itemView.findViewById<ProgressBar>(R.id.progressBar).apply {

//                        progressDrawable.setColorFilter(when {
//
//                            data.current >= data.min -> Color.YELLOW
//
//                            data.current >= data.max -> Color.GREEN
//
//                            else -> Color.RED
//
//                        }, PorterDuff.Mode.DST_OVER)

                        isIndeterminate=false

                        progress = data.current

                        max = data.max
                    }
                }
                bind(data)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Data) {

            with(binding) {

                if (data is CrossBlockFragment.EmptyCell) {

                    type = 0

                } else if (data is Cell) {

                    type = 1

                    binding.progressBar.progress = data.current

//                    progressBar.progressDrawable.setColorFilter(when {
//
//                        data.current >= data.max -> Color.GREEN
//
//                        data.current >= data.min -> Color.YELLOW
//
//                        else -> Color.RED
//
//                    }, PorterDuff.Mode.DST_OVER)

                    current = data.current

                    goal = data.max

                } else {

                    this.name = (data as Header).name

                    type = 2

                }

                executePendingBindings()
            }
        }
    }
}

private class HeaderDiffCallback : DiffUtil.ItemCallback<Data>() {

    override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
        TODO("Not yet implemented")
    }
}