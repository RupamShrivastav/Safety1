package com.example.safety.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.safety.R
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentServiceBinding
import com.example.safety.models.Users
import com.google.firebase.firestore.FirebaseFirestore

/**
 * ServiceFragment
 * Provides SOS and Guard functionality.
 */
class ServiceFragment : Fragment() {

    private lateinit var binding: FragmentServiceBinding // View binding
    private lateinit var sharedPref: SharedPrefFile // Shared preferences
    private lateinit var fdb: FirebaseFirestore // Firestore instance

    private var lat: String = "0.0"  // Default values, in case they can't be loaded
    private var lng: String = "0.0"
    private var name: String = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentServiceBinding.inflate(inflater, container, false) // Inflate layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDependencies()  // Initialize dependencies
        fetchUserData()    // Load user data (including location)
        setupClickListeners() // Set up click listeners
    }

    // Initializes dependencies.
    private fun initDependencies() {
        sharedPref = SharedPrefFile
        sharedPref.init(requireContext()) // Initialize shared preferences
        fdb = FirebaseFirestore.getInstance() // Initialize Firestore
    }

    // Fetches the current user's data from Firestore, including location.
    private fun fetchUserData() {
        val currentUser = sharedPref.getUserData(Constants.SP_USERDATA) ?: run {
            Log.e("ServiceFragment", "User data is null. Cannot fetch location.")
            return
        }

        // Retrieve user data including the location data from the firestore
        fdb.collection(Constants.FIRESTORE_COLLECTION).document(currentUser.email)
            .get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(Users::class.java)
                if (user != null) { // Check of the user object is not null
                    lat = user.lat
                    lng = user.long
                    name = user.name
                    Log.d("ServiceFragment", "Fetched user data: $user")
                } else {
                    Log.w("ServiceFragment", "User data is null in Firestore.")
                    // Handle the case where user data does not exit.
                }
            }
            .addOnFailureListener { e ->
                Log.e("ServiceFragment", "Error fetching user data: ${e.message}", e)
                // Handle the error appropriately, e.g., show an error message to the user.
            }
    }

    // Sets up click listeners for the SOS and Guard buttons.
    private fun setupClickListeners() {
        val numSOS = sharedPref.getUserData(Constants.SP_USERDATA)?.trustedContactNumber ?: "112" // Fallback to 112

        binding.cvSOS.setOnClickListener { showSOSDialog(numSOS) } // Show SOS dialog
        binding.cvGuard.setOnClickListener { sendSafetyMessage(numSOS) } // Send safety message
    }

    // Displays an SOS emergency dialog with options to call emergency numbers or a trusted contact.
    private fun showSOSDialog(numSOS: String) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.sos_dialog_box)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

        // Set click listeners for emergency numbers
        dialog.findViewById<LinearLayout>(R.id.number100)?.setOnClickListener { callNumber("100") } // Police
        dialog.findViewById<LinearLayout>(R.id.number102)?.setOnClickListener { callNumber("102") } // Ambulance
        dialog.findViewById<LinearLayout>(R.id.number112)?.setOnClickListener { callNumber("112") } // Emergency
        dialog.findViewById<LinearLayout>(R.id.number139)?.setOnClickListener { callNumber("139") } // Railway

        // Set trusted contact name (if available)
        val trustedContactName = sharedPref.getUserData(Constants.SP_USERDATA)?.trustedContactName ?: "Unknown Contact"
        dialog.findViewById<TextView>(R.id.trustedContName)?.text = trustedContactName

        // Call trusted contact
        dialog.findViewById<LinearLayout>(R.id.trustedContact)?.setOnClickListener {
            callNumber(numSOS)
        }
    }

     // Sends a predefined safety message to the trusted contact.
    private fun sendSafetyMessage(numSOS: String) {
        val msg = "I am safe for now.\nHere is my current location:\nhttp://maps.google.com/maps?q=loc:$lat,$lng($name)"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$numSOS")  // Use smsto: for SMS
            putExtra("sms_body", msg) // Add the message to the intent
        }

        // Check if there is an activity that is capable of handling the intent to prevent app crash
        val packageManager = requireContext().packageManager
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent) // Start the activity if there's an available one
        } else {
            // Inform user if not SMS app available
            Toast.makeText(requireContext(), "No SMS app found. Please install one.", Toast.LENGTH_LONG).show()
        }
    }

     //Initiates a call to the given phone number.
    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number") // Use tel: for phone calls
        }
        startActivity(intent) // Start the dialer activity
    }

    companion object {
        @JvmStatic
        fun newInstance() = ServiceFragment()
    }
}