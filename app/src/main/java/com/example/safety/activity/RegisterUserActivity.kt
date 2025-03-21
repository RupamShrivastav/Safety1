package com.example.safety.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safety.R
import com.example.safety.api.ApiService
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
 * Activity for user registration.
 */
class RegisterUserActivity : AppCompatActivity() {
    private val TAG = "Register User" // Logging tag
    private lateinit var binding: ActivityRegisterUserBinding // View binding
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent> // Contact picker launcher
    private var phoneNum = "" // Selected contact phone number
    private var name = "" // Selected contact name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fdb = FirebaseFirestore.getInstance() // Firestore instance
        val retrofit = RetrofitInstance.initialize() // Retrofit API instance
        val sharedpref = SharedPrefFile // SharedPref instance
        sharedpref.init(this)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPinEntryNavigation() // Configure PIN entry field navigation
        setupPhoneNumberFormatting() // Configure phone number input formatting
        setupAlreadyRegisteredNavigation() // Configure navigation to login for existing users
        setupContactPickerLauncher() // Initialize contact picker launcher
        setupUIActionListeners(fdb, retrofit, sharedpref) // Set up listeners for UI actions
    }

    // Configures navigation between PIN entry fields.
    fun setupPinEntryNavigation() {
        val editTexts = listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4, binding.pin5)

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                private var lastText = "" // Store previous text

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    lastText = s.toString()
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != lastText) {
                        when {
                            s?.length == 1 && i < editTexts.size - 1 -> editTexts[i + 1].requestFocus() // Move to next field
                            s.isNullOrEmpty() && i > 0 -> {
                                editTexts[i - 1].requestFocus() // Move to previous field
                                editTexts[i - 1].setText("") // Clear previous field
                            }
                        }
                    }
                }
            })

            editTexts[i].setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && editTexts[0].text.isEmpty()) {
                    editTexts[0].requestFocus() // Focus first field if empty
                }
            }
        }
    }

    // Sets up formatting and validation for phone number input.
    fun setupPhoneNumberFormatting() {
        binding.phoneNumber.apply {
            setText("+91 ") // Default prefix
            setSelection(text?.length ?: 0) // Cursor to end

            addTextChangedListener(object : TextWatcher {
                private var isEditing = false // Prevent recursion

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isEditing || s == null) return
                    isEditing = true

                    if (!s.startsWith("+91 ")) {
                        setText("+91  ") // Ensure prefix
                    }
                    if (selectionStart < 4) {
                        setSelection(text?.length ?: 0) // Prevent cursor in prefix
                    }

                    val phoneNumber = s.toString().replace("+91 ", "") // Extract number
                    binding.phoneNumberLayout.error = if (phoneNumber.length != 10) {
                        "Phone number should be 10 digits" // Error for invalid length
                    } else {
                        null // Clear error
                    }

                    isEditing = false
                }
            })
        }
    }

    // Configures navigation to LoginUserActivity for already registered users.
    private fun setupAlreadyRegisteredNavigation() {
        binding.tVAlreadyReg.setOnClickListener {
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish()
        }
    }

    // Initializes ActivityResultLauncher for contact picking.
    private fun setupContactPickerLauncher() {
        pickContactLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    handleContactResult(data) // Handle contact result
                }
            }
    }

    // Sets up listeners for UI elements like buttons and text fields.
    fun setupUIActionListeners(fdb: FirebaseFirestore, retrofit: ApiService, sharedpref: SharedPrefFile) {
        binding.apply {
            nextToNumberLL.setOnClickListener { navigateRegisterSteps(numberLL) } // Show number input
            nextToPinLL.setOnClickListener { navigateRegisterSteps(pinLL) } // Show PIN input
            backToNepLL.setOnClickListener { navigateRegisterSteps(nepLL) } // Back to name/email/password
            backToNumberLL.setOnClickListener { navigateRegisterSteps(numberLL) } // Back to number input
            trustedContactNumber.setOnClickListener { pickContact() } // Open contact picker
            RegisterBtn.setOnClickListener { handleRegistration(fdb, retrofit, sharedpref) } // Handle registration
        }
    }

    // Handles visibility changes for registration step layouts.
    fun navigateRegisterSteps(targetView: View) {
        binding.nepLL.visibility = View.GONE
        binding.numberLL.visibility = View.GONE
        binding.pinLL.visibility = View.GONE
        targetView.visibility = View.VISIBLE
    }

    private val READ_CONTACTS_PERMISSION_REQUEST = 1 // Permission request code

    // Initiates contact picking after permission check.
    fun pickContact() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_REQUEST)
        } else {
            launchContactPicker() // Launch if permission granted
        }
    }

    // Handles permission request result for contacts.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker() // Launch picker if permission granted
            } else {
                Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launches the contact picker intent.
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent) // Launch contact picker
    }

    // Handles result from contact picker intent.
    private fun handleContactResult(data: Intent?) {
        data?.data?.let { contactUri ->
            contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                    val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone > 0) {
                        queryContactPhoneNumber(id) // Query phone number if available
                    } else {
                        Toast.makeText(this, "Contact has no phone number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Queries and sets the phone number of the selected contact.
    private fun queryContactPhoneNumber(contactId: String) {
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )?.use { phoneCursor ->
            if (phoneCursor.moveToFirst()) {
                phoneNum = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                binding.trustedContactNumber.setText("$name - $phoneNum") // Set contact name and number
            }
        }
    }

    // Handles the user registration process.
    private fun handleRegistration(fdb: FirebaseFirestore, retrofit: ApiService, sharedpref: SharedPrefFile) {
        val fullname = binding.tilName.editText?.text.toString()
        val phoneNumber = binding.phoneNumber.text.toString()
        val organization = binding.tilOrganization.editText?.text.toString()
        val email = binding.tilemail.editText?.text.toString()
        val password = binding.tilpassword.editText?.text.toString()
        val securityPIN = listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4, binding.pin5).joinToString("") { it.text.toString() }

        if (!validateInputFields(fullname, phoneNumber, organization, email, password, securityPIN)) return // Validate input

        binding.progressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE


        val newUser = createUserModel(fullname, organization, email, password, phoneNumber, securityPIN) // Create UserModelItem
        registerUserWithApi(newUser, fdb, retrofit, sharedpref, email) // Register user via API
    }

    // Validates all registration input fields.
    fun validateInputFields(fullname: String, phoneNumber: String, organization: String, email: String, password: String, securityPIN: String): Boolean {
        when {
            email.isBlank() || password.isBlank() || organization.isBlank() || fullname.isBlank() || phoneNumber.isBlank() || phoneNum.isBlank() || name.isBlank() || securityPIN.isBlank() -> {
                Toast.makeText(this, "Enter all the details", Toast.LENGTH_SHORT).show()
                return false
            }
            phoneNumber.length != 14 -> {
                Toast.makeText(this, "Phone Number should be of 10 digits", Toast.LENGTH_SHORT).show()
                return false
            }
            securityPIN.length != 5 -> {
                Toast.makeText(this, "Security PIN should be of 5 digits", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    // Creates a UserModelItem for registration.
    private fun createUserModel(fullname: String, organization: String, email: String, password: String, phoneNumber: String, securityPIN: String): UserModelItem {
        return UserModelItem(
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
    }

    // Registers user with API and handles response.
    private fun registerUserWithApi(
        newUser: UserModelItem,
        fdb: FirebaseFirestore,
        retrofit: ApiService,
        sharedpref: SharedPrefFile,
        email: String
    ) {
        retrofit.registerNewUser(newUser).enqueue(object : Callback<NewRegistrationResponse> {
            override fun onResponse(call: Call<NewRegistrationResponse>, response: Response<NewRegistrationResponse>) {
                Log.d("${Constants.TAG} RegisterUser", "Response: ${response.body()}")
                handleRegistrationResponse(response, newUser, fdb, sharedpref, email) // Handle API response
            }

            override fun onFailure(call: Call<NewRegistrationResponse>, t: Throwable) {
                handleRegistrationFailure(t) // Handle API failure
            }
        })
    }

    // Handles successful and unsuccessful registration API responses.
    private fun handleRegistrationResponse(
        response: Response<NewRegistrationResponse>,
        newUser: UserModelItem,
        fdb: FirebaseFirestore,
        sharedpref: SharedPrefFile,
        email: String
    ) {
        binding.progressBar.visibility = View.GONE
        binding.mainContent.visibility = View.VISIBLE

        try {
            if (response.isSuccessful) {
                onRegistrationSuccess(response, newUser, fdb, sharedpref, email) // Handle success
            } else {
                onRegistrationError(response) // Handle error
            }
        } catch (e: Exception) {
            onRegistrationException(e) // Handle exception
        }
    }

    // Handles successful user registration actions.
    private fun onRegistrationSuccess(
        response: Response<NewRegistrationResponse>,
        newUser: UserModelItem,
        fdb: FirebaseFirestore,
        sharedpref: SharedPrefFile,
        email: String
    ) {
        sharedpref.putLoggedInfo(Constants.SP_LOGGED_INFO, true)
        sharedpref.putUserData(Constants.SP_USERDATA, newUser)

        saveUserLocationToFirestore(fdb, email) // Save user location to Firestore

        Toast.makeText(baseContext, response.body()!!.message, Toast.LENGTH_SHORT).show()
        startActivity(Intent(baseContext, MainActivity::class.java))
        finish()
    }


    // Saves default user location data to Firestore.
    private fun saveUserLocationToFirestore(fdb: FirebaseFirestore, email: String) {
        val fdbData = hashMapOf(
            "lat" to "0.0",
            "long" to "0.0",
            "connectionInfo" to "null",
            "batPer" to 0,
            "name" to "name",
            "phoneNumber" to "1111111"
        )

        fdb.collection(Constants.FIRESTORE_COLLECTION).document(email)
            .set(fdbData)
            .addOnSuccessListener { Log.d("RegisterUser", "Firebase entry created") }
            .addOnFailureListener { e -> Log.e("RegisterUser", "Firebase error: ${e.message}") }
    }


    // Handles registration error responses from API.
    private fun onRegistrationError(response: Response<NewRegistrationResponse>) {
        val errorMessage = JSONObject(response.errorBody()?.string().toString()).getString("message") ?: "Unknown error"
        Toast.makeText(baseContext, errorMessage, Toast.LENGTH_SHORT).show()
    }

    // Handles exceptions during registration response processing.
    private fun onRegistrationException(e: Exception) {
        Log.e("${Constants.TAG} ProfileFragment", "Response processing error: ${e.message}")
        Toast.makeText(baseContext, "Something went wrong!", Toast.LENGTH_SHORT).show()
    }

    // Handles API call failure during registration.
    private fun handleRegistrationFailure(t: Throwable) {
        Log.d("RegisterUser", "API Call Failed: ${t.message}")
    }
}
