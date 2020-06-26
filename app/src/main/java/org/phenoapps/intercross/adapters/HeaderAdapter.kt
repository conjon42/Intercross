package org.phenoapps.intercross.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
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

class HeaderAdapter : ListAdapter<Data, HeaderAdapter.ViewHolder>(HeaderDiffCallback()) {

    private class HeaderDiffCallback : DiffUtil.ItemCallback<Data>() {

        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            TODO("Not yet implemented")
        }
    }

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

                bind(data)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Data) {

            with(binding) {

                when (data) {

                    is CrossBlockFragment.EmptyCell -> {

                        type = 0

                    }

                    is Cell -> {

                        type = 1

                        binding.progressBar.progress = data.current

                        progressBar.progressTintList = ColorStateList.valueOf(

                            when {

                                data.current >= data.max -> Color.RED

                                data.current >= data.min -> Color.GREEN

                                else -> Color.YELLOW
                            })

                        current = data.current

                        goal = data.max

                    }
                    else -> {

                        this.name = (data as Header).name

                        type = 2

                    }
                }

                executePendingBindings()
            }
        }
    }
}

