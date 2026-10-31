package org.generosity.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Great-circle distance in meters. Used for nearby ordering and the
 * proximity notification boundary; it never stores the user's location.
 */
fun Coordinates.distanceMetersTo(other: Coordinates): Double {
    val startLatitude = latitude.toRadians()
    val endLatitude = other.latitude.toRadians()
    val deltaLatitude = (other.latitude - latitude).toRadians()
    val deltaLongitude = (other.longitude - longitude).toRadians()

    val haversine = sin(deltaLatitude / 2).pow(2) +
        cos(startLatitude) * cos(endLatitude) * sin(deltaLongitude / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * atan2(sqrt(haversine), sqrt(1 - haversine))
}

private fun Double.toRadians(): Double = this * 0.017453292519943295
