package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the response from the server after a new user registration.
 *
 * @property message A message from the server indicating the status of the registration
 * @property userData This contains the newly registered user's data.   This will  only be present on a successful registration
 */
data class NewRegistrationResponse(
    @SerializedName("message")
    val message: String, // Removed default empty string
    @SerializedName("user_data")
    val userData: UserModelItem? = null // Made userData nullable, no default.
)