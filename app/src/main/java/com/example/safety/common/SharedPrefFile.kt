package com.example.safety.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.safety.models.UserModelItem
import com.example.safety.models.UsersListModel
import com.google.gson.Gson

object SharedPrefFile {

    private lateinit var preferences: SharedPreferences
    private const val NAME = "SafetySharedPref"
    private val gson = Gson()


    fun init(context : Context){
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun clearAllData(){
        Log.d(Constants.TAG,"All data cleared !!!")
        preferences.edit().clear().apply()
    }

    fun putLoggedInfo(key: String, value: Boolean){
        preferences.edit().putBoolean(key,value).apply()
    }

    fun getLoggedInfo(key : String): Boolean{
        return preferences.getBoolean(key,false)
    }

    fun putPhoneNum(key: String,value: String){
        preferences.edit().putString(key, value).apply()
    }

    fun getPhoneNum(key: String): String? {
        return preferences.getString(key,"0")
    }

    fun putUserData(key: String, user: UserModelItem) {
        val userJson = gson.toJson(user)
        preferences.edit().putString(key, userJson).apply()
    }

    fun getUserData(key: String): UserModelItem? {
        val userJson = preferences.getString(key, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserModelItem::class.java)
        } else {
            null
        }
    }

    fun putAllUsersByOrg(key: String, allUsersByOrg:UsersListModel){
        val json = gson.toJson(allUsersByOrg)
        preferences.edit().putString(key,json).apply()
    }

    fun getAllUsersByOrg(key: String) : UsersListModel?{
        val json = preferences.getString(key,null)
        return if(json!=null){
            gson.fromJson(json, UsersListModel::class.java)
        }else{
            null
        }
    }
}