package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a generic API response.
 * Typically used to handle success or error messages from API calls.
 *
 * @property message A string message returned from the API.
 */
data class APIResponseModel(
    @SerializedName("message")
    val message: String
)
