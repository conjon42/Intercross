package org.phenoapps.intercross.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.*
import org.brapi.v2.model.germ.BrAPIPlannedCross
import org.phenoapps.intercross.R

class WishlistImportDialog(private val activity: FragmentActivity,
                           private val projectName: String,
                           private val plannedCrosses: List<BrAPIPlannedCross>,
                           private val onClick: () -> Unit) : Dialog(activity), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_wishlist_loading)

        setCancelable(true)

        //for some reason this is required for the views to match the layout file
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        setupSizeGroup()

    }

    //the initial step that asks the user for row/column size of their field
    //row*column plots will be generated based on a chosen pattern
    private fun setupSizeGroup() {

        val sizeGroup = findViewById<Group>(R.id.dialog_planned_crosses)

        val summary = findViewById<TextView>(R.id.dialog_planned_crosses_summary_text)

        summary.text = activity.getString(R.string.dialog_planned_crosses_summary, projectName, plannedCrosses.size.toString())

        val submitButton = findViewById<Button>(R.id.dialog_planned_crosses_submit_button)

        //when the OK button is pressed...
        submitButton.setOnClickListener {

            this.enableProgress()

            onClick()

            this.disableProgress()

            dismiss()

        }
    }

    fun disableProgress() {

        val progress = findViewById<ProgressBar>(R.id.dialog_planned_crosses_progress)

        progress.visibility = View.INVISIBLE

    }

    fun enableProgress() {

        val progress = findViewById<ProgressBar>(R.id.dialog_planned_crosses_progress)

        progress.visibility = View.VISIBLE

    }
}