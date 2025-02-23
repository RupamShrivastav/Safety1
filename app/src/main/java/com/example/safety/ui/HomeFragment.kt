package com.example.safety.ui


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safety.Constants
import com.example.safety.MapplsMapFragment
import com.example.safety.R
import com.example.safety.SafetyAdapter
import com.example.safety.SharedPrefFile
import com.example.safety.api.RetrofitInstance
import com.example.safety.databinding.FragmentHomeBinding
import com.example.safety.models.UsersListModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE
        binding.mainContent.visibility = View.GONE

        binding.progressBar.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
        }, 1500)


        Log.v("FetchContacts", "1")

        binding.rvHome.layoutManager = LinearLayoutManager(requireContext())
        Log.v("FetchContacts", "2")

        val sharedpref = SharedPrefFile
        sharedpref.init(requireContext())
        val userData = sharedpref.getUserData(Constants.SP_USERDATA)!!

        Log.d("Rupam Home Fragment","Data $userData")

        val retrofit = RetrofitInstance.initialize()
        val users = retrofit.getUsersByOrg(userData.organization)
        Thread.sleep(3000)
        users.enqueue(object : Callback<UsersListModel>{
            override fun onResponse(
                call: Call<UsersListModel>,
                response: Response<UsersListModel>,
            ) {
                Log.d(Constants.TAG,"AllUsersByOrg ${response.body()}")
                sharedpref.putAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG, response.body()!!)
                val adapter = SafetyAdapter(response.body()!!)
                binding.rvHome.adapter = adapter
                binding.rvHome.adapter?.notifyDataSetChanged()

            }

            override fun onFailure(call: Call<UsersListModel>, t: Throwable) {
                Log.d(Constants.TAG,"${t.message}")
            }

        })

//            db.collection("User Data")
//                .get()
//                .addOnSuccessListener { result ->
//
//                    val usersList = mutableListOf<Model>()
//                    for (usersInfo in result.documents) {
//                        val user = usersInfo.toObject(Users::class.java)
//                        if (!result.isEmpty) {
//                            val name = user?.fullName.orEmpty()
//                            val latitude = user?.lat.orEmpty()
//                            val longitude = user?.long.orEmpty()
//                            val phoneNumber = user?.phoneNumber.orEmpty()
//                            val batPer = user!!.batPer
//                            val connectionInfo = user.connectionInfo.orEmpty()
//
//                            val model = Model(name,phoneNumber,batPer,latitude,longitude,connectionInfo)
//                            usersList.add(model)
//                        }
//                        val adapter = safetyAdapter(usersList)
//                        binding.rvHome.adapter = adapter
//                        binding.rvHome.adapter?.notifyDataSetChanged()
//                    }
//
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("FireStoreData", "Error getting documents: ", exception)
//                    Toast.makeText(
//                        requireContext(),
//                        "Error while retrieving data from firestore",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }


        binding.mapIcon.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, MapplsMapFragment())
                .addToBackStack(null)
                .commit()
        }


        binding.dotIcon.setOnClickListener {
            val popMenu = PopupMenu(it.context,it)
            popMenu.menuInflater.inflate(R.menu.home_menu,popMenu.menu)

            popMenu.setOnMenuItemClickListener {

                when(it.itemId){
                    R.id.settings ->{
                        requireActivity().supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.container, ProfileFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> {false}
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
