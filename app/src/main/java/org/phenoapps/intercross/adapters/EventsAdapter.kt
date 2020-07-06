package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.coroutineScope
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.databinding.ListItemEventsBinding
import org.phenoapps.intercross.fragments.EventsFragmentDirections


class EventsAdapter : ListAdapter<Event, RecyclerView.ViewHolder>(Event.Companion.DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return EventViewHolder(DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_events, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        getItem(position).let { event ->

            with(holder as EventViewHolder) {

                 bind(event)
            }
        }
    }

    class EventViewHolder(private val binding: ListItemEventsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {

            with(binding) {

                event.id?.let { key ->

                    this.event = event

                    clickListener = View.OnClickListener {

                        it?.let {
                            Navigation.findNavController(binding.root)
                                    .navigate(EventsFragmentDirections
                                            .actionToEventFragment(key))
                        }
                    }
                }
            }
        }
    }
}