package com.example.safety.models

import com.google.gson.annotations.SerializedName

data class UpdateTrustedContactInfo(
    @SerializedName("TrustedContactName") val trustedContactName: String,
    @SerializedName("TrustedContactNumber") val trustedContactNumber: String
)