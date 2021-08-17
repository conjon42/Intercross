package org.phenoapps.intercross.adapters.viewholders

import androidx.recyclerview.widget.DiffUtil

class MetadataViewHolder(val property: String, val value: Int) {
    companion object {

        class DiffCallback : DiffUtil.ItemCallback<MetadataViewHolder>() {

            override fun areItemsTheSame(oldItem: MetadataViewHolder, newItem: MetadataViewHolder): Boolean {
                return oldItem.property == newItem.property && oldItem.value == newItem.value
            }

            override fun areContentsTheSame(oldItem: MetadataViewHolder, newItem: MetadataViewHolder): Boolean {
                return oldItem.property == newItem.property && oldItem.value == newItem.value
            }
        }
    }
}