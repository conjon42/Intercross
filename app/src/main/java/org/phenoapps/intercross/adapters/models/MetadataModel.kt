package org.phenoapps.intercross.adapters.models

import androidx.recyclerview.widget.DiffUtil

data class MetadataModel(val property: String, val value: String) {
    companion object {

        class DiffCallback : DiffUtil.ItemCallback<MetadataModel>() {

            override fun areItemsTheSame(oldItem: MetadataModel, newItem: MetadataModel): Boolean {
                return oldItem.property== newItem.property
            }

            override fun areContentsTheSame(oldItem: MetadataModel, newItem: MetadataModel): Boolean {
                return oldItem.property == newItem.property && oldItem.value == newItem.value
            }
        }
    }
}