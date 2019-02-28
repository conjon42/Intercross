package edu.ksu.wheatgenetics.survey.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.ksu.wheatgenetics.survey.R
import edu.ksu.wheatgenetics.survey.data.Experiment
import edu.ksu.wheatgenetics.survey.databinding.ListItemExperimentBinding
import edu.ksu.wheatgenetics.survey.fragments.ExperimentListFragmentDirections
import edu.ksu.wheatgenetics.survey.viewmodels.ExperimentViewModel

class ExperimentAdapter(
        val context: Context
) : ListAdapter<Experiment, ExperimentAdapter.ViewHolder>(ExperimentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.list_item_experiment, parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        getItem(position).let { experiment ->
            with(holder) {
                itemView.tag = experiment
                bind(createOnClickListener(experiment), experiment)
            }
        }
    }

    private fun createOnClickListener(experiment: Experiment): View.OnClickListener {
        return View.OnClickListener {
            val direction =
                    ExperimentListFragmentDirections.actionExperimentFragmentToSampleListFragment(experiment)
            it.findNavController().navigate(direction)
        }
    }

    class ViewHolder(
            private val binding: ListItemExperimentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: View.OnClickListener, experiment: Experiment) {

            with(binding) {
                clickListener = listener
                viewModel = ExperimentViewModel(
                        experiment
                )
                executePendingBindings()
            }
        }
    }
}

private class ExperimentDiffCallback : DiffUtil.ItemCallback<Experiment>() {

    override fun areItemsTheSame(oldItem: Experiment, newItem: Experiment): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Experiment, newItem: Experiment): Boolean {
        return oldItem.name == newItem.name
    }
}