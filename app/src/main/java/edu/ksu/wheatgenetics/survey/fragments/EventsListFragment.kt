package edu.ksu.wheatgenetics.survey.fragments

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import edu.ksu.wheatgenetics.survey.adapters.EventsAdapter
import edu.ksu.wheatgenetics.survey.data.EventsRepository
import edu.ksu.wheatgenetics.survey.data.ExperimentRepository
import edu.ksu.wheatgenetics.survey.data.IntercrossDatabase
import edu.ksu.wheatgenetics.survey.data.SurveyDatabase
import edu.ksu.wheatgenetics.survey.databinding.FragmentEventsBinding
import edu.ksu.wheatgenetics.survey.viewmodels.EventsListViewModel
import edu.ksu.wheatgenetics.survey.viewmodels.ExperimentListViewModel

class EventsListFragment : Fragment() {

    private lateinit var mBinding: FragmentEventsBinding

    private lateinit var mAdapter: EventsAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentEventsBinding
                .inflate(inflater, container, false)
        mAdapter = EventsAdapter(mBinding.root.context)
        mBinding.recyclerView.adapter = mAdapter

        val viewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {

                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventsListViewModel(EventsRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).eventsDao())) as T

                    }
                }
        ).get(EventsListViewModel::class.java)


        /** TODO add swipe to delete
        mBinding.recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {

                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onInterceptTouchEvent(p0: RecyclerView, p1: MotionEvent): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })**/

        mBinding.button.setOnClickListener {
            askUserNewExperimentName(viewModel, it)
        }

        mBinding.saveButton.setOnClickListener {
            askUserNewExperimentName(viewModel, it)
        }

        viewModel.events.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                mAdapter.submitList(it.reversed())
                mBinding.recyclerView.smoothScrollToPosition(0)
            }
        })
        return mBinding.root
    }

    private fun askUserNewExperimentName(vm: EventsListViewModel, v: View) {

        val value = mBinding.editTextCross.text.toString()
        if (value.isNotEmpty()) {
            vm.addCrossEvent(mBinding.editTextCross.text.toString(),
                    mBinding.firstText.text.toString(), mBinding.secondText.text.toString())
            Snackbar.make(v,
                    "New Cross Event! $value added.", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(v,
                    "You must enter a cross name.", Snackbar.LENGTH_LONG).show()
        }
    }
}