package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the request model for updating a trusted contact.
 * This model is used when sending data to the server to update the user's emergency contact details.
 *
 * @property trustedContactName The name of the trusted contact person.
 * @property trustedContactNumber The phone number of the trusted contact.
 */
data class UpdateTrustedContactRequest(
    @SerializedName("Email") val email: String, // Maps to JSON field "TrustedContactName"
    @SerializedName("TrustedContactName") val trustedContactName: String, // Maps to JSON field "TrustedContactName"
    @SerializedName("TrustedContactNumber") val trustedContactNumber: String // Maps to JSON field "TrustedContactNumber"
)
