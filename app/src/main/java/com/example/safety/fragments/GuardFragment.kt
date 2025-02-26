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

class GuardFragment : Fragment() {

    lateinit var binding: FragmentGuardBinding
    val sp = SharedPrefFile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGuardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = SharedPrefFile
        sharedPref.init(requireContext())
        val numSOS = sharedPref.getUserData(Constants.SP_USERDATA)!!.trustedContactNumber
        binding.cvSOS.setOnClickListener {

            val dialog = Dialog(requireContext())

            dialog.setContentView(R.layout.sos_dialog_box)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog.show()

            dialog.findViewById<LinearLayout>(R.id.number100).setOnClickListener{
                callNumber("100")
            }

            dialog.findViewById<LinearLayout>(R.id.number102).setOnClickListener{
                callNumber("102")
            }

            dialog.findViewById<LinearLayout>(R.id.number112).setOnClickListener{
                callNumber("112")
            }

            dialog.findViewById<LinearLayout>(R.id.number139).setOnClickListener{
                callNumber("139")
            }

            dialog.findViewById<TextView>(R.id.trustedContName).text = sharedPref.getUserData(
                Constants.SP_USERDATA)!!.trustedContactName
            dialog.findViewById<LinearLayout>(R.id.trustedContact).setOnClickListener{
                Log.d("TrustedContact",numSOS)
                callNumber(numSOS)
            }
        }

        binding.cvGuard.setOnClickListener {
            val msg = "I am safe for now"

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:$numSOS")

            intent.putExtra("sms_body",msg)

            if (requireContext().packageManager.resolveActivity(intent, 0) != null) {
                requireContext().startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Install whatsapp",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }


    private fun callNumber(number: String) {
            val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${number.toLong()}")
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GuardFragment()
    }

    private fun normalizePhoneNumber(number: String?): String? {
        var phoneNumber = number ?: return null

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
}










