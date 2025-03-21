package com.example.safety.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safety.adapter.SafetyAdapter
import com.example.safety.api.ApiService
import com.example.safety.api.RetrofitInstance
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentHomeBinding
import com.example.safety.models.UsersListModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * HomeFragment
 * Home screen of the Smart Safety Tracking Application, showing connected users in the organization.
 */
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding // View binding
    lateinit var sharedPref: SharedPrefFile // Shared preferences
    lateinit var apiService: ApiService // Retrofit API service
    private lateinit var adapter: SafetyAdapter  // make the adapter a property of the Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false) // Inflate layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDependencies()       // Initialize SharedPref and ApiService
        setupRecyclerView()       // Set up the RecyclerView
        fetchUsers()            // Fetch and display user data

    }

    // Initializes SharedPref and ApiService.
    fun initDependencies() {
        sharedPref = SharedPrefFile
        sharedPref.init(requireContext()) // Initialize SharedPref
        apiService = RetrofitInstance.apiService // Initialize Retrofit ApiService Instance.
        adapter = SafetyAdapter(UsersListModel()) // Initialize adapter here
    }

    // Sets up the RecyclerView.
    fun setupRecyclerView() {
        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHome.adapter = adapter // Set the adapter
    }


    // Fetches users from the API and updates the RecyclerView.
    private fun fetchUsers() {
        val userData = sharedPref.getUserData(Constants.SP_USERDATA) // Get logged-in user data

        // Check if userData is null
        if (userData == null) {
            Log.e(Constants.TAG, "User data is null. Cannot fetch users.")
            // Consider showing an error message to the user or handling this case appropriately.
            // For example, you might redirect them to the login screen.
            return
        }

        binding.progressBar.visibility = View.VISIBLE // Show progress bar
        binding.mainContent.visibility = View.GONE // Hide main content

        apiService.getUsersByOrg(userData.organization).enqueue(object : Callback<UsersListModel> { // Make API call
            override fun onResponse(call: Call<UsersListModel>, response: Response<UsersListModel>) {
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.mainContent.visibility = View.VISIBLE // Show main content

                if (response.isSuccessful) {
                    val usersList = response.body()
                    if (usersList != null) {
                        sharedPref.putAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG, usersList) // Save to SharedPref
                        adapter = SafetyAdapter(usersList) // Create a *new* adapter with the data
                        binding.rvHome.adapter = adapter // Set the *new* adapter on the RecyclerView
                        adapter.notifyDataSetChanged()      // Notifying after setting it to the recycler view is useless.

                    } else {
                        Log.w(Constants.TAG, "User list is null.") // Log a warning if null
                        //Consider showing empty state
                    }
                } else {
                    Log.e(Constants.TAG, "Error fetching users: ${response.errorBody()?.string()}") // Log error response
                    //Consider showing Error state
                }
            }

            override fun onFailure(call: Call<UsersListModel>, t: Throwable) {
                binding.progressBar.visibility = View.GONE // Hide progress bar
                binding.mainContent.visibility = View.VISIBLE // Show main content (even on failure, for error messages)
                Log.e(Constants.TAG, "API call failed: ${t.message}", t) // Log failure
                // Consider showing an error message to the user, e.g., using a Toast or a TextView in your layout.
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
