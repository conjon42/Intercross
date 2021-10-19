package org.phenoapps.intercross.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.BaseParent
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.databinding.ListItemSelectableParentRowBinding

class ParentsAdapter(private val listModel: ParentsListViewModel,
                    private val groupModel: PollenGroupListViewModel)
    : ListAdapter<BaseParent, ParentsAdapter.ViewHolder>(BaseParent.Companion.DiffCallback()) {

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

        fun bind(p: BaseParent) {

            with(binding) {

                if (p is Parent) {

                    name = p.name

                    checked = p.selected

                } else if (p is PollenGroup) {

                    this.textField.setBackgroundColor(Color.parseColor("#42FF5722"))

                    name = p.name

                    checked = p.selected
                }

                linearLayout.setOnClickListener {

                    doneCheckBox.isChecked=!doneCheckBox.isChecked

                    if (p is Parent) {

                        listModel.update(p.apply {

                            selected = doneCheckBox.isChecked

                        })
                    } else if (p is PollenGroup) {

                        groupModel.update(p.apply {

                            selected = doneCheckBox.isChecked

                        })
                    }
                }
            }
        }
    }
}