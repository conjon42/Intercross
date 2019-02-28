package edu.ksu.wheatgenetics.survey

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Pair
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class GeoNavService : Service() {

    private var mMaxAccuracy: Float = java.lang.Float.MAX_VALUE
    private var mMinDist: Float = java.lang.Float.MIN_VALUE
    private var mMinTime: Long = java.lang.Long.MIN_VALUE

    private val mLocationListener: AccurateLocationListener by lazy {
        AccurateLocationListener()
    }

    private val mLocManager: LocationManager by lazy {
        this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreate() {

        super.onCreate()


        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mMinTime, mMinDist, mLocationListener)
            //mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
            //        mMinTime, mMinDist, mLocationListener)
            mLocManager.addNmeaListener { s: String?, l: Long ->
                    broadcast(s)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = "survey_foreground_channel"
        val notification = NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.drawable.ic_satellite_variant)
                .setContentTitle("Survey")
                .setContentText("Survey GPS Running").build()
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            val nc = NotificationChannel(channel,
                    "Channel for Survey Lat/Lng Coordinates",
                    NotificationManager.IMPORTANCE_LOW).apply {
                enableVibration(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(nc)
        }
        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    //function to pass data to activities listening to this service
    private fun <T> broadcast(data: T) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(when (data) {
                is Pair<*,*> -> Intent(BROADCAST_LOCATION).apply {
                    putExtra(LAT, data.first as Double)
                    putExtra(LNG, data.second as Double)
                }
                is Location -> Intent(BROADCAST_LOCATION).apply {
                    putExtra(LOCATION, data)
                }
                is String -> Intent(BROADCAST_PLOT_ID).apply {
                    putExtra(PLOT_ID, data)
                }
                is Float -> Intent(BROADCAST_ACCURACY).apply {
                    putExtra(ACCURACY, data)
                }
                else -> Intent()
            })
    }

    private inner class AccurateLocationListener : LocationListener {

        override fun onLocationChanged(location: Location) {

            //check if accuracy is below the maximum requested accuracy
            if (location.hasAccuracy() && location.accuracy <= mMaxAccuracy) {
                //broadcast<Pair<Double, Double>>(Pair(location.latitude, location.longitude))
                //broadcast<Float>(location.accuracy)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    internal companion object {

        val BROADCAST_LOCATION = "edu.ksu.wheatgenetics.fieldmapping.BROADCAST_LOCATION"

        val BROADCAST_PLOT_ID = "edu.ksu.wheatgenetics.fieldmapping.BROADCAST_PLOT_ID"

        val BROADCAST_ACCURACY = "edu.ksu.wheatgenetics.fieldmapping.BROADCAST_ACCURACY"

        //key for defining gps parameters
        val GPS_PARAMETERS = "edu.ksu.wheatgenetics.fieldmapping.PARAMETERS"

        //extras
        internal val MAP_EXTRA = "edu.ksu.wheatgenetics.geonav.MAP_EXTRA"

        //key for accurate gps location data
        val LAT = "edu.ksu.wheatgenetics.survey.LAT"
        val LNG = "edu.ksu.wheatgenetics.survey.LNG"

        val LOCATION = "edu.ksu.wheatgenetics.fieldmapping.LOCATION"

        val PLOT_ID = "edu.ksu.wheatgenetics.fieldmapping.PLOT_ID"

        val ACCURACY = "edu.ksu.wheatgenetics.fieldmapping.ACCURACY"

        val filter = IntentFilter().apply {
            //addAction(SurveyActivity.BROADCAST_BT_OUTPUT)
            addAction(GeoNavService.BROADCAST_LOCATION)
            addAction(GeoNavService.BROADCAST_ACCURACY)
            addAction(GeoNavService.BROADCAST_PLOT_ID)
        }
    }
}
