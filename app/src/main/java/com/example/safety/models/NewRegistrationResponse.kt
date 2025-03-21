package com.example.safety.models


import com.google.gson.annotations.SerializedName

data class NewRegistrationResponse(
    @SerializedName("message")
    val message: String = "",
    @SerializedName("user_data")
    val userData: UserModelItem
)