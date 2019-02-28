package edu.ksu.wheatgenetics.survey.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import edu.ksu.wheatgenetics.survey.GeoNavService
import edu.ksu.wheatgenetics.survey.NmeaParser
import edu.ksu.wheatgenetics.survey.databinding.FragmentSatellitePlotBinding
import kotlin.concurrent.fixedRateTimer

class SatViewFragment: Fragment() {

    //a data binding class that contains the layout views
    private lateinit var mBinding: FragmentSatellitePlotBinding

    private var parser = NmeaParser()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

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

        fixedRateTimer("GNSSUpdates", false, 0, 1) {
            handler.obtainMessage().sendToTarget()
        }


        mBinding = FragmentSatellitePlotBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    private val handler = Handler {

        mBinding.graph.subscribe(parser.gsv)

        true
    }
}