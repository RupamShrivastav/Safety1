package com.example.safety.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.safety.Constants
import com.example.safety.LoginUserActivity
import com.example.safety.R
import com.example.safety.SharedPrefFile
import com.example.safety.databinding.FragmentProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
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

        val sharedPref = SharedPrefFile
        sharedPref.init(requireContext())

        val currentUser = sharedPref.getUserData(Constants.SP_USERDATA)!!
        binding.profileName.text = currentUser.fullName

        binding.inviteContacts.setOnClickListener {
            val trancs = parentFragmentManager.beginTransaction()
            trancs.replace(R.id.container, InviteFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.sosNum.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Enter Number ")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_NUMBER
            alertDialog.setView(input)

            alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                val enteredNumber = input.text.toString()
                sharedPref.putPhoneNum("PhoneNumberForSOS", enteredNumber)

            })

            alertDialog.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })

            alertDialog.create()
                .show()

        }

        binding.guardNum.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Enter Number ")

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_NUMBER
            alertDialog.setView(input)

            alertDialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->

                val enteredNumber = input.text.toString()

                sharedPref.putPhoneNum("PhoneNumberForGuard", enteredNumber)
            })

            alertDialog.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })

            alertDialog.create()
                .show()
        }

        binding.signout.setOnClickListener {
            sharedPref.putLoggedInfo(Constants.SP_LOGGED_INFO, false)
            val intent = Intent(requireContext(), LoginUserActivity::class.java)
            startActivity(intent)
            activity?.finishAffinity()
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()

    }
}
