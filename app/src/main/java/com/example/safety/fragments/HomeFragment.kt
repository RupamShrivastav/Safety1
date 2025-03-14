package com.example.safety.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safety.common.Constants
import com.example.safety.R
import com.example.safety.adapter.SafetyAdapter
import com.example.safety.common.SharedPrefFile
import com.example.safety.api.RetrofitInstance
import com.example.safety.databinding.FragmentHomeBinding
import com.example.safety.models.UsersListModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * HomeFragment
 */
class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding  // View binding instance for accessing UI components

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var adapter = SafetyAdapter(UsersListModel())
            // Initially show progress bar and hide main content
        binding.progressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        // Simulate loading time (1.5s) before displaying main content
        binding.progressBar.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
        }, 1500)

        Log.v("FetchContacts", "1") // Debugging log

        // Setup RecyclerView with a LinearLayoutManager
        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())

        Log.v("FetchContacts", "2") // Debugging log

        // Initialize SharedPreferences
        val sharedPref = SharedPrefFile
        sharedPref.init(requireContext())

        // Retrieve the logged-in user data from SharedPreferences
        val userData = sharedPref.getUserData(Constants.SP_USERDATA)!!

        Log.d("@@@@@@@@ home", "Data $userData") // Debugging log

        // Initialize Retrofit instance and fetch users of the same organization
        val retrofit = RetrofitInstance.initialize()
        val users = retrofit.getUsersByOrg(userData.organization)

        // Avoid using Thread.sleep(3000), as it blocks the UI thread and can cause ANR (Application Not Responding)
        users.enqueue(object : Callback<UsersListModel> {
            override fun onResponse(
                call: Call<UsersListModel>,
                response: Response<UsersListModel>
            ) {
                if (response.isSuccessful) {
                    Log.d(Constants.TAG, "AllUsersByOrg ${response.body()}")

                    // Save users list to SharedPreferences
                    sharedPref.putAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG, response.body()!!)

                    // Set up RecyclerView adapter with retrieved user list
                    adapter = SafetyAdapter(response.body()!!)
                    binding.rvHome.adapter = adapter
                    binding.rvHome.adapter?.notifyDataSetChanged()
                } else {
                    Log.e(Constants.TAG, "Error fetching users: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UsersListModel>, t: Throwable) {
                Log.e(Constants.TAG, "API Call Failed: ${t.message}")
            }
        })

        // Map icon click listener: Navigate to the Map fragment
        binding.mapIcon.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MapplsMapFragment())
                .addToBackStack(null)
                .commit()
        }

        // Dots menu icon click listener: Show popup menu with options
        binding.dotIcon.setOnClickListener {
            val popMenu = PopupMenu(it.context, it)
            popMenu.menuInflater.inflate(R.menu.home_menu, popMenu.menu)

            // Handle menu item clicks
            popMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.settings -> {
                        // Navigate to ProfileFragment (Settings screen)
                        requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, ProfileFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> false
                }
            }
            popMenu.show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
