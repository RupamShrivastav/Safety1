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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.safety.R
import com.example.safety.common.Constants
import com.example.safety.activity.LoginUserActivity
import com.example.safety.common.SharedPrefFile
import com.example.safety.api.RetrofitInstance
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ProfileFragment
 */
class ProfileFragment : Fragment() {

    // Request code for contacts permission
    private val READ_CONTACTS_PERMISSION_REQUEST = 1

    // View binding for accessing UI elements
    lateinit var binding: FragmentProfileBinding

    // Launcher for contact picker
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent>

    private val retrofit = RetrofitInstance.initialize()
    private lateinit var db: FirebaseFirestore

    // Variables to store selected contact details
    var phoneNum = ""
    var name = ""

    // Shared Preferences instance
    val sharedPref = SharedPrefFile
    lateinit var currentUser: UserModelItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences and retrieve user data
        sharedPref.init(requireContext())
        db = FirebaseFirestore.getInstance()
        currentUser = sharedPref.getUserData(Constants.SP_USERDATA)!!

        // Display user profile data
        binding.profileName.text = currentUser.fullName
        binding.email.text = currentUser.email
        binding.trustedContactNameProfile.text = currentUser.trustedContactName

        Log.d("${Constants.TAG} ProfileFragment", "name ${currentUser.trustedContactName}")
        Log.d("${Constants.TAG} ProfileFragment", "User data: ${currentUser}")

        // Register contact picker activity
        pickContactLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    handleContactResult(data) // Handle selected contact
                }
            }

        binding.updateName.setOnClickListener {
            Log.d("${Constants.TAG} ProfileFragment", " Update Name clicked")
            showUpdateNameDialog(currentUser)
        }

        binding.updateNumber.setOnClickListener {
            Log.d("${Constants.TAG} ProfileFragment", " Update Phone Number clicked")
            showUpdatePhoneNumberDialog(currentUser)
        }

        binding.updatePassword.setOnClickListener {
            Log.d("${Constants.TAG} ProfileFragment", " Update Password clicked")
            showUpdatePasswordDialog(currentUser)
        }

        binding.updatePin.setOnClickListener {
            Log.d("${Constants.TAG} ProfileFragment", " Update PIN clicked")
            showUpdatePinDialog(currentUser)
        }

        // Click listener for selecting a trusted contact
        binding.guardNum.setOnClickListener {
            pickContact()
            Log.d("${Constants.TAG} Update Trusted Contacts ", "Updated $name $phoneNum")
        }

        // Click listener for inviting contacts
        binding.inviteContacts.setOnClickListener {
            sendInvite()
        }

        // Click listener for signing out
        binding.signout.setOnClickListener {
            Log.d(Constants.TAG, "Before Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}")

            // Clear user session and redirect to login screen
            sharedPref.putLoggedInfo(Constants.SP_LOGGED_INFO, false)
            Log.d(Constants.TAG, "Signed Out !!!")
            sharedPref.clearAllData()
            Log.d(Constants.TAG, "After Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}")

            val intent = Intent(requireContext(), LoginUserActivity::class.java)
            Toast.makeText(requireContext(),"Signed Out",Toast.LENGTH_SHORT).show()
            startActivity(intent)
            activity?.finishAffinity()
        }

    }

    private fun showUpdateNameDialog(currentUser: UserModelItem) {
        val binding = UpdateNameDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }
        binding.currentName.text = currentUser.fullName

        Log.d("${Constants.TAG} ProfileFragment", "Name ${currentUser.fullName} ")

        binding.updateNameBtn.setOnClickListener {
            val newName = binding.updatedFullName.editText?.text.toString()
            Log.d("${Constants.TAG} ProfileFragment", "Name ${currentUser.fullName}  $newName")

            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT)
                    .show()
