package org.phenoapps.intercross.data.models

import androidx.recyclerview.widget.DiffUtil

class Metadata(val property: String, val value: Int) {
    companion object {

        class DiffCallback : DiffUtil.ItemCallback<Metadata>() {

            override fun areItemsTheSame(oldItem: Metadata, newItem: Metadata): Boolean {
                return oldItem.property == newItem.property && oldItem.value == newItem.value
            }

            override fun areContentsTheSame(oldItem: Metadata, newItem: Metadata): Boolean {
                return oldItem.property == newItem.property && oldItem.value == newItem.value
            }
        }
    }
}