package org.phenoapps.intercross.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.ListItemWishlistRowBinding
import org.phenoapps.intercross.fragments.SummaryFragment
import org.phenoapps.intercross.fragments.SummaryFragmentDirections
import org.phenoapps.intercross.util.Dialogs


/**
 * This adapter class handles CrossData and WishlistFragment.ListEntry data
 * CrossData holds information on two previously crossed samples, and their crosses.
 * ListEntry holds wishlist specific data s.a wishMax, current.
 *
 * CrossData's are not rendered with checkboxes.
 *
 */
class SummaryAdapter(val context: Context) :
        ListAdapter<SummaryFragment.ListEntry, SummaryAdapter.ViewHolder>(SummaryDiffCallback()) {

    private class SummaryDiffCallback : DiffUtil.ItemCallback<SummaryFragment.ListEntry>() {

        override fun areItemsTheSame(oldItem: SummaryFragment.ListEntry,
                                     newItem: SummaryFragment.ListEntry): Boolean {

            return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
        }

        override fun areContentsTheSame(oldItem: SummaryFragment.ListEntry,
                                        newItem: SummaryFragment.ListEntry): Boolean {

            return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
        }
    }

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

                    Dialogs.list(AlertDialog.Builder(context),
                            context.getString(R.string.crosses),
                            binding.root,
                            data.events) { id ->

                        Navigation.findNavController(root)
                                .navigate(SummaryFragmentDirections.actionToEventDetail(id))
                    }

                }
            }
        }
    }
}