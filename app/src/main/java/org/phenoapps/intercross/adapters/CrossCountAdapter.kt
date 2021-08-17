package org.phenoapps.intercross.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.databinding.ListItemCrossCountRowBinding
import org.phenoapps.intercross.fragments.CrossCountFragment
import org.phenoapps.intercross.fragments.CrossCountFragmentDirections
import org.phenoapps.intercross.util.Dialogs


/**
 * This adapter class handles CrossData and WishlistFragment.ListEntry data
 * CrossData holds information on two previously crossed samples, and their crosses.
 * ListEntry holds wishlist specific data s.a wishMax, current.
 *
 * CrossData's are not rendered with checkboxes.
 *
 */
class CrossCountAdapter(private val owner: LifecycleOwner, private val viewModel: EventListViewModel, val context: Context) :
        ListAdapter<CrossCountFragment.ListEntry, CrossCountAdapter.ViewHolder>(SummaryDiffCallback()) {

    private class SummaryDiffCallback : DiffUtil.ItemCallback<CrossCountFragment.ListEntry>() {

        override fun areItemsTheSame(oldItem: CrossCountFragment.ListEntry,
                                     newItem: CrossCountFragment.ListEntry): Boolean {

            return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
        }

        override fun areContentsTheSame(oldItem: CrossCountFragment.ListEntry,
                                        newItem: CrossCountFragment.ListEntry): Boolean {

            return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_cross_count_row, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { item ->

            with(holder) {

                itemView.tag = item

                bind(item)
            }
        }
    }

    inner class ViewHolder(
            private val binding: ListItemCrossCountRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CrossCountFragment.ListEntry) {

            with(binding) {

                maleParent = data.m

                femaleParent = data.f

                count = data.count

                //uses inner view holder to create list of crosses used for that wish item
                onClick = View.OnClickListener {

                    if (data.count == "1") {

                        data.events.first().let { event ->
                            Navigation.findNavController(root)
                                .navigate(CrossCountFragmentDirections
                                    .globalActionToEventDetail(event.id ?: -1))

                        }

                    } else {

                        val adapter = EventsAdapter(owner, viewModel)
                        adapter.submitList(data.events)

                        Dialogs.list(
                            AlertDialog.Builder(context),
                            context.getString(R.string.click_item_for_child_details),
                            context.getString(R.string.no_child_exists),
                            data.events) { id ->

                            Navigation.findNavController(root)
                                .navigate(CrossCountFragmentDirections
                                    .globalActionToEventDetail(id))
                        }
                    }
                }
            }
        }
    }
}