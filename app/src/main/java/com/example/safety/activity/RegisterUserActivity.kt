package com.example.safety.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safety.R
import com.example.safety.api.RetrofitInstance
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityRegisterUserBinding
import com.example.safety.models.NewRegistrationResponse
import com.example.safety.models.UserModelItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * RegisterUserActivity
 *
 * This activity allows new users to sign up for an account.
 * It collects user details and stores them in the database.
 */
class RegisterUserActivity : AppCompatActivity() {
    private val TAG = "Register User" // Tag for logging purposes
    private lateinit var binding: ActivityRegisterUserBinding // View binding for UI elements
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent> // Launcher for picking contacts
    var phoneNum = "" // Stores the selected contact's phone number
    var name = "" // Stores the selected contact's name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fdb = FirebaseFirestore.getInstance() // Firestore database instance
        val retrofit = RetrofitInstance.initialize() // Retrofit instance for API calls
        val sharedpref = SharedPrefFile // Shared preferences instance
        sharedpref.init(this) // Initialize shared preferences
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view using binding

        val editTexts = listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4, binding.pin5)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                private var lastText = ""

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    lastText = s.toString()  // Store previous text
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != lastText) {  // Only act if text actually changed
                        if (s?.length == 1 && i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()  // Move forward when typing
                        } else if (s.isNullOrEmpty() && i > 0) {
                            editTexts[i - 1].requestFocus()  // Move back when deleting
                            editTexts[i - 1].setText("")  // Also clear previous box
                        }
                    }
                }
            })

            // Prevent focus jumping unexpectedly
            editTexts[i].setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editTexts[0].text.isEmpty()) {
                    editTexts[0].requestFocus()
                }
            }
        }


        binding.phoneNumber.apply {
            setText("+91 ") // Set default prefix
            setSelection(text?.length ?: 0) // Move cursor to the end

            addTextChangedListener(object : TextWatcher {
                private var isEditing = false // Flag to prevent infinite loops

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isEditing || s == null) return
                    isEditing = true

                    // Ensure "+91 " prefix is always there
                    if (!s.startsWith("+91 ")) {
                        setText("+91  ")
                    }

                    // Prevent cursor from moving inside "+91 "
                    if (selectionStart < 4) {
                        setSelection(text?.length ?: 0)
                    }

                    // Extract actual phone number (excluding "+91 ")
                    val phoneNumber = s.toString().replace("+91 ", "")

                    // Show error if phone number is not exactly 10 digits
                    binding.phoneNumberLayout.error = if (phoneNumber.length != 10) {
                        "Phone number should be 10 digits"
                    } else {
                        null // Remove error when valid
                    }

                    isEditing = false
                }
            })
        }

        // Redirects user to login activity if already registered
        binding.tVAlreadyReg.setOnClickListener {
            val intent = Intent(this, LoginUserActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initializes the contact picker launcher
        pickContactLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    handleContactResult(data) // Handle contact selection
                }
            }


        binding.apply {
            nextToNumberLL.setOnClickListener {
                nepLL.visibility = View.GONE
                numberLL.visibility = View.VISIBLE
                pinLL.visibility = View.GONE
            }

            nextToPinLL.setOnClickListener {
                nepLL.visibility = View.GONE
                numberLL.visibility = View.GONE
                pinLL.visibility = View.VISIBLE
            }

            backToNepLL.setOnClickListener {
                nepLL.visibility = View.VISIBLE
                numberLL.visibility = View.GONE
                pinLL.visibility = View.GONE
            }

            backToNumberLL.setOnClickListener {
                nepLL.visibility = View.GONE
                numberLL.visibility = View.VISIBLE
                pinLL.visibility = View.GONE
            }
        }

        // Opens the contact picker when selecting a trusted contact
        binding.trustedContactNumber.setOnClickListener {
            pickContact()
        }

        // Handles user registration process when "Register" button is clicked
        binding.RegisterBtn.setOnClickListener {
            val fullname = binding.tilName.editText?.text.toString()
            val phoneNumber = binding.phoneNumber.text.toString()
            val organization = binding.tilOrganization.editText?.text.toString()
            val email = binding.tilemail.editText?.text.toString()
            val password = binding.tilpassword.editText?.text.toString()
            val securityPIN = editTexts.joinToString("") { it.text.toString() }
//            val securityPIN = binding.tilSecurityPIN.editText?.text.toString()
            // Ensures all fields are filled before proceeding
            if (email.isBlank() || password.isBlank() || organization.isBlank() || fullname.isBlank() || phoneNumber.isBlank() || phoneNum.isBlank() || name.isBlank() || securityPIN.isBlank()) {
                Toast.makeText(this, "Enter all the details", Toast.LENGTH_SHORT).show()
            } else if (phoneNumber.length != 14) {
                Toast.makeText(this, "Phone Number should be of 10 digits", Toast.LENGTH_SHORT)
                    .show()
            } else if (securityPIN.length != 5) {
                Toast.makeText(this, "Security PIN should be of 5 digits", Toast.LENGTH_SHORT)
                    .show()
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.mainContent.visibility = View.GONE

                // Creating a new user object
                val newUser = UserModelItem(
                    organization = organization,
                    email = email,
                    password = password,
                    fullName = fullname,
                    userID = (100..1000).random().toString(),
                    organizationID = (100..1000).random().toString(),
                    phoneNumber = phoneNumber,
                    trustedContactID = (100..1000).random().toString(),
                    trustedContactNumber = Constants.normalizePhoneNumber(phoneNum).toString(),
                    trustedContactName = name,
                    securityPIN = securityPIN
                )

                Log.d("RegisterUser", "$newUser")

                val jsonBody = Gson().toJson(newUser)
                Log.d("${Constants.TAG} API_REQUEST", "JSON Body: $jsonBody")

                // Sending the user data to the backend API for registration
                retrofit.registerNewUser(newUser)
                    .enqueue(object : Callback<NewRegistrationResponse> {
                        override fun onResponse(
                            call: Call<NewRegistrationResponse>,
                            response: Response<NewRegistrationResponse>,
                        ) {
                            Log.d("${Constants.TAG} RegisterUser", "Response: ${response.body()}")



                            try {
                                if (response.isSuccessful) { // Successfully registered
                                    sharedpref.putLoggedInfo(Constants.SP_LOGGED_INFO, true)
                                    sharedpref.putUserData(Constants.SP_USERDATA, newUser)
                                    Log.d("${Constants.TAG} RegisterUser", "New User: ${newUser}")

                                    val fdbData = hashMapOf(
                                        "lat" to "0.0",
                                        "long" to "0.0",
                                        "connectionInfo" to "null",
                                        "batPer" to 0,
                                        "name" to "name",
                                        "phoneNumber" to "1111111"
                                    )

                                    // Save user location info in Firestore
                                    fdb.collection(Constants.FIRESTORE_COLLECTION).document(email)
                                        .set(fdbData)
                                        .addOnSuccessListener {
                                            Log.d("RegisterUser", "New Firebase entry: $fdbData")
                                        }
                                        .addOnFailureListener {
                                            Log.d(
                                                "RegisterUser",
                                                "Error in Firebase: ${it.message}"
                                            )
                                        }

                                    binding.progressBar.visibility = View.GONE
                                    binding.mainContent.visibility = View.VISIBLE
                                    Toast.makeText(baseContext, response.body()!!.message, Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(baseContext, MainActivity::class.java))
                                    finish()
                                } else {  // Handles 400, 404, 500
                                    val errorMessage = JSONObject(
                                        response.errorBody()?.string().toString()
                                    ).getString("message") ?: "Unknown error"
                                    Toast.makeText(baseContext, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    "${Constants.TAG} ProfileFragment",
                                    "Error processing response: ${e.message}"
                                )
                                Toast.makeText(
                                    baseContext,
                                    "Something went wrong!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<NewRegistrationResponse>, t: Throwable) {
                            Log.d("RegisterUser", "API Call Failed: ${t.message}")
                        }
                    })
            }
        }
    }

    private val READ_CONTACTS_PERMISSION_REQUEST = 1 // Permission request code for reading contacts

    /**
     * Initiates contact picking by checking permissions
     */
    private fun pickContact() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST
            )
        } else {
            launchContactPicker()
        }
    }

    /**
     * Handles permission request results for reading contacts
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot access contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Launches the contact picker
     */
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    /**
     * Handles the result of the contact picker
     */
    private fun handleContactResult(data: Intent?) {
        if (data != null) {
            val contactUri: Uri = data.data!!
            val cursor = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name =
                        it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                    val hasPhone =
                        it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use { phone ->
                            if (phone.moveToFirst()) {
                                phoneNum =
                                    phone.getString(phone.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                binding.trustedContactNumber.setText("$name - $phoneNum")
                            }
                        }
                    } else {
                        Toast.makeText(this, "Contact has no phone number", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

}
