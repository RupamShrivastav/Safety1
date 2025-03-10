package com.example.safety.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.safety.api.RetrofitInstance
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityLoginUserBinding
import com.example.safety.models.LoginInfoModel
import com.example.safety.models.VerifiedUserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * LoginUserActivity
 *
 * This activity handles user login functionality.
 * It provides UI for users to enter their credentials and authenticate themselves.
 */
class LoginUserActivity : AppCompatActivity() {

    private val TAG = "LoginUserActivity" // Tag for logging

    private lateinit var binding: ActivityLoginUserBinding // View binding for activity layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Retrofit instance for network calls
        val retrofit = RetrofitInstance.initialize()

        // Initialize shared preferences for storing user data
        val sharedpref = SharedPrefFile
        sharedpref.init(this)

        // Set up view binding for activity layout
        binding = ActivityLoginUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back button press to finish activity
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)

        // Navigate to registration screen when 'New User? Register' is clicked
        binding.tVNewUserRegister.setOnClickListener {
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }

        // Handle login button click
        binding.loginBtn.setOnClickListener {
            val email = binding.tilemailLogin.editText?.text.toString()
            val password = binding.tilpasswordLogin.editText?.text.toString()

            // Check if email or password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Make API call to verify user credentials
                retrofit.verifyUser(LoginInfoModel(email, password))
                    .enqueue(object : Callback<VerifiedUserModel> {
                        override fun onResponse(
                            call: Call<VerifiedUserModel>,
                            response: Response<VerifiedUserModel>
                        ) {
                            Log.d("Rupam $TAG", "Response: ${response.body()?.userData}")

                            when (response.code()) {
                                200 -> { // Successful login
                                    sharedpref.putLoggedInfo(Constants.SP_LOGGED_INFO, true)
                                    sharedpref.putUserData(
                                        Constants.SP_USERDATA,
                                        response.body()!!.userData
                                    )

                                    // Show progress bar before navigating to main screen
                                    binding.progressBar.visibility = View.VISIBLE
                                    binding.mainContent.visibility = View.GONE

                                    binding.progressBar.postDelayed({
                                        binding.progressBar.visibility = View.GONE
                                        binding.mainContent.visibility = View.VISIBLE
                                    }, 1500)

                                    startActivity(Intent(baseContext, MainActivity::class.java))
                                }
                                401 -> { // Incorrect password
                                    Toast.makeText(baseContext, "Wrong Password", Toast.LENGTH_SHORT).show()
                                }
                                404 -> { // User not found
                                    Toast.makeText(
                                        baseContext,
                                        "No User Found, Recheck Email or Register as New User",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<VerifiedUserModel>, t: Throwable) {
                            Log.e("Rupam $TAG", "Response: ${t.message}") // Log error on API failure
                        }
                    })
            }
        }
    }
}
