package com.example.safety.models


/**
 * Data class representing a user in the Safety App.
 * This model is used to store and retrieve real-time user data,
 * including their location, battery percentage, and network connection status.
 *
 * @property name The full name of the user.
 * @property phoneNumber The contact number of the user.
 * @property lat The latitude of the user's current location (default is "0").
 * @property long The longitude of the user's current location (default is "0").
 * @property batPer The battery percentage of the user's device (default is 0).
 * @property connectionInfo Information about the user's network connection status (e.g., WiFi or Mobile Data).
 */
data class Users(
    var name: String = "",  // User's full name, default is an empty string
    var phoneNumber: String = "", // User's phone number, default is an empty string
    var lat: String = "0", // Latitude of the user's location, default is "0"
    var long: String = "0", // Longitude of the user's location, default is "0"
    var batPer: Int = 0, // Battery percentage of the user's device, default is 0
    var connectionInfo: String = "" // Network connection status (e.g., "WiFi", "4G"), default is an empty string
)

