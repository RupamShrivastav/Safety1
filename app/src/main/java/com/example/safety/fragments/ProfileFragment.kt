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

class ProfileFragment : Fragment() {


    private val READ_CONTACTS_PERMISSION_REQUEST = 1
    lateinit var binding: FragmentProfileBinding
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent>
    var phoneNum = ""
    var name=""
    val sharedPref = SharedPrefFile
    lateinit var currentUser:UserModelItem

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


        sharedPref.init(requireContext())

        currentUser = sharedPref.getUserData(Constants.SP_USERDATA)!!
        binding.profileName.text = currentUser.fullName
        binding.trustedContactNameProfile.text = currentUser.trustedContactName
        Log.d("@@@@@@@","name ${currentUser.trustedContactName}")
        Log.d("@@@@@@@","name ${currentUser}")

        pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleContactResult(data)
            }
        }

        binding.inviteContacts.setOnClickListener {
            sendInvite()
        }

        binding.guardNum.setOnClickListener {
            pickContact()
            Log.d("@@@@@@ Update Trusted Contacts ","Updated $name $phoneNum")
        }

        binding.signout.setOnClickListener {
            Log.d(Constants.TAG,"Before Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}")
            sharedPref.putLoggedInfo(Constants.SP_LOGGED_INFO, false)
            Log.d(Constants.TAG,"Signed Out !!!")
            sharedPref.clearAllData()
            Log.d(Constants.TAG,"After Signout ${sharedPref.getUserData(Constants.SP_USERDATA)}")
            val intent = Intent(requireContext(), LoginUserActivity::class.java)
            startActivity(intent)
            activity?.finishAffinity()
        }

    }

    fun sendInvite(){
        val sharedPref = SharedPrefFile
        sharedPref.init(requireContext())
        val inviteMessage = "I invite you to Safety\nDownload the app from the link https://github.com/Jayamshriv/Safety_Project/releases/download/testing/app-release.apk\nAdd Organization - ${sharedPref.getUserData(
            Constants.SP_USERDATA)!!.organization}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, inviteMessage)
        }
        context?.startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun pickContact() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_REQUEST)
        } else {
            // Permission already granted, proceed with contact picking
            launchContactPicker()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with contact picking
                launchContactPicker()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(requireContext(), "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchContactPicker(){
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(intent)
    }

    private fun handleContactResult(data: Intent?){
        if (data != null) {
            val contactUri: Uri = data.data!!
            val cursor = activity?.contentResolver!!.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    Log.d("1 @@@@@@ handle ","Updated $name $phoneNum")
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
                                Log.d("2 @@@@@@ handle ","Updated $name $phoneNum")
                                phoneNum = phone.getString(phone.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                Log.d("3 @@@@@@ handle ","Updated $name $phoneNum")
                                val retrofit = RetrofitInstance.initialize()
                                retrofit.updateTrustedContact(
                                    currentUser.email,
                                    UpdateTrustedContactInfo(trustedContactName = name, trustedContactNumber = phoneNum)
                                ).enqueue(object :Callback<APIResponseModel>{
                                        override fun onResponse(
                                            call: Call<APIResponseModel>,
                                            response: Response<APIResponseModel>,
                                        ) {
                                            Log.d("@@@@@ update","${response}")
                                            Log.d("@@@@@@ Update Trusted Contacts ","Updated $name $phoneNum")
                                            sharedPref.putUserData(Constants.SP_USERDATA,currentUser.copy(trustedContactName = name, trustedContactNumber = normalizePhoneNumber(phoneNum)))
                                            binding.trustedContactNameProfile.text = name
                                        }

                                        override fun onFailure(call: Call<APIResponseModel>, t: Throwable) {
                                            Log.d("@@@@@@@@ Error Trusted Contacts ","Update : ${t.message}")
                                        }
                                    })

                            }
                            Log.d("4 @@@@@@ handle ","Updated $name $phoneNum")
                        }
                    } else {
                        Toast.makeText(requireContext(), "Contact has no phone number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun normalizePhoneNumber(number: String): String{
        var phoneNumber = number

        phoneNumber = phoneNumber.replace(" ", "")
        if(phoneNumber.length>10){
            Log.d("@@@@@ 1","$phoneNumber ${phoneNumber.length}")
            if(phoneNumber[0]=='+'){
                Log.d("@@@@@@ 2","${phoneNumber}")
                phoneNumber = phoneNumber.substring(3)
            }
        }
        Log.d("@@@@@@ 3","${phoneNumber}")
        return phoneNumber.toString()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()

    }
}
