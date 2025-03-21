
package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a request to update a user's phone number.
 *
 * @property email The user's email address (used for identification).
 * @property phoneNumber The new phone number to be set.
 */
data class UpdatePhoneNumberRequest(
    @SerializedName("Email")
    val email: String,  // Email is required
    @SerializedName("PhoneNumber")
    val phoneNumber: String // New phone number is required
)
