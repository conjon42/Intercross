package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.ListItemSummaryBinding
import org.phenoapps.intercross.fragments.SummaryFragment

class SummaryAdapter:
        ListAdapter<SummaryFragment.ListEntry, SummaryAdapter.ViewHolder>(SummaryDiffCallback()) {

    private class SummaryDiffCallback : DiffUtil.ItemCallback<SummaryFragment.ListEntry>() {

        override fun areItemsTheSame(oldItem: SummaryFragment.ListEntry,
                                     newItem: SummaryFragment.ListEntry): Boolean {

            return (oldItem.label == newItem.label)
        }

        override fun areContentsTheSame(oldItem: SummaryFragment.ListEntry,
                                        newItem: SummaryFragment.ListEntry): Boolean {

            return (oldItem.label == newItem.label) && (oldItem.label == newItem.label)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_summary, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { item ->

            with(holder) {

                itemView.tag = item

                bind(item)
            }
        }
    }

    inner class ViewHolder(
            private val binding: ListItemSummaryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SummaryFragment.ListEntry) {

            with(binding) {

                labelText = data.label

                countValue = data.value.toInt()

            }
        }
    }
}