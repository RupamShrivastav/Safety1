package com.example.safety.common

/**
 * Object to store constant values used throughout the application.
 * This ensures consistency and prevents hardcoded values in multiple places.
 */
object Constants {

    // TAG for logging/debugging purposes
    const val TAG: String = "SafetyApp"

    // Shared Preferences keys for storing user-related data
    const val SP_USERDATA = "userdata"   // Key for storing user details
    const val SP_LOGGED_INFO = "loggedinfo"   // Key for checking user login status
    const val SP_ALL_USERS_BY_ORG = "AllUsersByOrg"  // Key for storing organization-specific user list

    // Firestore collection names
    const val FIRESTORE_COLLECTION = "UserLocation"  // Collection storing real-time user locations

    // API Endpoints (If applicable, to centralize API paths)
    const val BASE_URL = "https://rest-api-safety-qyd4.onrender.com/"

    // Default values for fallback scenarios
    const val DEFAULT_PHONE_NUMBER = "0"

    fun normalizePhoneNumber(number: String?): String? {
        var phoneNumber = number ?: return null
        phoneNumber = phoneNumber.replace(" ", "")
        if (phoneNumber.length > 10 && phoneNumber[0] == '+') {
            phoneNumber = phoneNumber.substring(3)
        }
        return phoneNumber
    }

}


