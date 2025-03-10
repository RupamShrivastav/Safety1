package com.example.safety

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safety.activity.LoginUserActivity
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.activity.MainActivity

/**
 * SplashScreen Activity
 *
 * This activity acts as a launcher screen when the app starts. It checks whether the user is
 * already logged in and navigates them to the appropriate screen.
 */
class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the shared preferences to access stored user data
        SharedPrefFile.init(this)

        // Retrieve login status from shared preferences
        val isLoggedIn = SharedPrefFile.getLoggedInfo(Constants.SP_LOGGED_INFO)

        if (isLoggedIn) {
            // If the user is logged in, navigate to the main dashboard
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close the splash screen
        } else {
            // If the user is not logged in, navigate to the login screen
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish() // Close the splash screen
        }
    }
}
