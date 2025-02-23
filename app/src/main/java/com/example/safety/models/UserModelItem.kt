package com.example.safety.models

import com.google.gson.annotations.SerializedName

data class UserModelItem(
    @SerializedName("Organization") val organization: String,
    @SerializedName("OrganizationID") val organizationID: String="",
    @SerializedName("FullName") val fullName: String,
    @SerializedName("PersonID") val personID: String ,
    @SerializedName("Email") val email: String,
    @SerializedName("Password") val password: String,
    @SerializedName("PhoneNumber") val phoneNumber: String,
    @SerializedName("TrustedContactName") val trustedContactName: String,
    @SerializedName("TrustedContactID") val trustedContactID: String,
    @SerializedName("TrustedContactNumber") val trustedContactNumber: String,
)