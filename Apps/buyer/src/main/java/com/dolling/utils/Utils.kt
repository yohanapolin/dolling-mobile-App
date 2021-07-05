package com.dolling.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import kotlin.math.absoluteValue

object Utils {

    fun countDaysTimestamp(date: Date): String {
        return when (val diff = ((date.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()) {
            0 -> "today"
            1 -> "yesterday"
            else -> "${diff.absoluteValue} days ago"
        }
    }

    fun countDistanceFromTwoGeoPoints(point1: LatLng, point2: LatLng): Int {
        val point1Location = Location("").apply {
            latitude = point1.latitude
            longitude = point2.longitude
        }

        val point2Location = Location("").apply {
            latitude = point2.latitude
            longitude = point2.longitude
        }

        return point1Location.distanceTo(point2Location).toInt()
    }

    fun getDummyMovingSellerGeoPoints(): List<LatLng> {
        val result = arrayListOf<LatLng>()
        result.add(LatLng(-7.918211301079948, 112.58667465643074))
        result.add(LatLng(-7.918106082854895, 112.58620251842643))
        result.add(LatLng(-7.917895646324169, 112.5861435011759))
        result.add(LatLng(-7.917821603815572, 112.58576972525583))
        result.add(LatLng(-7.917494257828618, 112.58606087702513))
        result.add(LatLng(-7.917298582825966, 112.58627841521589))
        return result
    }
}