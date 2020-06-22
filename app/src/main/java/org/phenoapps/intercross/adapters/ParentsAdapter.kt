package org.phenoapps.intercross.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.databinding.ListItemSelectableParentRowBinding

class ParentsAdapter(private val listModel: ParentsListViewModel)
    : ListAdapter<Parent, ParentsAdapter.ViewHolder>(Parent.Companion.DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(

                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_selectable_parent_row, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { item ->

            with(holder) {

                bind(item)
            }
        }
    }

    inner class ViewHolder(private val binding: ListItemSelectableParentRowBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(p: Parent) {

            with(binding) {

                parent = p

//                if (event.type == CrossType.POLY) {
//
//                    this.textField.setBackgroundColor(Color.GREEN)
//
//                } else {
//
//                    this.textField.setBackgroundResource(R.drawable.cell)
//                }

                doneCheckBox.isChecked = p.selected

                linearLayout3.setOnClickListener {

                    doneCheckBox.isChecked=!doneCheckBox.isChecked

                    listModel.update(p.apply {
                        selected = doneCheckBox.isChecked
                    })
                }

                executePendingBindings()
            }
        }
    }
}