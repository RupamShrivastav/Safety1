package com.example.safety.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.safety.R
import com.example.safety.activity.LoginUserActivity
import com.example.safety.api.ApiService
import com.example.safety.api.RetrofitInstance
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentProfileBinding
import com.example.safety.databinding.UpdateNameDialogBinding
import com.example.safety.databinding.UpdatePasswordDialogBinding
import com.example.safety.databinding.UpdatePhoneNumberDialogBinding
import com.example.safety.databinding.UpdateSecurityPinDialogBinding
import com.example.safety.models.APIResponseModel
import com.example.safety.models.UpdateNameRequest
import com.example.safety.models.UpdatePasswordRequest
import com.example.safety.models.UpdatePhoneNumberRequest
import com.example.safety.models.UpdateSecurityPinRequest
import com.example.safety.models.UpdateTrustedContactRequest
import com.example.safety.models.UserModelItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ProfileFragment
 * Displays and allows editing of user profile information.
 */
class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment" // Constant for logging
    private val READ_CONTACTS_PERMISSION_REQUEST = 1 // Permission request code

    private lateinit var binding: FragmentProfileBinding // View binding
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent> // Contact picker
    private lateinit var apiService: ApiService // Retrofit API service
    private lateinit var db: FirebaseFirestore // Firestore instance
    private lateinit var sharedPref: SharedPrefFile // Shared preferences
    private lateinit var currentUser: UserModelItem // Currently logged-in user

    private var phoneNum = "" // Selected contact phone number
    private var name = "" // Selected contact name

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false) // Inflate layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDependencies()  // Initialize dependencies
        loadUserData()  // Load and display user data
        setupContactPicker()  // Set up contact picker
        setupClickListeners() // Set up click listeners for buttons
    }

    // Initializes dependencies.
    private fun initDependencies() {
        apiService = RetrofitInstance.apiService // Initialize Retrofit API service.
        sharedPref = SharedPrefFile // Initialize SharedPref.
        sharedPref.init(requireContext())// Initialize shared preferences
        db = FirebaseFirestore.getInstance() // Initialize Firestore
    }

    // Loads and displays user data.
    private fun loadUserData() {
        currentUser = sharedPref.getUserData(Constants.SP_USERDATA) ?: run { //Elvis Operator used
            // Handle the case where user data is not found (e.g., not logged in).
            Log.e(TAG, "User data not found in SharedPreferences")
            // Redirect to login or show an error message.
            // For example:
            startActivity(Intent(requireContext(), LoginUserActivity::class.java))
            requireActivity().finish() // Finish current activity, prevent further action.
            return // Return to prevent further execution.
        }

        // Display user profile data.
        binding.profileName.text = currentUser.fullName
        binding.email.text = currentUser.email
        binding.trustedContactNameProfile.text = currentUser.trustedContactName

        Log.d(TAG, "User data loaded: $currentUser")
    }

    // Sets up the contact picker launcher.
    private fun setupContactPicker() {
        pickContactLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleContactResult(result.data) // Handle selected contact
                }
            }
    }

    // Sets up click listeners for various UI elements.
    private fun setupClickListeners() {
        binding.apply { // Scoped to avoid repeating 'binding.'
            updateName.setOnClickListener { showUpdateNameDialog(currentUser) } // Name update
            updateNumber.setOnClickListener { showUpdatePhoneNumberDialog(currentUser) } // Phone update
            updatePassword.setOnClickListener { showUpdatePasswordDialog(currentUser) } // Password update
            updatePin.setOnClickListener { showUpdatePinDialog(currentUser) } // PIN update
            guardNum.setOnClickListener { pickContact() } // Select trusted contact
            inviteContacts.setOnClickListener { sendInvite() } // Invite contacts
            signout.setOnClickListener { signOut() } // Sign out
        }
    }

    // Shows the dialog for updating the user's name.
    private fun showUpdateNameDialog(currentUser: UserModelItem) {
        val dialogBinding = UpdateNameDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.currentName.text = currentUser.fullName

        dialogBinding.updateNameBtn.setOnClickListener {
            val newName = dialogBinding.updatedFullName.editText?.text?.toString()?.trim() ?: ""

            if (newName.isBlank()) { // Check if name is not null, empty or just whitespace characters
                dialogBinding.updatedFullName.error = "Name cannot be empty" // Show error on TextInputLayout
                return@setOnClickListener // Prevent further action
            }
            dialogBinding.updatedFullName.error = null // Clear previous error

            updateUserName(newName, dialog) // Call the update function

        }

        dialogBinding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
    }


    // Function to make the API Call to update User Name.
    private fun updateUserName(newName: String, dialog: Dialog){

        apiService.updateUserFullName(
            updateNameModel = UpdateNameRequest(currentUser.email, newName)
        ).enqueue(object : Callback<APIResponseModel> {
            override fun onResponse(
                call: Call<APIResponseModel>,
                response: Response<APIResponseModel>,
            ) {
                if (response.isSuccessful) {
                    handleNameUpdateSuccess(response, newName) // Handle success
                    dialog.dismiss() //Dismiss on Success

                } else {
                    handleUpdateError(response) // Handle API errors
                }
            }

            override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                handleUpdateFailure(t) // Handle network failure
            }
        })
    }

    // Handles a successful name update.
    private fun handleNameUpdateSuccess(response: Response<APIResponseModel>, newName: String) {
        Toast.makeText(requireContext(), response.body()?.message ?: "Name updated", Toast.LENGTH_SHORT).show() // Toast the message from response
        sharedPref.putUserData(
            Constants.SP_USERDATA,
            currentUser.copy(fullName = newName)
        ) // Update SharedPreferences
        currentUser = currentUser.copy(fullName = newName) // Update the local currentUser object
        binding.profileName.text = newName // Update the name on the profile screen immediately

        // Update name in Firestore
        val data = hashMapOf<String, Any>("name" to newName) // Only update the name field
        db.collection(Constants.FIRESTORE_COLLECTION)
            .document(currentUser.email)
            .set(data, SetOptions.merge()) // Merge to avoid overwriting other fields
            .addOnSuccessListener { Log.d(TAG, "Firestore name updated successfully") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore name update failed", e) }
    }


    // Shows the dialog for updating the user's phone number.
    private fun showUpdatePhoneNumberDialog(currentUser: UserModelItem) {
        val dialogBinding = UpdatePhoneNumberDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        dialogBinding.currentPhoneNumber.text = currentUser.phoneNumber

        dialogBinding.updatePhoneNumberBtn.setOnClickListener {
            val newNumber = dialogBinding.updatedPhoneNumber.editText?.text?.toString()?.trim() ?: ""
            if (newNumber.isBlank() ) { // check for valid input
                dialogBinding.updatedPhoneNumber.error = "Phone number cannot be empty"
                return@setOnClickListener
            }
            dialogBinding.updatedPhoneNumber.error = null
            updateUserPhoneNumber(newNumber, dialog) // Update Phone Number.
        }

        dialogBinding.cancelPhoneNumberBtn.setOnClickListener {
            dialog.dismiss() // Dismiss dialog on cancel
        }
    }

    // Function to make the API call to update Phone Number
    private fun updateUserPhoneNumber(newNumber: String, dialog: Dialog){

        apiService.updateUserPhoneNo(
            updatePhoneNumberRequest = UpdatePhoneNumberRequest(currentUser.email, newNumber)
        ).enqueue(object : Callback<APIResponseModel> {
            override fun onResponse(
                call: Call<APIResponseModel>,
                response: Response<APIResponseModel>,
            ) {
                if (response.isSuccessful) { // Handle successful response
                    handlePhoneNumberUpdateSuccess(response,newNumber)
                    dialog.dismiss()

                } else {
                    handleUpdateError(response) // Handle API errors
                }
            }

            override fun onFailure(call: Call<APIResponseModel>, t: Throwable) { // Handle failure
                handleUpdateFailure(t)
            }
        })
    }


    // Handle Successful Phone Number Update
    private fun handlePhoneNumberUpdateSuccess(response: Response<APIResponseModel>, newNumber: String){
        Toast.makeText(requireContext(), response.body()?.message ?: "Phone number updated", Toast.LENGTH_SHORT).show()

        sharedPref.putUserData(
            Constants.SP_USERDATA,
            currentUser.copy(phoneNumber = newNumber)
        )  // Update SharedPreferences

        currentUser = currentUser.copy(phoneNumber = newNumber) // Update the local currentUser

        // Update number in Firestore
        val data = hashMapOf<String, Any>("phoneNumber" to newNumber)
        db.collection(Constants.FIRESTORE_COLLECTION)
            .document(currentUser.email)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Firestore phone number updated successfully") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore phone number update failed", e) }
    }

    // Shows the dialog for updating the user's password.
    private fun showUpdatePasswordDialog(currentUser: UserModelItem) {
        val dialogBinding = UpdatePasswordDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        dialogBinding.updatePasswordBtn.setOnClickListener {
            val oldPassword = dialogBinding.oldPassword.editText?.text?.toString()?.trim() ?: ""
            val newPassword = dialogBinding.newPassword.editText?.text?.toString()?.trim() ?: ""

            if (oldPassword.isBlank() || newPassword.isBlank()) {
                if(oldPassword.isBlank()){
                    dialogBinding.oldPassword.error = "Old password cannot be empty" // Set error
                }
                if (newPassword.isBlank()){
                    dialogBinding.newPassword.error = "New password cannot be empty"
                }
                return@setOnClickListener // Exit if fields are empty
            }
            dialogBinding.oldPassword.error = null // Clear the errors
            dialogBinding.newPassword.error = null
            updateUserPassword(oldPassword, newPassword, dialog) // Update password
        }

        dialogBinding.cancelPasswordBtn.setOnClickListener {
            dialog.dismiss() // Dismiss dialog on cancel
        }
    }

    private fun updateUserPassword(oldPassword: String, newPassword: String, dialog: Dialog){
        apiService.updateUserPassword(
            updatePasswordRequest = UpdatePasswordRequest(
                email = currentUser.email,
                oldPassword = oldPassword,
                newPassword = newPassword
            )
        ).enqueue(object : Callback<APIResponseModel> {
            override fun onResponse(
                call: Call<APIResponseModel>,
                response: Response<APIResponseModel>,
            ) {
                if (response.isSuccessful) {  //  Handles 200-299
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Password Updated Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                } else {  // Handles 400, 404, 500
                    handleUpdateError(response) // Handle API errors
                }
            }

            override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                handleUpdateFailure(t)
            }
        })
    }

    // Shows the dialog for updating the user's security PIN.
    private fun showUpdatePinDialog(currentUser: UserModelItem) {
        val binding = UpdateSecurityPinDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        val pinSpinner = arrayOf("Using Old Security PIN", "Using Password") // Options for PIN update
        binding.dropDownSpinner.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item,
            pinSpinner
        )

        binding.dropDownSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long,
                ) {
                    when (position) {
                        0 -> { // Using old PIN
                            binding.oldPassword.isVisible = false
                            binding.oldPIN.isVisible = true
                        }
                        1 -> { // Using password
                            binding.oldPassword.isVisible = true
                            binding.oldPIN.isVisible = false
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // You can Optionally handle a case where the user does not make any selection.
                }
            }

        binding.updatePINBtn.setOnClickListener {
            val newPIN = binding.newPIN.editText?.text?.toString()?.trim() ?: ""
            val oldPIN = binding.oldPIN.editText?.text?.toString()?.trim()?.ifEmpty { null }  // Use ifEmpty for conciseness
            val password = binding.oldPassword.editText?.text?.toString()?.trim()?.ifEmpty { null }

            if (newPIN.isBlank()) {
                binding.newPIN.error = "New PIN cannot be empty"
                return@setOnClickListener
            }
            binding.newPIN.error = null

            if (oldPIN == null && password == null) { // If PIN is not updated by Old PIN then password must be not null.
                Toast.makeText(requireContext(), "Enter Old PIN or Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateUserPIN(newPIN, oldPIN, password, dialog) // Update security PIN
        }

        binding.cancelPINBtn.setOnClickListener {
            dialog.dismiss() // Dismiss dialog on cancel
        }

    }

    private fun updateUserPIN(newPIN: String, oldPIN: String?, password: String?, dialog: Dialog){
        apiService.updateSecurityPin(
            updateSecurityPinRequest = UpdateSecurityPinRequest(
                email = currentUser.email,
                newPIN,
                oldPIN,
                password
            )
        ).enqueue(object : Callback<APIResponseModel> {
            override fun onResponse(
                call: Call<APIResponseModel>,
                response: Response<APIResponseModel>,
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "PIN Updated", // Use a default message
                        Toast.LENGTH_SHORT
                    ).show()
                    sharedPref.putUserData(
                        Constants.SP_USERDATA,
                        currentUser.copy(securityPIN = newPIN) // Update only PIN
                    )
                    dialog.dismiss()
                } else {  // Handles 400, 404, 500
                    handleUpdateError(response)
                }
            }
            override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                handleUpdateFailure(t)
            }
        })
    }

    // Common function to handle update failures
    private fun handleUpdateFailure(t: Throwable) {
        Log.e(TAG, "Update failed: ${t.message}", t)
        Toast.makeText(requireContext(), "Update failed. Check your connection.", Toast.LENGTH_SHORT).show()
    }

    // Common function to handle update errors
    private fun handleUpdateError(response: Response<APIResponseModel>) {
        val errorMessage = try {
            JSONObject(response.errorBody()?.string() ?: "").getString("message")
        } catch (e: Exception) {
            "Unknown error"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Update error: $errorMessage")
    }

    // Sends an invitation message with an app download link.
    private fun sendInvite() {
        val inviteMessage =
            "I invite you to Safety\nDownload the app from the link https://github.com/RupamShrivastav/Safety1/releases/download/beta_v2/app-debug.apk\nAdd Organization - ${
                currentUser.organization
            }"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    // Requests permission and launches contact picker if granted.
    private fun pickContact() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_PERMISSION_REQUEST
            )
        } else {
            launchContactPicker()
        }
    }

    // Handles permission request results.
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
                    requireContext(),
                    "Permission denied. Cannot access contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Launches the contact picker.
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    // Handles selected contact and updates the trusted contact.
    private fun handleContactResult(data: Intent?) {
        val contactUri: Uri? = data?.data
        if (contactUri != null) { // Check for null outside the use block
            val cursor = requireActivity().contentResolver.query(contactUri, null, null, null, null) // Use requireActivity()
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                    val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone > 0) { // Check if contact has a phone number
                        queryContactPhoneNumber(id) // Query the contact phone number
                    } else {
                        Toast.makeText(requireContext(), "Contact has no phone number", Toast.LENGTH_SHORT).show() // Show error
                    }
                }
            }
        }
    }

    // Queries for contact phone number using the contact ID
    private fun queryContactPhoneNumber(contactId: String){
        val phoneCursor = requireActivity().contentResolver.query( // Use requireActivity()
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use { phone ->
            if (phone.moveToFirst()) {
                phoneNum = phone.getString(phone.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                updateTrustedContact()// Update trusted contact
            }
        }
    }

    // Function to update Trusted Contact on API call
    private fun updateTrustedContact(){
        apiService.updateTrustedContact(
            UpdateTrustedContactRequest(
                email = currentUser.email,
                trustedContactName = name,
                trustedContactNumber = phoneNum
            )
        ).enqueue(object : Callback<APIResponseModel> {
            override fun onResponse(
                call: Call<APIResponseModel>,
                response: Response<APIResponseModel>,
            ) {
                if (response.isSuccessful) {
                    handleTrustedContactUpdateSuccess(response) // Handle success
                } else {
                    handleUpdateError(response)  // Handle API error
                }
            }

            override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                handleUpdateFailure(t) // Handle network failure
            }
        })
    }

    // Function to handle the successful trusted contact update.
    private fun handleTrustedContactUpdateSuccess(response: Response<APIResponseModel>){
        Toast.makeText(requireContext(), response.body()?.message ?: "Trusted Contact Updated", Toast.LENGTH_SHORT).show()

        // Update local user data with new trusted contact info
        currentUser = currentUser.copy(
            trustedContactName = name,
            trustedContactNumber = Constants.normalizePhoneNumber(phoneNum) ?: ""
        )
        sharedPref.putUserData(Constants.SP_USERDATA, currentUser) // Update shared preferences

        // Update the UI to reflect the changes
        binding.trustedContactNameProfile.text = name

        // Update Firestore document
        val updatedData = hashMapOf(
            "trustedContactName" to name,
            "trustedContactNumber" to (Constants.normalizePhoneNumber(phoneNum) ?: "")
        )

        db.collection(Constants.FIRESTORE_COLLECTION)
            .document(currentUser.email)
            .set(updatedData, SetOptions.merge()) // Merge with existing data
            .addOnSuccessListener {
                Log.d(TAG, "Trusted contact updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating trusted contact in Firestore", e)
            }
    }

    // Signs out the user and clears data.
    private fun signOut() {
        Log.d(TAG, "Before Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}") // Log before sign-out

        sharedPref.putLoggedInfo(Constants.SP_LOGGED_INFO, false) // Clear login status
        sharedPref.clearAllData() // Clear all shared preferences data

        Log.d(TAG, "After Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}") // Log after sign-out

        // Navigate to LoginActivity and clear activity stack.
        val intent = Intent(requireContext(), LoginUserActivity::class.java)
        Toast.makeText(requireContext(), "Signed Out", Toast.LENGTH_SHORT).show()
        startActivity(intent)
        activity?.finishAffinity() // Finish all activities in the stack
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
