package org.phenoapps.intercross.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Parents
import org.phenoapps.intercross.databinding.ListItemParentsBinding

class ParentsAdapter
    : ListAdapter<Parents, ParentsAdapter.ViewHolder>(ParentsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_parents, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { p ->
            with(holder) {
                itemView.tag = p
                if (p.isSelected) itemView.setBackgroundColor(Color.parseColor("#FBE9E7"))
                else holder.itemView.setBackgroundColor(Color.WHITE)
                bind(View.OnClickListener {
                    p.isSelected = !p.isSelected
                    if (p.isSelected) itemView.setBackgroundColor(Color.parseColor("#FBE9E7"))
                    else holder.itemView.setBackgroundColor(Color.WHITE)
                }, p)
            }
        }
    }

    class ViewHolder(
            private val binding: ListItemParentsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(click: View.OnClickListener, p: Parents) {

            with(binding) {
                model = p
                onClick = click
                executePendingBindings()
            }
        }
    }
}

private class ParentsDiffCallback : DiffUtil.ItemCallback<Parents>() {

    override fun areItemsTheSame(oldItem: Parents, newItem: Parents): Boolean {
        return oldItem.parentDbId == newItem.parentDbId
    }

    override fun areContentsTheSame(oldItem: Parents, newItem: Parents): Boolean {
        return oldItem.parentDbId == newItem.parentDbId
    }
}