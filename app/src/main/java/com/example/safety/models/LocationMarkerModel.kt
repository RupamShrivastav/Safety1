package com.example.safety.models

/**
 * Data class representing a location marker on a map.
 * Used to store latitude, longitude, and a title for the marker.
 *
 * @property lat The latitude of the location.
 * @property lng The longitude of the location.
 * @property markerTitle The title or label for the marker.
 */
data class LocationMarkerModel(
    val lat: Double,
    val lng: Double,
    val markerTitle: String
)