package edu.ksu.wheatgenetics.survey.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.snackbar.Snackbar
import edu.ksu.wheatgenetics.survey.R
import edu.ksu.wheatgenetics.survey.data.ExperimentRepository
import edu.ksu.wheatgenetics.survey.data.Sample
import edu.ksu.wheatgenetics.survey.data.SampleRepository
import edu.ksu.wheatgenetics.survey.data.SurveyDatabase
import edu.ksu.wheatgenetics.survey.databinding.FragmentMapPlotsBinding
import edu.ksu.wheatgenetics.survey.viewmodels.SampleListViewModel

class SampleMapFragment: Fragment(), OnMapReadyCallback {

    //this object contains an array of sample models including sample name, person, and experiment id
    private lateinit var mViewModel: SampleListViewModel

    //a data binding class that contains the layout views
    private lateinit var mBinding: FragmentMapPlotsBinding

    private lateinit var mMap: GoogleMap

    private var mCurrentPlot: String = String()

    //private lateinit var mAdapter: SampleAdapter

    override fun onMapReady(map: GoogleMap) {

        mMap = map

        //map?.mapType = GoogleMap.MAP_TYPE_HYBRID
        //map?.isBuildingsEnabled = false
        //map?.isIndoorEnabled = false
        //map?.isTrafficEnabled = false
        map.clear()

        map.setOnMarkerClickListener {marker ->
            if (mCurrentPlot.isNotEmpty()) {
                //mBinding.markerTextView.text = it.title
                val s: Sample? = mViewModel.samples.value?.first { marker.title == it.name }
                s?.let {
                    mViewModel.update(it.apply {
                        plot = mCurrentPlot
                    })
                }
                mBinding.startPlot.isEnabled = true
                val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

                mapFragment.getMapAsync(this)
            }
            true
        }

        mViewModel.plotNames.observe(viewLifecycleOwner, Observer {
            it?.forEach { plot ->
                mViewModel.plots(plot).observe(viewLifecycleOwner, Observer {
                    if (it.isNotEmpty() && plot.isNotEmpty()) {
                        map.addPolygon(PolygonOptions()
                                .addAll(it.asIterable().map { LatLng(it.latitude, it.longitude) }))
                    }
                })
            }
        })

        mViewModel.samples.observe(viewLifecycleOwner, Observer{
            it.forEach { sample ->

                val latlng = LatLng(sample.latitude, sample.longitude)
                map.addMarker(MarkerOptions()
                        .position(latlng)
                        .title(sample.name)
                        .snippet(sample.person))
            }

            map.setLatLngBoundsForCameraTarget(LatLngBounds(
                    //most south-western latlng point
                    LatLng(it.minBy { it.latitude }?.latitude ?: 0.0,
                            it.minBy { it.longitude }?.longitude ?: 0.0),
                    //most north-eastern latlng pont
                    LatLng(it.maxBy { it.latitude }?.latitude ?: 0.0,
                            it.maxBy { it.longitude }?.longitude ?: 0.0)))

            map.moveCamera(CameraUpdateFactory.zoomTo(20f))
        })



        //view model observer that will update the google map fragment whenever
        //a lifecycle event occurs or the dataset is updated
        //1. Draw a polygon that connects all samples
        //2. Add a marker for each sample, which can be clicked on
        //3. Set the view of the scene by creating a LatLng boundary from the SW and NE most points
       /* mViewModel.samples.observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {

                val lats = it.map { s -> s.latitude }
                val lngs = it.map { s -> s.longitude }
                //mAdapter.submitList(it.asReversed())
                val pts: DoubleArray = doubleArrayOf(
                        lats.min() ?: 0.0, //south most point
                        lngs.min() ?: 0.0, //western most point
                        lats.max() ?: 0.0, //northern most point
                        lngs.max() ?: 0.0 //eastern most point
                )
                /*map.addPolygon(PolygonOptions()
                        .addAll(hull.asIterable().map { LatLng(it.latitude, it.longitude) }))*/
                val hull: Array<LatLng> = arrayOf(
                        LatLng(pts[0], pts[1]),
                        LatLng(pts[2], pts[1]),
                        LatLng(pts[2], pts[3]),
                        LatLng(pts[0], pts[3])
                )
                map.addPolygon(PolygonOptions()
                        .addAll(hull.asIterable())

                )

                it.forEach { sample ->

                    val latlng = LatLng(sample.latitude, sample.longitude)
                    map.addMarker(MarkerOptions()
                            .position(latlng)
                            .title(sample.name)
                            .snippet(sample.person))
                }

                map.setLatLngBoundsForCameraTarget(LatLngBounds(
                        //most south-western latlng point
                        LatLng(it.minBy { it.latitude }?.latitude ?: 0.0,
                                it.minBy { it.longitude }?.longitude ?: 0.0),
                        //most north-eastern latlng pont
                        LatLng(it.maxBy { it.latitude }?.latitude ?: 0.0,
                                it.maxBy { it.longitude }?.longitude ?: 0.0)))

                //10f is 'street-level' zoom
                map.moveCamera(CameraUpdateFactory.zoomTo(20f))
            }
        })*/
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //if arguments is null we don't have an experiment id and must return immediately
        arguments ?: findNavController().popBackStack()

        val experiment = SampleMapFragmentArgs.fromBundle(arguments!!).experiment
        mBinding = edu.ksu.wheatgenetics.survey.databinding.FragmentMapPlotsBinding
                .inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        mViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    val db = SurveyDatabase.getInstance(requireContext())
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return SampleListViewModel(experiment.id,
                                SampleRepository.getInstance(db.sampleDao()),
                                ExperimentRepository.getInstance(db.experimentDao())) as T
                    }
                }).get(SampleListViewModel::class.java)

        mBinding.startPlot.setOnClickListener {
            val input = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Plot"
            }

            val builder = AlertDialog.Builder(requireContext()).apply {

                setView(input)

                setPositiveButton("OK") { _, _ ->
                    val value = input.text.toString()
                    if (value.isNotEmpty()) {
                        mBinding.startPlot.isEnabled = false
                        mCurrentPlot = value
                        mapFragment.getMapAsync(this@SampleMapFragment)
                        Snackbar.make(it,
                                "Started plotting $value", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(it,
                                "You must enter a plot name.", Snackbar.LENGTH_LONG).show()
                    }
                }
                setTitle("Enter a new plot name")
            }
            builder.show()
        }

        return mBinding.root
    }
}