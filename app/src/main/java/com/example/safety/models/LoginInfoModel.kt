package com.example.safety.models

/**
 * Data class representing user login credentials.
 * Used for authentication when logging in to the application.
 *
 * @property email The user's email address.
 * @property password The user's password.
 */
data class LoginInfoModel(
    val email: String,
    val password: String
)
