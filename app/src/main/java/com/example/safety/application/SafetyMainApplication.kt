package com.example.safety.application

import android.app.Application
import com.example.safety.BuildConfig
import com.mappls.sdk.maps.Mappls
import com.mappls.sdk.services.account.MapplsAccountManager

/**
 * SafetyMainApplication
 *
 * Custom Application class responsible for initializing global configurations
 * such as the Mappls SDK at app startup.
 */
class SafetyMainApplication : Application() {

    /**
     * Called when the application is first created.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize Mappls SDK with authentication credentials
        setupMapplsSDK()
    }

    /**
     * Configures Mappls SDK with API keys and authentication credentials.
     * The API keys are stored securely in BuildConfig to prevent exposure.
     */
    private fun setupMapplsSDK() {
        // Setting up REST API Key for Mappls services
        MapplsAccountManager.getInstance().restAPIKey = BuildConfig.REST_MAP_SDK_KEY

        // Setting up SDK Key for Mappls mapping services
        MapplsAccountManager.getInstance().mapSDKKey = BuildConfig.REST_MAP_SDK_KEY

        // Client ID for Mappls authentication
        MapplsAccountManager.getInstance().atlasClientId = BuildConfig.CLIENT_ID_KEY

        // Client Secret for authentication
        MapplsAccountManager.getInstance().atlasClientSecret = BuildConfig.CLIENT_SECRET_KEY

        // Initialize Mappls SDK with the application context
        Mappls.getInstance(applicationContext)
    }
}