//                    return@setOnClickListener
            } else {
                retrofit.updateUserFullName(
                    updateNameModel = UpdateNameRequest(currentUser.email, newName)
                ).enqueue(object : Callback<APIResponseModel> {
                    override fun onResponse(
                        call: Call<APIResponseModel>,
                        response: Response<APIResponseModel>,
                    ) {

                        Log.d("${Constants.TAG} ProfileFragment", "${response}")

                        try {
                            if (response.isSuccessful) {  //  Handles 200-299
                                Toast.makeText(
                                    requireContext(),
                                    response.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPref.putUserData(
                                    Constants.SP_USERDATA,
                                    currentUser.copy(
                                        fullName = newName
                                    )
                                )
                                val data = hashMapOf<String, Any>()
                                data["name"] = newName

                                if (data.isNotEmpty()) {
                                    db.collection(Constants.FIRESTORE_COLLECTION)
                                        .document(currentUser.email)
                                        .set(data, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d(
                                                " ${Constants.TAG} FirestoreUpdate",
                                                "Successfully updated data"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "${Constants.TAG} FirestoreUpdate",
                                                "Failed to update data",
                                                e
                                            )
                                        }
                                }
                                dialog.dismiss()
                            } else {  // Handles 400, 404, 500
                                val errorMessage =
                                    JSONObject(response.errorBody()?.string().toString()).getString(
                                        "message"
                                    ) ?: "Unknown error"
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "${Constants.TAG} ProfileFragment",
                                "Error processing response: ${e.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                        Log.d("${Constants.TAG} ProfileFragment", "${t.message}")

                    }
                })
            }
        }

        binding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun showUpdatePhoneNumberDialog(currentUser: UserModelItem) {
        val binding = UpdatePhoneNumberDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }

        binding.currentPhoneNumber.text = currentUser.phoneNumber

        binding.updatePhoneNumberBtn.setOnClickListener {
            val new = binding.updatedPhoneNumber.editText?.text.toString()
            Log.d(
                "${Constants.TAG} ProfileFragment",
                "Phone Number ${currentUser.phoneNumber}  $new"
            )
            if (new.isBlank()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                retrofit.updateUserPhoneNo(
                    updatePhoneNumberRequest = UpdatePhoneNumberRequest(currentUser.email, new)
                ).enqueue(object : Callback<APIResponseModel> {
                    override fun onResponse(
                        call: Call<APIResponseModel>,
                        response: Response<APIResponseModel>,
                    ) {

                        Log.d("${Constants.TAG} ProfileFragment", "${response}")
                        try {
                            if (response.isSuccessful) {  //  Handles 200-299

                                Toast.makeText(
                                    requireContext(),
                                    response.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPref.putUserData(
                                    Constants.SP_USERDATA,
                                    currentUser.copy(
                                        phoneNumber = Constants.normalizePhoneNumber(new) ?: "0000"
                                    )
                                )
                                val data = hashMapOf<String, Any>()
                                data["phoneNumber"] = new

                                if (data.isNotEmpty()) {
                                    db.collection(Constants.FIRESTORE_COLLECTION)
                                        .document(currentUser.email)
                                        .set(data, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d(
                                                "${Constants.TAG} FirestoreUpdate",
                                                "Successfully updated data"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "${Constants.TAG} FirestoreUpdate",
                                                "Failed to update data",
                                                e
                                            )
                                        }
                                }
                                dialog.dismiss()

                            } else {  // Handles 400, 404, 500
                                val errorMessage =
                                    JSONObject(response.errorBody()?.string().toString()).getString(
                                        "message"
                                    ) ?: "Unknown error"
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "${Constants.TAG} ProfileFragment",
                                "Error processing response: ${e.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                        Log.d("${Constants.TAG} ProfileFragment", "${t.message}")

                    }
                })
            }
        }

        binding.cancelPhoneNumberBtn.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun showUpdatePasswordDialog(currentUser: UserModelItem) {
        val binding = UpdatePasswordDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.95).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            show()
        }


        binding.updatePasswordBtn.setOnClickListener {
            val old = binding.oldPassword.editText?.text.toString()
            val new = binding.newPassword.editText?.text.toString()
            Log.d(
                "${Constants.TAG} ProfileFragment",
                "Password ${currentUser.phoneNumber}  $new"
            )
            if (new.isBlank() || old.isBlank()) {
                Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                retrofit.updateUserPassword(
                    updatePasswordRequest = UpdatePasswordRequest(
                        email = currentUser.email,
                        oldPassword = old,
                        newPassword = new
                    )
                ).enqueue(object : Callback<APIResponseModel> {
                    override fun onResponse(
                        call: Call<APIResponseModel>,
                        response: Response<APIResponseModel>,
                    ) {

                        Log.d("${Constants.TAG} ProfileFragment", "$response")

                        try {
                            if (response.isSuccessful) {  //  Handles 200-299
                                Toast.makeText(
                                    requireContext(),
                                    response.body()!!.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPref.putUserData(
                                    Constants.SP_USERDATA,
                                    currentUser.copy(
                                        phoneNumber = Constants.normalizePhoneNumber(new) ?: "0000"
                                    )
                                )
                                dialog.dismiss()
                            } else {  // Handles 400, 404, 500
                                val errorMessage =
                                    JSONObject(response.errorBody()?.string().toString()).getString(
                                        "message"
                                    ) ?: "Unknown error"
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "${Constants.TAG} ProfileFragment",
                                "Error processing response: ${e.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                        Log.d("${Constants.TAG} ProfileFragment", "${t.message}")
                    }
                })
            }
        }

        binding.cancelPasswordBtn.setOnClickListener {
            dialog.dismiss()
        }

    }

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

        val pinSpinner = arrayOf("Using Old Security PIN", "Using Password")
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
                        0 -> {
                            binding.oldPassword.isVisible = false
                            binding.oldPIN.isVisible = true
                            Log.d(
                                "${Constants.TAG} ProfileFragment",
                                "PIN ${currentUser.securityPIN}  ${parent.getItemAtPosition(position)} "
                            )
                        }

                        1 -> {
                            binding.oldPassword.isVisible = true
                            binding.oldPIN.isVisible = false
                            Log.d(
                                "${Constants.TAG} ProfileFragment",
                                "PIN ${currentUser.securityPIN}  ${parent.getItemAtPosition(position)} "
                            )
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Toast.makeText(requireContext(), "Nothing Selected", Toast.LENGTH_SHORT).show()

                }
            }

        binding.updatePINBtn.setOnClickListener {
            val newPIN = binding.newPIN.editText?.text.toString()
            val oldPIN = binding.oldPIN.editText?.text.toString().ifEmpty { null }
            val password = binding.oldPassword.editText?.text.toString().ifEmpty { null }
            Log.d(
                "${Constants.TAG} ProfileFragment",
                "PIN ${currentUser.securityPIN}  $oldPIN $newPIN $password "
            )
            if (newPIN.isBlank() || (oldPIN.isNullOrBlank() && password.isNullOrBlank())) {
                Toast.makeText(requireContext(), "New PIN cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                retrofit.updateSecurityPin(
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

                        Log.d("${Constants.TAG} ProfileFragment", "${response}")

                        try {
                            if (response.isSuccessful) {  //  Handles 200-299
                                Toast.makeText(
                                    requireContext(),
                                    response.body()?.message ?: "Success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPref.putUserData(
                                    Constants.SP_USERDATA,
                                    currentUser.copy(securityPIN = newPIN ?: "0000")
                                )
                                dialog.dismiss()
                            } else {  // Handles 400, 404, 500
                                val errorMessage =
                                    JSONObject(response.errorBody()?.string().toString()).getString(
                                        "message"
                                    ) ?: "Unknown error"
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "${Constants.TAG} ProfileFragment",
                                "Error processing response: ${e.message}"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                        Log.d("${Constants.TAG} ProfileFragment", "${t.message}")

                    }
                })
            }
        }

        binding.cancelPINBtn.setOnClickListener {
            dialog.dismiss()
        }

    }

    // Sends an invitation message with an app download link
    private fun sendInvite() {
        sharedPref.init(requireContext())

        val inviteMessage =
            "I invite you to Safety\nDownload the app from the link https://github.com/RupamShrivastav/Safety1/releases/download/beta_v2/app-debug.apk\nAdd Organization - ${
                sharedPref.getUserData(Constants.SP_USERDATA)!!.organization
            }"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
        }
        context?.startActivity(Intent.createChooser(intent, "Share via"))
    }

    // Requests permission and launches contact picker if granted
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
            launchContactPicker() // Launch contact picker directly if permission granted
        }
    }

    // Handles permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker() // Launch contact picker if permission granted
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission denied. Cannot access contacts.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Launches the contact picker
    private fun launchContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    // Handles selected contact and updates the trusted contact
    private fun handleContactResult(data: Intent?) {
        if (data != null) {
            val contactUri: Uri = data.data!!
            val cursor = activity?.contentResolver!!.query(contactUri, null, null, null, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name =
                        it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    Log.d("1 @@@@@@ handle ", "Updated $name $phoneNum")

                    val hasPhone =
                        it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone > 0) {
                        val phoneCursor = activity?.contentResolver!!.query(
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

                                // API call to update trusted contact

                                retrofit.updateTrustedContact(
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
                                        Log.d("@@@@@ update", "Response: ${response}")
                                        Log.d(
                                            "@@@@@@ Update Trusted Contacts ",
                                            "Updated $name $phoneNum"
                                        )

                                        // Save updated contact in shared preferences
                                        Log.d(
                                            "${Constants.TAG} Profile",
                                            sharedPref.getUserData(Constants.SP_USERDATA).toString()
                                        )
                                        sharedPref.putUserData(
                                            Constants.SP_USERDATA,
                                            currentUser.copy(
                                                trustedContactName = name,
                                                trustedContactNumber = Constants.normalizePhoneNumber(
                                                    phoneNum
                                                ) ?: "00000"
                                            )
                                        )
                                        binding.trustedContactNameProfile.text = name
                                    }

                                    override fun onFailure(
                                        call: Call<APIResponseModel>,
                                        t: Throwable,
                                    ) {
                                        Log.d(
                                            "${Constants.TAG} ProfileFragment Error Trusted Contacts ",
                                            "Update failed: ${t.message}"
                                        )
                                    }
                                })
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Contact has no phone number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
