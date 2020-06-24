package org.phenoapps.intercross.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.databinding.ListItemSimpleRowBinding
import org.phenoapps.intercross.databinding.ListItemWishlistRowBinding
import org.phenoapps.intercross.fragments.SummaryFragment


/**
 * This adapter class handles CrossData and WishlistFragment.ListEntry data
 * CrossData holds information on two previously crossed samples, and their crosses.
 * ListEntry holds wishlist specific data s.a wishMax, current.
 *
 * CrossData's are not rendered with checkboxes.
 *
 * TODO implement child list pop-up, maybe use AlertDialog SingleChoice item list
 */
class SummaryAdapter(
        val context: Context
) : ListAdapter<SummaryFragment.ListEntry, SummaryAdapter.ViewHolder>(WishlistDiffCallback()) {

    private lateinit var mParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        mParent = parent

        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_wishlist_row, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { wishitem ->

            with(holder) {

                var itemType = 0

                itemView.tag = wishitem

                if (wishitem is SummaryFragment.CrossData) {

                    itemType = 1
                }

                bind(wishitem, itemType)
            }
        }
    }

    inner class InnerViewHolder internal constructor(private val binding: ListItemSimpleRowBinding,
                                                     private val parentRoot: View,
                                                     private val dialog: AlertDialog)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Event) {

            with (binding) {

                name.text = data.readableName

                onClick = View.OnClickListener {

//                    Navigation.findNavController(parentRoot).navigate(R.id.global_action_to_event_fragment, Bundle().apply {
//
//                        putParcelable("events", data)
//                    })

                    dialog.cancel()
                }

                executePendingBindings()
            }
        }
    }


    inner class ViewHolder(
            private val binding: ListItemWishlistRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SummaryFragment.ListEntry, type: Int) {

            with(binding) {

                maleParent = data.m

                femaleParent = data.f

                count = data.count

                checkboxType = type

                if (data.count.contains("/")) {

                    val tokens = data.count.split("/")

                    if (tokens[0].toInt() >= tokens[1].toInt()) {

                        completed = true
                    }
                }

                //uses inner view holder to create list of crosses used for that wish item
                onClick = View.OnClickListener {

                    val builder = AlertDialog.Builder(context)

                    builder.setTitle("Crosses")

                    val layout = RecyclerView(context)

                    builder.setView(layout)

                    val dialog = builder.create()

                    val adapter = object : ListAdapter<Event, InnerViewHolder>(Event.Companion.DiffCallback()) {

                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {

                            return InnerViewHolder(

                                    DataBindingUtil.inflate(

                                            LayoutInflater.from(parent.context),

                                            R.layout.list_item_simple_row, parent, false

                                    ), mParent, dialog
                            )
                        }

                        override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {

                            with(holder) {

                                itemView.tag = getItem(position)

                                bind(getItem(position))
                            }
                        }

                    }

                    adapter.submitList(data.events)

                    layout.adapter = adapter

                    layout.layoutManager = LinearLayoutManager(context)

                    dialog.show()
                }

                executePendingBindings()
            }
        }
    }
}

//TODO move this to diff callback file
private class WishlistDiffCallback : DiffUtil.ItemCallback<SummaryFragment.ListEntry>() {

    override fun areItemsTheSame(oldItem: SummaryFragment.ListEntry,
                                 newItem: SummaryFragment.ListEntry): Boolean {

        return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
    }

    override fun areContentsTheSame(oldItem: SummaryFragment.ListEntry,
                                    newItem: SummaryFragment.ListEntry): Boolean {

        return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
    }
}