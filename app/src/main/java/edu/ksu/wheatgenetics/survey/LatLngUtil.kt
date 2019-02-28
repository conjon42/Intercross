package edu.ksu.wheatgenetics.survey

import android.location.Location

/**
 * Created by Chaney on 1/30/2017.
 */

internal object LatLngUtil {

    /* uses the Haversine method to calculate distance between two GPS coordinates */
    fun distanceHaversine(a: Location, b: Location): Double? {

        val lata = a.latitude
        val lnga = a.longitude
        val latb = b.latitude
        val lngb = b.longitude
        val R = 6371.0 //radius of the Earth
        val latDst = Math.toRadians(latb - lata)
        val lngDst = Math.toRadians(lngb - lnga)
        val A = Math.sin(latDst / 2) * Math.sin(latDst / 2) + (Math.cos(Math.toRadians(lata)) * Math.cos(Math.toRadians(latb))
                * Math.sin(lngDst / 2) * Math.sin(lngDst / 2))
        val c = 2 * Math.atan2(Math.sqrt(A), Math.sqrt(1 - A))
//double height = el1 - el2;
        //dst = Math.pow(dst, 2);
        //return Math.sqrt(dst);
        return R * c * 1000.0
    }

    fun geodesicDestination(start: Location, bearing: Double, distance: Double): Location {

        val latRads = Math.toRadians(start.latitude)
        val lngRads = Math.toRadians(start.longitude) //(Degrees * Math.PI) / 180.0;
        //final double bearing = azimuth;//location.getBearing(); //created weighted vector with bearing...?
        val R = 6371.0 //radius of the Earth
        val angDst = distance / 6371.0 // d/R distance to point B over Earth's radius
        val lat2 = Math.asin(Math.sin(latRads) * Math.cos(angDst) + Math.cos(latRads) * Math.sin(angDst) * Math.cos(bearing))
        val lng2 = lngRads + Math.atan2(Math.sin(bearing) * Math.sin(angDst) * Math.cos(latRads),
                Math.cos(angDst) - Math.sin(latRads) * Math.sin(lat2))

        val l = Location("end point")
        l.latitude = Math.toDegrees(lat2)
        l.longitude = Math.toDegrees(lng2)
        return l
    }
}
