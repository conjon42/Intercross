package org.phenoapps.intercross.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Pollen
import org.phenoapps.intercross.databinding.ListItemSimpleRowBinding

class PollenAdapter(
        val context: Context
) : ListAdapter<Pollen, PollenAdapter.ViewHolder>(PollenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_simple_row, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { name ->

            with(holder) {
                itemView.tag = name
                bind(name)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemSimpleRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pollen: Pollen) {

            with(binding) {
                name.text = pollen.pollenId
                executePendingBindings()
            }
        }
    }

    private class PollenDiffCallback : DiffUtil.ItemCallback<Pollen>() {

        override fun areItemsTheSame(oldItem: Pollen, newItem: Pollen): Boolean {
            return oldItem.pollenId == newItem.pollenId
        }

        override fun areContentsTheSame(oldItem: Pollen, newItem: Pollen): Boolean {
            return oldItem.pollenId == newItem.pollenId
        }
    }
}
