package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class UpdateNameRequest(
    @SerializedName("Email")
    val email: String = "",
    @SerializedName("FullName")
    val fullName: String = ""
)