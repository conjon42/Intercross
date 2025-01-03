package org.phenoapps.intercross.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import org.phenoapps.intercross.R

class ListAddDialog(
    private val activity: Activity,
    private val dialogTitle: String,
    private val items: Array<String?>,
    private val icons: IntArray,
    private val onItemClickListener: AdapterView.OnItemClickListener
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val listView = ListView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        layout.addView(listView)

        val adapter = ListAddAdapter(activity, items, icons)
        listView.adapter = adapter

        val builder = AlertDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setCancelable(true)
            .setView(layout)
            .setPositiveButton(getString(R.string.dialog_cancel)) { dialogInterface, _ -> dialogInterface.dismiss() }

        val dialog = builder.create()
        dialog.show()

        dialog.window?.let { window ->
            window.attributes = window.attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            onItemClickListener.onItemClick(parent, view, position, id)
            dialog.dismiss()
        }

        return dialog
    }

    private inner class ListAddAdapter(
        private val context: Activity,
        private val values: Array<String?>,
        private val icons: IntArray
    ) : ArrayAdapter<String?>(context, R.layout.list_item_dialog_with_icon, values) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: context.layoutInflater.inflate(
                R.layout.list_item_dialog_with_icon,
                parent,
                false
            )

            view.findViewById<ImageView>(R.id.icon)?.setImageResource(icons[position])
            view.findViewById<TextView>(R.id.spinnerTarget)?.text = values[position]

            return view
        }
    }
}