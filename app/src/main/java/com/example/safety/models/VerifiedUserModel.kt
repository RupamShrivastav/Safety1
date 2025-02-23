package com.example.safety.models

import com.google.gson.annotations.SerializedName

data class VerifiedUserModel(
    @SerializedName("status") val status: String,
    @SerializedName("user_data") val userData: UserModelItem
)