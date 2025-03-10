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
import com.example.safety.common.Constants
import com.example.safety.R
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentGuardBinding

/**
 * GuardFragment
 */
class GuardFragment : Fragment() {

    private lateinit var binding: FragmentGuardBinding  // View binding instance
    private val sharedPref = SharedPrefFile  // Shared Preferences instance for storing user data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout using View Binding
        binding = FragmentGuardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref.init(requireContext())  // Initialize SharedPreferences

        // Retrieve the trusted contact number from SharedPreferences
        val numSOS = sharedPref.getUserData(Constants.SP_USERDATA)?.trustedContactNumber ?: "112"

        // Set up click listeners for **SOS Mode** and **Guard Mode**
        binding.cvSOS.setOnClickListener { showSOSDialog(numSOS) }
        binding.cvGuard.setOnClickListener { sendSafetyMessage(numSOS) }
    }

    /**
     * Displays an **SOS emergency dialog** with options to call emergency numbers or a trusted contact.
     */
    private fun showSOSDialog(numSOS: String) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.sos_dialog_box)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

        // Click listeners for emergency numbers
        dialog.findViewById<LinearLayout>(R.id.number100).setOnClickListener { callNumber("100") }  // Police
        dialog.findViewById<LinearLayout>(R.id.number102).setOnClickListener { callNumber("102") }  // Ambulance
        dialog.findViewById<LinearLayout>(R.id.number112).setOnClickListener { callNumber("112") }  // Emergency
        dialog.findViewById<LinearLayout>(R.id.number139).setOnClickListener { callNumber("139") }  // Railway Helpline

        // Set trusted contact details in the dialog
        dialog.findViewById<TextView>(R.id.trustedContName).text =
            sharedPref.getUserData(Constants.SP_USERDATA)?.trustedContactName ?: "Unknown Contact"

        // Call the trusted contact when clicked
        dialog.findViewById<LinearLayout>(R.id.trustedContact).setOnClickListener {
            Log.d("TrustedContact", "Calling $numSOS")
            callNumber(numSOS)
        }
    }

    /**
     * Sends a predefined **safety message** ("I am safe for now") to the trusted contact.
     * - Opens the default **SMS app** with the message pre-filled.
     * - If SMS is unavailable, suggests installing a messaging app.
     */
    private fun sendSafetyMessage(numSOS: String) {
        val msg = "I am safe for now"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$numSOS")  // Set recipient
            putExtra("sms_body", msg)  // Add message content
        }

        // Check if there's an available SMS app
        if (requireContext().packageManager.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Install WhatsApp or SMS app", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * **Initiates a call** to the given phone number.
     * - Uses **ACTION_DIAL** so the user can confirm before calling.
     */
    private fun callNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    /**
     * **Normalizes a phone number** by:
     * - Removing spaces
     * - Removing country codes (if applicable)
     */
    private fun normalizePhoneNumber(number: String?): String? {
        var phoneNumber = number ?: return null
        phoneNumber = phoneNumber.replace(" ", "")

        // Remove country code if present
        if (phoneNumber.length > 10 && phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(3)
        }

        Log.d("Normalized Phone", phoneNumber)
        return phoneNumber
    }

    /**
     * **Factory method** to create a new instance of `GuardFragment`.
     */
    companion object {
        @JvmStatic
        fun newInstance() = GuardFragment()
    }
}
