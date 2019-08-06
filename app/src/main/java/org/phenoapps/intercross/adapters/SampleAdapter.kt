/*package org.phenoapps.intercross.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R

class SampleAdapter(val context: Context
) : ListAdapter<Sample, SampleAdapter.ViewHolder>(SampleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.list_item_sample, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).let { sample ->
            with(holder) {
                itemView.tag = sample
                bind(createOnClickListener(sample.name), sample)
            }
        }
    }

    private fun createOnClickListener(sampleName: String): View.OnClickListener {
        return View.OnClickListener {

        }
    }

    class ViewHolder(
            private val binding: org.phenoapps.intercross.databinding.ListItemSampleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, sample: Sample) {
            with(binding) {
                clickListener = listener
                viewModel = SampleViewModel(
                        sample
                )
                executePendingBindings()
            }
        }
    }
}

private class SampleDiffCallback : DiffUtil.ItemCallback<Sample>() {

    override fun areItemsTheSame(oldItem: Sample, newItem: Sample): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Sample, newItem: Sample): Boolean {
        return oldItem.name == newItem.name
    }
}*/