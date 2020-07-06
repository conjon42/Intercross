package org.phenoapps.intercross.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
typealias Empty = CrossBlockFragment.EmptyCell

class HeaderAdapter(val context: Context) : ListAdapter<Data, HeaderAdapter.ViewHolder>(HeaderDiffCallback()) {

    private class HeaderDiffCallback : DiffUtil.ItemCallback<Data>() {

        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {

            return oldItem.hashCode() == newItem.hashCode()
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

                val nameText = itemView.findViewById<TextView>(R.id.nameTextView)
                val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
                val emptyView = itemView.findViewById<View>(R.id.emptyView)

                when (data) {

                    is CrossBlockFragment.CellData -> {
                        progressBar.visibility = View.VISIBLE
                        nameText.visibility = View.GONE
                        emptyView.visibility = View.GONE
                    }

                    is CrossBlockFragment.HeaderData -> {
                        progressBar.visibility = View.GONE
                        emptyView.visibility = View.GONE
                        nameText.visibility = View.VISIBLE
                    }

                    is CrossBlockFragment.EmptyCell -> {
                        progressBar.visibility = View.GONE
                        nameText.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE

                    }
                }

                bind(data)
            }
        }
    }

    inner class ViewHolder(
            private val binding: ListItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Data) {

            with(binding) {

                when (data) {

                    is CrossBlockFragment.HeaderData -> {

                        name = data.name

                    }
                    is CrossBlockFragment.CellData -> {

                        progressBar.visibility = View.VISIBLE

                        nameTextView.visibility = View.GONE

                        emptyView.visibility = View.GONE

                        current = data.current

                        goal = data.max

//                        val pd = progressBar.progressDrawable.mutate()
//
//                        pd.setColorFilter(when {
//
//                            data.current >= data.max -> Color.RED
//
//                            data.current >= data.min -> Color.GREEN
//
//                            data.current < data.min && data.current > 0 -> Color.YELLOW
//
//                            else -> Color.GRAY
//
//                        }, PorterDuff.Mode.SRC_IN)
//
//                        progressBar.progressDrawable = pd
//
////                        progressBar.indeterminateTintList = ColorStateList.valueOf(
////
////                            when {
////
////                                data.current >= data.max -> Color.RED
////
////                                data.current >= data.min -> Color.GREEN
////
////                                else -> Color.YELLOW
////                            })

                        progressBar.progressDrawable.setColorFilter(when {

                            data.current >= data.max -> Color.RED

                            data.current >= data.min -> Color.GREEN

                            data.current < data.min && data.current > 0 -> Color.YELLOW

                            else -> Color.GRAY

                        }, PorterDuff.Mode.SRC_IN)


                        onClick = data.onClick

                    }
                }
            }
        }
    }
}

