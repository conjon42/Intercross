package org.phenoapps.intercross.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.phenoapps.intercross.R
import org.phenoapps.intercross.fragments.CrossTrackerFragment
import org.phenoapps.intercross.util.DateUtil

class CrossTrackerAdapter(
    private val onCrossClicked: (male: String, female: String) -> Unit
) : ListAdapter<CrossTrackerFragment.CrossData, CrossTrackerAdapter.ViewHolder>(
        DiffCallback()
    ) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val crossCount: TextView = view.findViewById(R.id.cross_count)
        val femaleParent: TextView = view.findViewById(R.id.female_parent)
        val maleParent: TextView = view.findViewById(R.id.male_parent)
        val personChip: Chip = view.findViewById(R.id.person_chip)
        val dateChip: Chip = view.findViewById(R.id.date_chip)
        val wishlistProgressChip: Chip = view.findViewById(R.id.wish_progress_chip)

        val progressSection: LinearLayout = view.findViewById(R.id.wish_progress_section)
        val progressStatusIcon: ImageView = view.findViewById(R.id.wish_progress_status)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.wish_progress_bar)

        init {
            view.setOnClickListener {
                val position = layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = currentList[position]
                    onCrossClicked(item.m, item.f)
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_cross_tracker, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        with(currentList[position]) {
            viewHolder.crossCount.text = count
            viewHolder.femaleParent.text = f
            viewHolder.maleParent.text = m
            if (person.isEmpty()) {
                viewHolder.personChip.visibility = View.GONE
            } else {
                viewHolder.personChip.visibility = View.VISIBLE
                viewHolder.personChip.text = person
            }

            if (count != "0"){
                viewHolder.dateChip.visibility = View.VISIBLE
                viewHolder.dateChip.text = DateUtil().getFormattedDate(date)
            } else {
                viewHolder.dateChip.visibility = View.GONE
            }

            setProgress(viewHolder, this)
        }
    }

    private fun setProgress(viewHolder: ViewHolder, crossData: CrossTrackerFragment.CrossData) {
        val currentProgress = crossData.progress.toIntOrNull() ?: 0
        val targetProgress = crossData.wishMin.toIntOrNull() ?: 0

        if (targetProgress == 0) {
            viewHolder.wishlistProgressChip.visibility = View.GONE
            viewHolder.progressSection.visibility = View.GONE
        } else {
            viewHolder.wishlistProgressChip.visibility = View.VISIBLE
            viewHolder.progressSection.visibility = View.VISIBLE
            val percentage = (currentProgress.toFloat() / targetProgress.toFloat()) * 100

            val color = when {
                percentage > 100 -> Color.parseColor("#2E7D32")  // dark green
                percentage == 100f -> Color.parseColor("#8BC34A") // light green
                percentage >= 66 -> Color.parseColor("#FFEB3B")   // yellow
                percentage >= 33 -> Color.parseColor("#FF9800")   // orange
                else -> Color.parseColor("#F44336")              // red
            }

            viewHolder.progressStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    if (currentProgress >= targetProgress) R.drawable.ic_wishes_complete else R.drawable.ic_wishes_incomplete
                )
            )
            viewHolder.wishlistProgressChip.text = "${crossData.progress}/${crossData.wishMin}"
            viewHolder.progressBar.apply {
                max = targetProgress
                progress = currentProgress
                setIndicatorColor(color)
                visibility = View.VISIBLE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CrossTrackerFragment.CrossData>() {

        override fun areItemsTheSame(oldItem: CrossTrackerFragment.CrossData, newItem: CrossTrackerFragment.CrossData) = oldItem == newItem

        override fun areContentsTheSame(oldItem: CrossTrackerFragment.CrossData, newItem: CrossTrackerFragment.CrossData) = oldItem == newItem
    }
}