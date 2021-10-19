package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.models.MetadataModel
import org.phenoapps.intercross.databinding.ListItemMetadataBinding
import org.phenoapps.intercross.interfaces.MetadataManager

class MetadataAdapter(val listener: MetadataManager) :
    ListAdapter<MetadataModel, RecyclerView.ViewHolder>(MetadataModel.Companion.DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MetadataViewHolder(DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_metadata, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position).let { meta ->

            with(holder as MetadataViewHolder) {

                bind(meta)

            }
        }
    }

    inner class MetadataViewHolder(private val binding: ListItemMetadataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(meta: MetadataModel) {

            with(binding) {

                listItemMetadataEditText.setText(meta.value)

                listItemMetadataEditText.hint = meta.property

                listItemMetadataTextLayout.hint = meta.property

                listItemMetadataEditText.addTextChangedListener {

                    val intValue = listItemMetadataEditText.text.toString().toIntOrNull() ?: 0

                    listener.onMetadataUpdated(meta.property, intValue)

                }

                value = meta.value

                property = meta.property

                hint = meta.property
            }
        }
    }
}