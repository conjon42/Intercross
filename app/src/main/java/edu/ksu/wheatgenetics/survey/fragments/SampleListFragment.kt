package edu.ksu.wheatgenetics.survey.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import edu.ksu.wheatgenetics.survey.GeoNavService
import edu.ksu.wheatgenetics.survey.NmeaParser
import edu.ksu.wheatgenetics.survey.R
import edu.ksu.wheatgenetics.survey.adapters.SampleAdapter
import edu.ksu.wheatgenetics.survey.data.Experiment
import edu.ksu.wheatgenetics.survey.data.ExperimentRepository
import edu.ksu.wheatgenetics.survey.data.SampleRepository
import edu.ksu.wheatgenetics.survey.data.SurveyDatabase
import edu.ksu.wheatgenetics.survey.databinding.FragmentListSampleBinding
import edu.ksu.wheatgenetics.survey.viewmodels.SampleListViewModel
import kotlin.concurrent.fixedRateTimer

class SampleListFragment: Fragment() {

    //this object contains an array of sample models including sample name, person, and experiment id
    private lateinit var mViewModel: SampleListViewModel

    //a data binding class that contains the layout views
    private lateinit var mBinding: FragmentListSampleBinding

    private lateinit var mExperiment: Experiment

    private var parser = NmeaParser()

    private lateinit var mAdapter: SampleAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        val lbm = LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {

                    if (intent.hasExtra(GeoNavService.PLOT_ID)) {
                        parser.parse(intent
                                .getStringExtra(GeoNavService.PLOT_ID))
                        /*Log.d("NMEA", intent
                                .getStringExtra(GeoNavService.PLOT_ID))*/

                    }
                }
            }, GeoNavService.filter)
        }

        //if arguments is null we don't have an experiment id and must return immediately
        //arguments ?: findNavController().popBackStack()

        parser = NmeaParser()

        fixedRateTimer("GNSSUpdates", false, 0, 1) {
            handler.obtainMessage().sendToTarget()
        }

        mExperiment = SampleListFragmentArgs.fromBundle(arguments!!).experiment
        mBinding = edu.ksu.wheatgenetics.survey.databinding.FragmentListSampleBinding
                .inflate(inflater, container, false)

        mAdapter = SampleAdapter(mBinding.root.context)

        mBinding.recyclerView.adapter = mAdapter

        mViewModel = ViewModelProviders.of(this,
            object : ViewModelProvider.NewInstanceFactory() {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SampleListViewModel(mExperiment.id, SampleRepository.getInstance(
                        SurveyDatabase.getInstance(requireContext()).sampleDao()),
                        ExperimentRepository.getInstance(SurveyDatabase.getInstance(requireContext()).experimentDao())) as T
            }
        }).get(SampleListViewModel::class.java)

        mViewModel.samples.observe(viewLifecycleOwner, Observer {samples ->
            samples.let {
                mAdapter.submitList(samples.asReversed())
            }
        })

        mBinding.submitSample.setOnClickListener {
            val input = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Sample"
            }

            val builder = AlertDialog.Builder(requireContext()).apply {

                setView(input)

                setPositiveButton("OK") { _, _ ->
                    val value = input.text.toString()
                    if (value.isNotEmpty() && mBinding.latTextView.text.isNotBlank()
                            && mBinding.lngTextView.text.isNotBlank()) {
                        //TODO ADD PERSON

                        mViewModel.addSample(mExperiment, value, mBinding.latTextView.text.toString().toDouble(),
                                mBinding.lngTextView.text.toString().toDouble(), "CHANEY")
                        Snackbar.make(it,
                                "New sample $value added.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(it,
                                "You must enter a sample name.", Snackbar.LENGTH_LONG).show()
                    }
                }
                setTitle("Enter a new experiment name")
            }
            builder.show()
        }

        return mBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.activity_main_toolbar, menu)

       // return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map_locations -> {
                findNavController().navigate(
                        SampleListFragmentDirections
                                .actionSampleListFragmentToMapFragment(mExperiment))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val handler = Handler {

        mBinding.latTextView.text = parser.latitude
        mBinding.lngTextView.text = parser.longitude
        mBinding.accTextView.text = parser.fix
        mBinding.spdTextView.text = parser.speed
        mBinding.utcTextView.text = parser.utc
        mBinding.brgTextView.text = parser.bearing
        mBinding.satTextView.text = parser.satellites
        mBinding.altTextView.text = parser.altitude
         true
    }
}