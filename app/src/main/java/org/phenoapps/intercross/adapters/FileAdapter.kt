package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.dialogs.FileExploreDialogFragment.Item

class FileAdapter(private val onItemClick: (Item) -> Unit) :
    ListAdapter<Item, FileAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_dialog_item_select, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.textView.apply {
            text = item.file
            setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0)

            // add margin between image and text (support various screen
            // densities)
            val dp5 = (5 * resources.displayMetrics.density + 0.5f).toInt()
            compoundDrawablePadding = dp5
        }

        viewHolder.itemView.setOnClickListener { onItemClick(item) }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Item>() {

        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Item, newItem: Item) =
            oldItem.file == newItem.file &&
                    oldItem.isDir == newItem.isDir &&
                    oldItem.icon == newItem.icon
    }
}