package com.example.safety.models

import com.google.gson.annotations.SerializedName
/**
 * Data class representing a request to reset a forgotten password.
 *
 * @property emailOrPhone The user's email address or phone number.
 * @property newPassword The new password to be set.
 * @property securityPIN The user's security PIN.
 */
data class ForgotPasswordRequest(
    @SerializedName("email_or_phone")
    val emailOrPhone: String,
    @SerializedName("new_password")
    val newPassword: String,
    @SerializedName("PIN")
    val securityPIN: String
)