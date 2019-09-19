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
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.ListItemEventsBinding
import org.phenoapps.intercross.viewmodels.EventsViewModel

class PollenAdapter(
        val context: Context
) : ListAdapter<Events, PollenAdapter.ViewHolder>(EventsDiffCallback()) {

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
                if (events.isSelected) itemView.setBackgroundColor(Color.parseColor("#FBE9E7"))
                else holder.itemView.setBackgroundColor(Color.WHITE)
                bind(View.OnClickListener {
                    events.isSelected = !events.isSelected
                    if (events.isSelected) itemView.setBackgroundColor(Color.parseColor("#FBE9E7"))
                    else itemView.setBackgroundColor(Color.WHITE)
                }, events)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemEventsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, e: Events) {

            with(binding) {
                clickListener = listener
                viewModel = EventsViewModel(e)
                executePendingBindings()
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
}
