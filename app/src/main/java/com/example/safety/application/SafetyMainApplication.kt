package com.example.safety.application

import android.app.Application
import com.example.safety.BuildConfig
import com.mappls.sdk.maps.Mappls
import com.mappls.sdk.services.account.MapplsAccountManager

class SafetyMainApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        MapplsAccountManager.getInstance().restAPIKey = BuildConfig.REST_MAP_SDK_KEY
        MapplsAccountManager.getInstance().mapSDKKey = BuildConfig.REST_MAP_SDK_KEY
        MapplsAccountManager.getInstance().atlasClientId = BuildConfig.CLIENT_ID_KEY
        MapplsAccountManager.getInstance().atlasClientSecret = BuildConfig.CLIENT_SECRET_KEY
        Mappls.getInstance(applicationContext)
    }
}