package com.example.safety.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safety.common.Constants
import com.example.safety.activity.LoginUserActivity
import com.example.safety.common.SharedPrefFile
import com.example.safety.api.RetrofitInstance
import com.example.safety.databinding.FragmentProfileBinding
import com.example.safety.models.APIResponseModel
import com.example.safety.models.UpdateTrustedContactInfo
import com.example.safety.models.UserModelItem
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
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize shared preferences and retrieve user data
        sharedPref.init(requireContext())
        currentUser = sharedPref.getUserData(Constants.SP_USERDATA)!!

        // Display user profile data
        binding.profileName.text = currentUser.fullName
        binding.trustedContactNameProfile.text = currentUser.trustedContactName

        Log.d("@@@@@@@", "name ${currentUser.trustedContactName}")
        Log.d("@@@@@@@", "User data: ${currentUser}")

        // Register contact picker activity
        pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleContactResult(data) // Handle selected contact
            }
        }

        // Click listener for inviting contacts
        binding.inviteContacts.setOnClickListener {
            sendInvite()
        }

        // Click listener for selecting a trusted contact
        binding.guardNum.setOnClickListener {
            pickContact()
            Log.d("@@@@@@ Update Trusted Contacts ", "Updated $name $phoneNum")
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
            startActivity(intent)
            activity?.finishAffinity()
        }
    }

    // Sends an invitation message with an app download link
    fun sendInvite() {
        sharedPref.init(requireContext())

        val inviteMessage = "I invite you to Safety\nDownload the app from the link https://github.com/Jayamshriv/Safety_Project/releases/download/testing/app-release.apk\nAdd Organization - ${sharedPref.getUserData(Constants.SP_USERDATA)!!.organization}"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
        }
        context?.startActivity(Intent.createChooser(intent, "Share via"))
    }

    // Requests permission and launches contact picker if granted
    private fun pickContact() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_REQUEST)
        } else {
            launchContactPicker() // Launch contact picker directly if permission granted
        }
    }

    // Handles permission request results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchContactPicker() // Launch contact picker if permission granted
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
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
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    Log.d("1 @@@@@@ handle ", "Updated $name $phoneNum")

                    val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
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
                                phoneNum = phone.getString(phone.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                                // API call to update trusted contact
                                val retrofit = RetrofitInstance.initialize()
                                retrofit.updateTrustedContact(
                                    currentUser.email,
                                    UpdateTrustedContactInfo(trustedContactName = name, trustedContactNumber = phoneNum)
                                ).enqueue(object : Callback<APIResponseModel> {
                                    override fun onResponse(call: Call<APIResponseModel>, response: Response<APIResponseModel>) {
                                        Log.d("@@@@@ update", "Response: ${response}")
                                        Log.d("@@@@@@ Update Trusted Contacts ", "Updated $name $phoneNum")

                                        // Save updated contact in shared preferences
                                        sharedPref.putUserData(Constants.SP_USERDATA, currentUser.copy(trustedContactName = name, trustedContactNumber = normalizePhoneNumber(phoneNum)))
                                        binding.trustedContactNameProfile.text = name
                                    }

                                    override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                                        Log.d("@@@@@@@@ Error Trusted Contacts ", "Update failed: ${t.message}")
                                    }
                                })
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Contact has no phone number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Normalizes phone numbers by removing spaces and country code if present
    private fun normalizePhoneNumber(number: String): String {
        var phoneNumber = number.replace(" ", "")

        if (phoneNumber.length > 10 && phoneNumber[0] == '+') {
            phoneNumber = phoneNumber.substring(3) // Remove country code
        }

        Log.d("@@@@@@ Normalized Number", phoneNumber)
        return phoneNumber
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
