package com.example.safety.adapter

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
 * RecyclerView adapter for displaying a list of users and their safety details.
 */
class SafetyAdapter(private val memberList: UsersListModel) :
    RecyclerView.Adapter<SafetyAdapter.ViewHolder>() {

    private val TAG = "SafetyAdapter" // Logging tag
    private val fdb: FirebaseFirestore = FirebaseFirestore.getInstance() // Firestore instance

    // ViewHolder class to hold the view binding for each item.  Made *not* inner.
    class ViewHolder(var binding: ModelBinding) : RecyclerView.ViewHolder(binding.root)

    // Inflates the layout for each item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Binds data to the views in each item.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = memberList[position] // Get the user at the current position.  No !! needed.

        // Fetch user details from Firestore.
        fdb.collection(Constants.FIRESTORE_COLLECTION)
            .document(item.email)
            .get()
            .addOnSuccessListener { doc ->
                val mem = doc.toObject(Users::class.java) // Convert to Users object.
                if (mem != null) { // Check for null before proceeding
                    bindUserData(holder, mem) // Bind the fetched data
                    setupClickListeners(holder, mem) // Set up click listeners
                } else {
                    Log.w(TAG, "User data is null for email: ${item.email}") // Log a warning.
                    // Consider showing a placeholder or error state in the UI here.
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching user data: ${e.message}", e) // Log error.  More detail.
                // Consider showing a placeholder or error state in the UI here.
            }
    }

    // Binds the fetched user data to the views.
    private fun bindUserData(holder: ViewHolder, mem: Users) {
        holder.binding.nameModel.text = mem.name
        holder.binding.addModel.text = "Latitude: ${mem.lat}\nLongitude: ${mem.long}"
        holder.binding.batPerModel.text = "${mem.batPer}%" // Battery percentage
        holder.binding.connectionTvModel.text = mem.connectionInfo // Connection info
    }


    // Sets up click listeners for call and map actions.
    private fun setupClickListeners(holder: ViewHolder, mem: Users) {
        // Call button click listener
        holder.binding.callImgModel.setOnClickListener {
            val phoneNum = mem.phoneNumber
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNum") // Use apply for concise intent setup.
            }
            // Use a safe call with let to ensure context is not null
            holder.itemView.context?.let { context ->
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Log.e(TAG, "No activity found to handle dial intent")
                    // Optionally show a Toast to inform the user.
                }
            }
        }

        // Item click listener for map navigation
        holder.itemView.setOnClickListener {
            val latitude = mem.lat.toDoubleOrNull() ?: 0.0 // Safe conversion to Double. Defaults to 0.0
            val longitude = mem.long.toDoubleOrNull() ?: 0.0  // Safe conversion to Double
            Log.d(TAG, "Navigating to map: ${mem.name}, lat=$latitude, long=$longitude")

            val fragment = MapplsMapFragment.newInstance(latitude, longitude, mem.name, isOnDashBoard = false) // Pass data to fragment
            val activity = it.context as AppCompatActivity // Get activity from context

            activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment) // Replace current fragment with map
                .addToBackStack(null) // Add to back stack for navigation
                .commit()
        }
    }

    // Returns the total number of items.
    override fun getItemCount(): Int {
        return memberList.size // No !! needed
    }
}