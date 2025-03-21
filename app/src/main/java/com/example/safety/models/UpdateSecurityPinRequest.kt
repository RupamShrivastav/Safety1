package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class UpdateSecurityPinRequest(
    @SerializedName("Email") val email: String,
    @SerializedName("NewPIN") val newPin: String,
    @SerializedName("OldPIN") val oldPin: String? = null,
    @SerializedName("Password") val password: String? = null
)
