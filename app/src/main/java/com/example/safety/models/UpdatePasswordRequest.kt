package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a request to update a user's password.
 *
 * @property email The user's email address (used for identification).
 * @property newPassword The new password to be set.
 * @property oldPassword The user's current password (for verification).
 */
data class UpdatePasswordRequest(
    @SerializedName("Email")
    val email: String, // Email is required
    @SerializedName("NewPassword")
    val newPassword: String, // New password is required
    @SerializedName("OldPassword")
    val oldPassword: String // Old password is required
)
