package com.example.safety.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.safety.R
import com.example.safety.common.Constants
import com.example.safety.databinding.ModelBinding
import com.example.safety.fragments.MapplsMapFragment
import com.example.safety.models.Users
import com.example.safety.models.UsersListModel
import com.google.firebase.firestore.FirebaseFirestore

/**
 * SafetyAdapter
 *
 * This RecyclerView adapter is responsible for displaying a list of users and their safety details.
 * It binds data from a list of `UserModelItem` objects to the UI elements in each item view.
 *
 * @param memberList The list of users containing their details.
 */
class SafetyAdapter(private val memberList: UsersListModel) :
    RecyclerView.Adapter<SafetyAdapter.ViewHolder>() {

    val TAG = "Rupam Safety Adapter"
    private lateinit var fdb: FirebaseFirestore // Firestore instance for fetching user details

    // ViewHolder class to hold the view binding for each item
    inner class ViewHolder(var binding: ModelBinding) : RecyclerView.ViewHolder(binding.root)

    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

 // Suppresses warning for setting text directly in UI
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = memberList!![position] // Gets the user at the current position
        fdb = FirebaseFirestore.getInstance() // Initializes Firestore instance

        // Fetches user details from Firestore using the email as the document ID
        fdb.collection(Constants.FIRESTORE_COLLECTION)
            .document(item.email)
            .get()
            .addOnSuccessListener { doc ->
                val mem = doc.toObject(Users::class.java)!! // Converts Firestore document to Users object
                Log.d("Rupam Safety Adapter", " $mem")

                // Binds retrieved data to UI elements
                holder.binding.nameModel.text = mem.name
                holder.binding.addModel.text = "Latitude: ${mem.lat}\nLongitude: ${mem.long}"
                holder.binding.batPerModel.text = "${mem.batPer}%" // Battery percentage
                holder.binding.connectionTvModel.text = mem.connectionInfo // Connection info

                // Handles phone call action when the call icon is clicked
                holder.binding.callImgModel.setOnClickListener {
                    val phoneNum = mem.phoneNumber.toLong()
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNum") // Opens dialer with the user's phone number
                    holder.itemView.context.startActivity(intent)
                }

                // Handles map navigation when the item is clicked
                holder.itemView.setOnClickListener {
                    val latitude = mem.lat.toDouble()
                    val longitude = mem.long.toDouble()
                    Log.d(TAG, "$mem.name $latitude $longitude ${mem.long}")

                    // Creates a map fragment with the user's location
                    val fragment = MapplsMapFragment.newInstance(latitude, longitude, mem.name, false)

                    val activity = it.context as AppCompatActivity
                    activity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, fragment) // Replaces the current fragment with the map
                        .addToBackStack(null) // Allows back navigation
                        .commit()
                }
            }
            .addOnFailureListener {
                Log.d("Rupam Safety Adapter Error", "${it.message}") // Logs error if Firestore fetch fails
            }
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int {
        return memberList?.size ?: 0
    }
}
