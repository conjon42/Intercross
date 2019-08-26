package org.phenoapps.intercross.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.ListItemSummaryBinding
import org.phenoapps.intercross.fragments.SummaryFragment
import org.phenoapps.intercross.viewmodels.SummaryViewModel

class SummaryAdapter(
        val context: Context
) : ListAdapter<SummaryFragment.SummaryData, SummaryAdapter.ViewHolder>(SummaryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_summary, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { s ->
            with(holder) {
                itemView.tag = s
                bind(s)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemSummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SummaryFragment.SummaryData) {

            with(binding) {
                viewModel = SummaryViewModel(data)
                data.f?.let {
                    femaleClick = Navigation.createNavigateOnClickListener(R.id.global_action_to_event_fragment, Bundle().apply {
                        putParcelable("events", it)
                    })
                }
                data.m?.let {
                    maleClick = Navigation.createNavigateOnClickListener(R.id.global_action_to_event_fragment, Bundle().apply {
                        putParcelable("events", it)
                    })
                }

                executePendingBindings()
            }
        }
    }
}

private class SummaryDiffCallback : DiffUtil.ItemCallback<SummaryFragment.SummaryData>() {

    override fun areItemsTheSame(oldItem: SummaryFragment.SummaryData, newItem: SummaryFragment.SummaryData): Boolean {
        return oldItem.event.id == newItem.event.id
    }

    override fun areContentsTheSame(oldItem: SummaryFragment.SummaryData, newItem: SummaryFragment.SummaryData): Boolean {
        return  oldItem.event.id == newItem.event.id
    }
}