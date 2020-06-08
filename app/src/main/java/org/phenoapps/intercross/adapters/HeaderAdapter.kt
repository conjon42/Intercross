package org.phenoapps.intercross.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Parents
import org.phenoapps.intercross.databinding.ListItemHeaderBinding
import org.phenoapps.intercross.databinding.ListItemParentsBinding

class HeaderAdapter(ctx: Context)
    : ListAdapter<String, HeaderAdapter.ViewHolder>(HeaderDiffCallback()) {

    override fun onCreateViewHolder(vg: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(vg.context),
                        R.layout.list_item_header, vg, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { name ->
            with(holder) {
                bind(name)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {

            with(binding) {
                this.name = name
                executePendingBindings()
            }
        }
    }
}

private class HeaderDiffCallback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}