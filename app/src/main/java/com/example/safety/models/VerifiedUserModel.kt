package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a verified user response.
 * This model is used to store the status of the verification process and the corresponding user details.
 *
 * @property status The verification status of the user (e.g., "Verified", "Unverified").
 * @property userData The user's detailed information, encapsulated in the UserModelItem class.
 */
data class VerifiedUserModel(
    @SerializedName("status") val status: String, // Verification status of the user
    @SerializedName("user_data") val userData: UserModelItem // User details
)
