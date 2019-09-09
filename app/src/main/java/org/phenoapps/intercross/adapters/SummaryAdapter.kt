package org.phenoapps.intercross.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.ListItemSummaryBinding
import org.phenoapps.intercross.fragments.SummaryFragment
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.SummaryViewModel

class SummaryAdapter(
        val context: Context
) : ListAdapter<SummaryFragment.SummaryData, SummaryAdapter.ViewHolder>(SummaryDiffCallback()) {

    private var mSnackbar: SnackbarQueue = SnackbarQueue()

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

    inner class ViewHolder(
            private val binding: ListItemSummaryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SummaryFragment.SummaryData) {

            with(binding) {
                viewModel = SummaryViewModel(data)

                femaleClick = View.OnClickListener {
                    if (data.f == null)  mSnackbar.push(SnackbarQueue.SnackJob(binding.root, "Entry does not exist."))
                    else {
                        Navigation.findNavController(binding.root).navigate(R.id.global_action_to_event_fragment, Bundle().apply {
                            putParcelable("events", data.f)
                        })
                    }
                }

                maleClick = View.OnClickListener {
                    if (data.m == null)  mSnackbar.push(SnackbarQueue.SnackJob(binding.root, "Entry does not exist."))
                    else {
                        Navigation.findNavController(binding.root).navigate(R.id.global_action_to_event_fragment, Bundle().apply {
                            putParcelable("events", data.m)
                        })
                    }
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