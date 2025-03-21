package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email_or_phone")
    val emailOrPhone: String = "",
    @SerializedName("new_password")
    val newPassword: String = "",
    @SerializedName("PIN")
    val securityPIN: String = ""
)