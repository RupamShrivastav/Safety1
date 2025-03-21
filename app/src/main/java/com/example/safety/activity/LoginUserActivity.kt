package com.example.safety.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.safety.api.ApiService
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityLoginUserBinding
import com.example.safety.models.APIResponseModel
import com.example.safety.models.ForgotPasswordRequest
import com.example.safety.models.LoginInfoModel
import com.example.safety.models.VerifiedUserModel
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * LoginUserActivity
 * Handles user login and password recovery.
 */
class LoginUserActivity : AppCompatActivity() {

    private val TAG = "LoginUserActivity" // Logging tag
    private lateinit var binding: ActivityLoginUserBinding // View binding
    private lateinit var apiService: ApiService // Retrofit API service
    private lateinit var sharedPref: SharedPrefFile // Shared preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginUserBinding.inflate(layoutInflater) // Initialize view binding
        setContentView(binding.root)

        initDependencies() // Initialize API service and shared preferences
        setupBackButton() // Configure back button
        setupNavigation()  // Set up navigation to other activities
        setupForgotPasswordUI() // Configure forgot password UI
        setupLoginButton() // Configure login button
        setupForgotPasswordButton() // Configure forgot password button

    }


    // Initializes API service and shared preferences.
    private fun initDependencies() {
        apiService = com.example.safety.api.RetrofitInstance.initialize() // Initialize Retrofit
        sharedPref = SharedPrefFile
        sharedPref.init(this) // Initialize SharedPref

    }


    // Sets up back button behavior.
    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the activity
            }
        })
    }

    // Sets up navigation to other activities.
    private fun setupNavigation() {
        binding.tVNewUserRegister.setOnClickListener { // Navigate to registration
            startActivity(Intent(this, RegisterUserActivity::class.java))
        }
    }

    // Sets up forgot password UI elements and visibility toggles.
    private fun setupForgotPasswordUI() {
        binding.apply {
            forgetPassword.setOnClickListener { // Show forgot password UI
                forgotPasswordLL.isVisible = true
                loginLL.isVisible = false
            }
            cancelBtnFP.setOnClickListener { // Hide forgot password UI
                forgotPasswordLL.isVisible = false
                loginLL.isVisible = true
            }
        }
    }

    // Configures the login button click listener and API call.
    private fun setupLoginButton() {
        binding.loginBtn.setOnClickListener {
            val email = binding.tilemailLogin.editText?.text.toString()
            val password = binding.tilpasswordLogin.editText?.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Early return if fields are blank
            }

            binding.progressBar.visibility = View.VISIBLE // Show progress bar
            binding.mainContent.visibility = View.GONE // Hide main content

            apiService.verifyUser(LoginInfoModel(email, password)).enqueue(object : Callback<VerifiedUserModel> { // Make API call
                override fun onResponse(call: Call<VerifiedUserModel>, response: Response<VerifiedUserModel>) {
                    handleLoginResponse(response) // Handle successful response
                    binding.progressBar.visibility = View.GONE // Hide progress bar
                    binding.mainContent.visibility = View.VISIBLE // Show main content
                }

                override fun onFailure(call: Call<VerifiedUserModel>, t: Throwable) {
                    handleLoginFailure(t) // Handle API call failure
                    binding.progressBar.visibility = View.GONE // Hide progress bar
                    binding.mainContent.visibility = View.VISIBLE // Show main content
                }
            })
        }
    }

    // Handles the login API response.
    private fun handleLoginResponse(response: Response<VerifiedUserModel>) {
        try {
            if (response.isSuccessful) { // Successful login
                sharedPref.putLoggedInfo(Constants.SP_LOGGED_INFO, true) // Store login status
                sharedPref.putUserData(Constants.SP_USERDATA, response.body()!!.userData)  // Store user data
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show() // Show success message. Using 'this', as the context is clear
                startActivity(Intent(this, MainActivity::class.java)) // Navigate to MainActivity
                finish() // Finish LoginActivity
            } else { // Handle error responses (e.g., 400, 401, 404, 500)
                val errorMessage = try {
                    JSONObject(response.errorBody()?.string() ?: "").getString("status") // Parse error message
                } catch (e: Exception) {
                    "Unknown error" // Default error message
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show() // Show error message
                Log.e(TAG, "Login error: $errorMessage") // Log the error
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login: ${e.message}", e) // Log exception
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show() // Show generic error
        }
    }

    // Handles API call failure.
    private fun handleLoginFailure(t: Throwable) {
        Log.e(TAG, "Login API call failed: ${t.message}", t) // Log the failure
        Toast.makeText(this, "Login failed. Check your internet connection.", Toast.LENGTH_SHORT).show() // Show error message
    }

    // Configures the forgot password button click listener and API call.
    private fun setupForgotPasswordButton() {
        binding.resetPasswordBtnFP.setOnClickListener {
            val emailOrPhone = binding.emailOrPhoneFP.editText?.text.toString()
            val securityPIN = binding.securityPINFP.editText?.text.toString()
            val newPassword = binding.newPasswordFP.editText?.text.toString()

            if (emailOrPhone.isBlank() || securityPIN.isBlank() || newPassword.isBlank()) { // Check for empty fields
                Toast.makeText(this, "Enter all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Early return if fields are blank
            }

            val forgotPasswordRequest = ForgotPasswordRequest(emailOrPhone, securityPIN, newPassword) // Create request object

            apiService.forgotPassword(forgotPasswordRequest).enqueue(object : Callback<APIResponseModel> { // Make API call
                override fun onResponse(call: Call<APIResponseModel>, response: Response<APIResponseModel>) {
                    handleForgotPasswordResponse(response) // Handle the response
                }

                override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                    handleForgotPasswordFailure(t)  // Handle API call failure
                }
            })
        }
    }

    // Handles the forgot password API response.
    private fun handleForgotPasswordResponse(response: Response<APIResponseModel>) {
        when (response.code()) { // Check HTTP status code
            200 -> handleForgotPasswordSuccess() // Successful password reset
            404 -> handleForgotPasswordNotFoundError() // User not found
            401 -> handleForgotPasswordUnauthorizedError() // Invalid credentials
            else -> handleForgotPasswordGenericError() // Other errors
        }
    }

    // Handles successful password reset.
    private fun handleForgotPasswordSuccess() {
        Toast.makeText(this, "Password reset successful", Toast.LENGTH_SHORT).show() // Show success message
        navigateToLogin() // Navigate to login screen
    }

    // Handles user not found error.
    private fun handleForgotPasswordNotFoundError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show() // Show error message
        navigateToLogin() // Navigate to login screen
    }

    // Handles invalid credentials error.
    private fun handleForgotPasswordUnauthorizedError() {
        Toast.makeText(this, "Invalid Email or PIN", Toast.LENGTH_SHORT).show() // Show error message
        navigateToLogin() // Navigate to login screen
    }

    // Handles other errors during forgot password.
    private fun handleForgotPasswordGenericError() {
        Toast.makeText(this, "Password reset failed", Toast.LENGTH_SHORT).show() // Show error message
    }

    // Handles API call failure for forgot password.
    private fun handleForgotPasswordFailure(t: Throwable) {
        Log.e(TAG, "Forgot password API call failed: ${t.message}", t) // Log the failure
        Toast.makeText(this, "Forgot password failed. Check your internet connection.", Toast.LENGTH_SHORT).show()  // Show error message
    }

    // Navigates back to the login screen.
    private fun navigateToLogin() {
        finish() // Finish current activity
        startActivity(Intent(this, LoginUserActivity::class.java)) // Start LoginActivity
    }
}
