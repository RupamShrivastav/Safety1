package com.example.safety.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.safety.models.UserModelItem
import com.example.safety.models.UsersListModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * SharedPrefFile
 *
 * A singleton object for managing SharedPreferences.
 * Stores user-related data securely in the local storage.
 */
object SharedPrefFile {

    private lateinit var preferences: SharedPreferences  // SharedPreferences instance
    private const val NAME = "SafetySharedPref"         // Name of SharedPreferences file
    private val gson = Gson()                           // Gson instance for JSON serialization

    /**
     * Initializes SharedPreferences with the application context.
     * Must be called in the `onCreate` method of Application or an Activity.
     */
    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    /**
     * Clears all stored data in SharedPreferences.
     */
    fun clearAllData() {
        Log.d(Constants.TAG, "All SharedPreferences data cleared.")
        preferences.edit().clear().apply()
    }

    /**
     * Stores a boolean value indicating whether the user is logged in.
     * @param key The key under which the value is stored.
     * @param value The boolean login status.
     */
    fun putLoggedInfo(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Retrieves the stored boolean value for login status.
     * @param key The key associated with the login status.
     * @return Boolean indicating if the user is logged in (default: `false`).
     */
    fun getLoggedInfo(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    /**
     * Stores a phone number as a string.
     * @param key The key associated with the phone number.
     * @param value The phone number to be stored.
     */
    fun putPhoneNum(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    /**
     * Retrieves a stored phone number.
     * @param key The key associated with the phone number.
     * @return The stored phone number (default: `"0"` if not found).
     */
    fun getPhoneNum(key: String): String {
        return preferences.getString(key, "0") ?: "0"
    }

    /**
     * Stores user data as a JSON string.
     * @param key The key under which the user data is stored.
     * @param user The user object to be serialized and stored.
     */
    fun putUserData(key: String, user: UserModelItem) {
        val userJson = gson.toJson(user)  // Convert user object to JSON
        preferences.edit().putString(key, userJson).apply()
    }

    /**
     * Retrieves stored user data and converts it back to `UserModelItem` object.
     * @param key The key associated with the stored user data.
     * @return A `UserModelItem` object if data is found, otherwise `null`.
     */
    fun getUserData(key: String): UserModelItem? {
        val userJson = preferences.getString(key, null)
        return try {
            userJson?.let { gson.fromJson(it, UserModelItem::class.java) }
        } catch (e: JsonSyntaxException) {
            Log.e(Constants.TAG, "Error parsing user data: ${e.message}")
            null
        }
    }

    /**
     * Stores a list of users by organization as a JSON string.
     * @param key The key under which the user list is stored.
     * @param allUsersByOrg The list of users to be serialized and stored.
     */
    fun putAllUsersByOrg(key: String, allUsersByOrg: UsersListModel) {
        val json = gson.toJson(allUsersByOrg)  // Convert user list to JSON
        preferences.edit().putString(key, json).apply()
    }

    /**
     * Retrieves a list of users by organization.
     * Converts the stored JSON string back to a `UsersListModel` object.
     * @param key The key associated with the stored user list.
     * @return A `UsersListModel` object if data is found, otherwise `null`.
     */
    fun getAllUsersByOrg(key: String): UsersListModel? {
        val json = preferences.getString(key, null)
        return try {
            json?.let { gson.fromJson(it, UsersListModel::class.java) }
        } catch (e: JsonSyntaxException) {
            Log.e(Constants.TAG, "Error parsing users list: ${e.message}")
            null
        }
    }
}
