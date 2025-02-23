package com.example.safety

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safety.api.RetrofitInstance
import com.example.safety.databinding.ActivityRegisterUserBinding
import com.example.safety.models.NewUserCreatedResponse
import com.example.safety.models.UserModelItem
import com.example.safety.ui.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterUserActivity : AppCompatActivity() {

    private val TAG = "Register User"
    private lateinit var binding: ActivityRegisterUserBinding
    private lateinit var pickContactLauncher: ActivityResultLauncher<Intent>
    var phoneNum = ""
    var name=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fdb = FirebaseFirestore.getInstance()

        val retrofit = RetrofitInstance.initialize()
        val sharedpref = SharedPrefFile
        sharedpref.init(this)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.tVAlreadyReg.setOnClickListener {
            val intent = Intent(this, LoginUserActivity::class.java)
            startActivity(intent)
            finish()
        }

        pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                handleContactResult(data)
            }
        }

        binding.apply {
            nextBtn.setOnClickListener {
                nepLL.visibility = View.GONE
                numberLL.visibility = View.VISIBLE
            }
        }

        binding.trustedContactNumber.setOnClickListener {
            pickContact()
        }

        binding.RegisterBtn.setOnClickListener {
            val fullname = binding.tilName.editText?.text.toString()
            val phoneNumber = binding.phoneNumber.editText?.text.toString()
            val organization = binding.tilOrganization.editText?.text.toString()
            val email = binding.tilemail.editText?.text.toString()
            val password = binding.tilpassword.editText?.text.toString()

            if(email.isBlank() || password.isBlank() || organization.isBlank() || fullname.isBlank()){
                Toast.makeText(this,"Enter all the details",Toast.LENGTH_SHORT).show()
            }else {

                binding.progressBar.visibility = View.VISIBLE
                binding.mainContent.visibility = View.GONE

                binding.progressBar.postDelayed({
                    binding.progressBar.visibility = View.GONE
                    binding.mainContent.visibility = View.VISIBLE
                }, 1500)

                val newUser = UserModelItem(
                    organization = organization,
                    email = email,
                    password = password,
                    fullName = fullname,
                    personID = (100..1000).random().toString(),
                    organizationID = (100..1000).random().toString(),
                    phoneNumber = phoneNumber,
                    trustedContactID = (100..1000).random().toString(),
                    trustedContactNumber = phoneNum,
                    trustedContactName = name
                )
                retrofit.registerNewUser(newUser)
                    .enqueue(object : Callback<NewUserCreatedResponse>{
                        override fun onResponse(call: Call<NewUserCreatedResponse>, response: Response<NewUserCreatedResponse>) {
                            Log.d("Rupam $TAG", "Response: ${response.body()}")
                            sharedpref.putLoggedInfo(Constants.SP_LOGGED_INFO,true)
                            sharedpref.putUserData(Constants.SP_USERDATA,newUser)
                            Log.d(Constants.TAG," Register User Data : ${sharedpref.getUserData(Constants.SP_USERDATA).toString()}")
                            val fdbData = hashMapOf(
                                "name" to fullname,
                                "batteryPer" to "100",
                                "connectionInfo" to "Wifi",
                                "lat" to "32.232232",
                                "long" to "32.232232",
                                "phoneNumber" to "12111",
                            )

                            fdb.collection(Constants.FIRESTORE_COLLECTION).document(email)
                                .set(fdbData)
                                .addOnSuccessListener {
                                    Log.d("Rupam Register","New firebase entry :$fdbData")
                                }
                                .addOnFailureListener {
                                    Log.d("Rupam Register","Error Firebase${it.message}")
                                }

                            startActivity(Intent(baseContext, MainActivity::class.java))
                        }

                        override fun onFailure(call: Call<NewUserCreatedResponse>, t: Throwable) {
                            Log.d("Rupam $TAG", "Response: ${t.message}")
                        }
                    })


            }

        }
    }

    private val READ_CONTACTS_PERMISSION_REQUEST = 1

    private fun pickContact() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), READ_CONTACTS_PERMISSION_REQUEST)
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
                Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
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
            val cursor = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                    val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
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
                                binding.trustedContactNumber.setText("$name -$phoneNum")
                            }
                        }
                    } else {
                        Toast.makeText(this, "Contact has no phone number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}