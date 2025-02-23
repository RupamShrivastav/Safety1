package com.example.safety

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.safety.ui.MainActivity

class SplashScreen : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        SharedPrefFile.init(this)
        val isLoggedIn = SharedPrefFile.getLoggedInfo(Constants.SP_LOGGED_INFO)

        if(isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish()
        }
    }
}