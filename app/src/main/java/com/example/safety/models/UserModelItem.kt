package com.example.safety.models

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a user model for the Safety App.
 * This model is used to store and retrieve user-related information from the database.
 *
 * @property organization The name of the organization the user belongs to.
 * @property organizationID The unique identifier for the organization (default is an empty string).
 * @property fullName The full name of the user.
 * @property userID A unique identifier for the user.
 * @property email The user's registered email address.
 * @property password The user's password.
 * @property phoneNumber The user's phone number.
 * @property securityPIN The user's 5-digit security PIN.
 * @property trustedContactName The name of the user's emergency/trusted contact.
 * @property trustedContactID The unique identifier for the trusted contact.
 * @property trustedContactNumber The phone number of the trusted contact.
 */
data class UserModelItem(
    @SerializedName("Organization") val organization: String, // Maps to JSON field "Organization"
    @SerializedName("OrganizationID") val organizationID: String = "", // Default empty string if not provided
    @SerializedName("FullName") val fullName: String, // Maps to JSON field "FullName"
    @SerializedName("UserID") val userID: String, // Maps to JSON field "PersonID"
    @SerializedName("Email") val email: String, // Maps to JSON field "Email"
    @SerializedName("Password") val password: String, // Maps to JSON field "Password"
    @SerializedName("PhoneNumber") val phoneNumber: String, // Maps to JSON field "PhoneNumber"
    @SerializedName("PIN") val securityPIN: String, // Maps to JSON field "PIN"
    @SerializedName("TrustedContactName") val trustedContactName: String, // Maps to JSON field "TrustedContactName"
    @SerializedName("TrustedContactID") val trustedContactID: String, // Maps to JSON field "TrustedContactID"
    @SerializedName("TrustedContactNumber") val trustedContactNumber: String // Maps to JSON field "TrustedContactNumber"
)
