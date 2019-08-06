package org.phenoapps.intercross.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.ListItemEventsBinding
import org.phenoapps.intercross.viewmodels.EventsViewModel

class EventsAdapter(
        val context: Context
) : ListAdapter<Events, EventsAdapter.ViewHolder>(EventsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_events, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { events ->
            with(holder) {
                itemView.tag = events
                bind(Navigation.createNavigateOnClickListener(
                      R.id.action_to_event,
                        Bundle().apply { putParcelable("events", events) }), events)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemEventsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, events: Events) {

            with(binding) {
                clickListener = listener
                viewModel = EventsViewModel(events)
                executePendingBindings()
            }
        }
    }
}

private class EventsDiffCallback : DiffUtil.ItemCallback<Events>() {

    override fun areItemsTheSame(oldItem: Events, newItem: Events): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Events, newItem: Events): Boolean {
        return oldItem.id == newItem.id
    }
}