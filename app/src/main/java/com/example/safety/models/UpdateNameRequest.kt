package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a request to update a user's full name.
 *
 * @property email The user's email address (used for identification).
 * @property fullName The new full name to be set for the user.
 */
data class UpdateNameRequest(
    @SerializedName("Email")
    val email: String, // No default value, as email is required

    @SerializedName("FullName")
    val fullName: String // No default value, as new name is required
)