package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class UpdatePhoneNumberRequest(
    @SerializedName("Email")
    val email: String = "",
    @SerializedName("PhoneNumber")
    val phoneNumber: String = ""
)