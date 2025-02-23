package com.example.safety.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.safety.Constants
import com.example.safety.SharedPrefFile
import com.example.safety.databinding.FragmentGuardBinding

class GuardFragment : Fragment() {

    lateinit var binding: FragmentGuardBinding
    val sp = SharedPrefFile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

            val msg = "I am safe for now"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setPackage("com.whatsapp")
//            intent.data = Uri.parse("https://wa.me/$numSOS&text=\${Uri.encode(msg)")
                        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=+91$numSOS&text=${
                            Uri.encode(
                                msg
                            )
                        }\")\n")

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

        binding.cvGuard.setOnClickListener {
            val msg = "I am safe for now"

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:+91$numSOS")

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

    companion object {
        @JvmStatic
        fun newInstance() = GuardFragment()
    }
}










