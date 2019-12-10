package org.phenoapps.intercross.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.databinding.ListItemSimpleRowBinding
import org.phenoapps.intercross.databinding.ListItemWishlistBinding
import org.phenoapps.intercross.fragments.WishlistFragment
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.WishlistSummaryViewModel

class WishlistAdapter(
        val context: Context
) : ListAdapter<WishlistFragment.WishlistData, WishlistAdapter.ViewHolder>(WishlistDiffCallback()) {

    private lateinit var mParent: ViewGroup
    private var mSnackbar: SnackbarQueue = SnackbarQueue()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mParent = parent
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_wishlist, parent, false
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

    inner class InnerViewHolder internal constructor(private val binding: ListItemSimpleRowBinding,
                                                     private val parentRoot: View,
                                                     private val dialog: AlertDialog)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Events) {
            with (binding) {
                name.text = data.eventDbId
                onClick = View.OnClickListener {
                    Navigation.findNavController(parentRoot).navigate(R.id.global_action_to_event_fragment, Bundle().apply {
                        putParcelable("events", data)
                    })
                    dialog.cancel()
                }
                executePendingBindings()
            }
        }
    }

    inner class ViewHolder(
            private val binding: ListItemWishlistBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: WishlistFragment.WishlistData) {

            with(binding) {
                viewModel = WishlistSummaryViewModel(data)
                click = View.OnClickListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Crosses")
                    val layout = RecyclerView(context)
                    builder.setView(layout)

                    val dialog = builder.create()
                    val adapter = object : ListAdapter<Events, InnerViewHolder>(EventsDiffCallback()) {

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
                    adapter.submitList(data.event)
                    layout.adapter = adapter
                    layout.layoutManager = LinearLayoutManager(context)

                    dialog.show()
                }

                executePendingBindings()
            }
        }
    }
}

private class WishlistDiffCallback : DiffUtil.ItemCallback<WishlistFragment.WishlistData>() {

    override fun areItemsTheSame(oldItem: WishlistFragment.WishlistData, newItem: WishlistFragment.WishlistData): Boolean {
        return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
    }

    override fun areContentsTheSame(oldItem: WishlistFragment.WishlistData, newItem: WishlistFragment.WishlistData): Boolean {
        return (oldItem.f == newItem.f) && (oldItem.m == newItem.m)
    }
}