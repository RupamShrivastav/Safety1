package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a request to update a user's security PIN.
 *
 * @property email The user's email address (used for identification).
 * @property newPin The new security PIN to be set.
 * @property oldPin The user's current security PIN (optional, for verification).
 * @property password The user's password (optional, for verification if oldPin is not provided).
 */
data class UpdateSecurityPinRequest(
    @SerializedName("Email") val email: String, // Email is required
    @SerializedName("NewPIN") val newPin: String,  // New PIN is required
    @SerializedName("OldPIN") val oldPin: String? = null, // Old PIN is optional
    @SerializedName("Password") val password: String? = null // Password is optional
)