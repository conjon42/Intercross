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
) : ListAdapter<CrossTrackerFragment.ListEntry, CrossTrackerAdapter.ViewHolder>(
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
                    when (item) {
                        is CrossTrackerFragment.PlannedCrossData -> {
                            onCrossClicked(item.maleId, item.femaleId)
                        }
                        else -> {
                            onCrossClicked(item.male, item.female)
                        }
                    }
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
            viewHolder.apply {
                crossCount.text = count
                femaleParent.text = female
                maleParent.text = male

                personChip.text = person
                personChip.visibility = if (person.isNotEmpty()) View.VISIBLE else View.GONE

                dateChip.text = if (count != "0") DateUtil().getFormattedDate(date) else ""
                dateChip.visibility = if (count != "0") View.VISIBLE else View.GONE
            }

            when (this) {
                is CrossTrackerFragment.UnplannedCrossData -> hideProgressBar(viewHolder)
                is CrossTrackerFragment.PlannedCrossData -> setProgressBar(viewHolder, this)
            }
        }
    }

    private fun hideProgressBar(viewHolder: ViewHolder) {
        viewHolder.apply {
            wishlistProgressChip.visibility = View.GONE
            progressSection.visibility = View.GONE
        }
    }

    private fun setProgressBar(viewHolder: ViewHolder, plannedCrossData: CrossTrackerFragment.PlannedCrossData) {
        val currentProgress = plannedCrossData.progress.toIntOrNull() ?: 0
        val targetProgress = plannedCrossData.wishMin.toIntOrNull() ?: 0

        val percentage = (currentProgress.toFloat() / targetProgress.toFloat()) * 100

        val color = when {
            percentage > 100 -> Color.parseColor("#2E7D32")  // dark green
            percentage == 100f -> Color.parseColor("#8BC34A") // light green
            percentage >= 66 -> Color.parseColor("#FFEB3B")   // yellow
            percentage >= 33 -> Color.parseColor("#FF9800")   // orange
            else -> Color.parseColor("#F44336")              // red
        }

        viewHolder.apply {
            wishlistProgressChip.visibility = View.VISIBLE
            progressSection.visibility = View.VISIBLE
            progressStatusIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    viewHolder.itemView.context,
                    if (currentProgress >= targetProgress) R.drawable.ic_wishes_complete else R.drawable.ic_wishes_incomplete
                )
            )
            wishlistProgressChip.text = "${plannedCrossData.progress}/${plannedCrossData.wishMin}"
            progressBar.apply {
                max = targetProgress
                progress = currentProgress
                setIndicatorColor(color)
                visibility = View.VISIBLE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CrossTrackerFragment.ListEntry>() {

        override fun areItemsTheSame(oldItem: CrossTrackerFragment.ListEntry, newItem: CrossTrackerFragment.ListEntry) = oldItem == newItem

        override fun areContentsTheSame(oldItem: CrossTrackerFragment.ListEntry, newItem: CrossTrackerFragment.ListEntry) = oldItem == newItem
    }
}