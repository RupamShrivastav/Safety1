package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class UpdatePasswordRequest(
    @SerializedName("Email")
    val email: String = "",
    @SerializedName("NewPassword")
    val newPassword: String = "",
    @SerializedName("OldPassword")
    val oldPassword: String = ""
)