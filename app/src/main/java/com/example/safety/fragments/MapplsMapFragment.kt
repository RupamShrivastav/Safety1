package com.example.safety.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safety.R
import com.example.safety.models.Users
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentMapsBinding
import com.example.safety.models.LocationMarkerModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.mappls.sdk.maps.MapplsMap
import com.mappls.sdk.maps.OnMapReadyCallback
import com.mappls.sdk.maps.SupportMapFragment
import com.mappls.sdk.maps.annotations.IconFactory
import com.mappls.sdk.maps.annotations.MarkerOptions
import com.mappls.sdk.maps.camera.CameraPosition
import com.mappls.sdk.maps.geometry.LatLng
import com.mappls.sdk.maps.location.LocationComponentActivationOptions

class MapplsMapFragment : Fragment() {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var markertitle:String = ""
    private var isOnDashboard : Boolean = false

    lateinit var binding: FragmentMapsBinding
    private lateinit var mapplsMap: MapplsMap

    private val mapplsCallback = object : OnMapReadyCallback {
        override fun onMapReady(map: MapplsMap) {
            mapplsMap = map
            mapplsMap.getStyle { style ->

                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        requireContext(),
                        "Location Permission Not Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(
                            ACCESS_COARSE_LOCATION,
                            ACCESS_FINE_LOCATION
                        ), 1000
                    )

                    return@getStyle
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Location Permission Granted",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                val locationComponent = mapplsMap.locationComponent
                locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        requireContext(), style
                    ).build()
                )
                locationComponent.isLocationComponentEnabled = true
                if(isOnDashboard) {
                    val sharedPref = SharedPrefFile.getAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG)
                    Log.d(Constants.TAG, "All users: $sharedPref")

                    val firestoreTasks = mutableListOf<Task<DocumentSnapshot>>()
                    val locationMarkers = mutableListOf<LocationMarkerModel>()

                    for (users in sharedPref!!) {
                        Log.d(Constants.TAG, "Fetching data for user: $users")
                        val fdb = FirebaseFirestore.getInstance()
                        val task = fdb.collection(Constants.FIRESTORE_COLLECTION)
                            .document(users.email)
                            .get()
                        firestoreTasks.add(task)
                    }

                    Tasks.whenAllSuccess<DocumentSnapshot>(firestoreTasks)
                        .addOnSuccessListener { documents ->
                            Log.d(Constants.TAG, "All location data fetched successfully")
                            for (document in documents) {
                                val user = document.toObject(Users::class.java)
                                if (user != null) {
                                    locationMarkers.add(LocationMarkerModel(
                                        user.lat.toDouble(),
                                        user.long.toDouble(),
                                        user.name
                                    ))
                                    Log.d(Constants.TAG, "Added marker for ${user.name}")
                                }
                            }

                            showMultipleLocationWithMarker(ArrayList(locationMarkers))
                        }
                        .addOnFailureListener { exception ->
                            Log.e(Constants.TAG, "Error fetching locations", exception)
                            Toast.makeText(
                                requireContext(),
                                "Failed to load all locations",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }else{
                    Log.d("@@@@@@@","$latitude $markertitle $longitude")
                    showLocationWithMarker(
                        LocationMarkerModel(latitude,longitude,markertitle)
                    )
                }
            }
        }

        override fun onMapError(p0: Int, p1: String?) {
            Log.d(Constants.TAG, p1.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(mapplsCallback)

        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
            markertitle = it.getString(ARG_MARKER_TITLE).toString()
            isOnDashboard = it.getBoolean(IsOnDashboard)
            Log.d(Constants.TAG,isOnDashboard.toString())
        }
    }

    private fun showLocationWithMarker(location : LocationMarkerModel) {
        val point = LatLng(location.lat,location.lng )
        val cameraPosition = CameraPosition.Builder()
            .target(point)
            .zoom(7.0)
            .tilt(0.0)
            .build()
        mapplsMap.cameraPosition = cameraPosition

        val markerOptions: MarkerOptions = MarkerOptions().position(point).icon(
            IconFactory.getInstance(requireContext()).fromBitmap(
                getBitmapFromVectorDrawable(
                    requireContext(),
                    R.drawable.baseline_person_pin_circle_24
                )
            )
        ).title(location.markerTitle)

//        markerOptions.snippet = "This is a Marker"
        mapplsMap.addMarker(markerOptions)
    }

    private fun showMultipleLocationWithMarker(locations : ArrayList<LocationMarkerModel>) {

        for(location in locations) {
            Log.d(Constants.TAG,isOnDashboard.toString()+" shoa")
            Log.d(Constants.TAG,"$location")
            val point = LatLng(location.lat,location.lng)
            val cameraPosition = CameraPosition.Builder()
                .target(point)
                .zoom(10.0)
                .tilt(0.0)
                .build()
            mapplsMap.cameraPosition = cameraPosition

            val markerOptions: MarkerOptions = MarkerOptions().position(point).icon(
                IconFactory.getInstance(requireContext()).fromBitmap(
                    getBitmapFromVectorDrawable(
                        requireContext(),
                        R.drawable.baseline_person_pin_circle_24
                    )
                )
            ).title(location.markerTitle)

//            markerOptions.snippet = "This is a Marker"
            mapplsMap.addMarker(markerOptions)
        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val ARG_LATITUDE = "arg_latitude"
        private const val ARG_LONGITUDE = "arg_longitude"
        private const val ARG_MARKER_TITLE = "marker_title"
        private const val IsOnDashboard = "isOnDashboard"

        fun newInstance(latitude: Double?, longitude: Double?, markerTitle : String?, isOnDashboard: Boolean): MapplsMapFragment {
            val fragment = MapplsMapFragment()
            val args = Bundle()
            latitude?.let { args.putDouble(ARG_LATITUDE, it) }
            longitude?.let { args.putDouble(ARG_LONGITUDE, it) }
            markerTitle?.let { args.putString(ARG_MARKER_TITLE, markerTitle)}
            args.putBoolean(IsOnDashboard, isOnDashboard)
            fragment.arguments = args
            return fragment

        }
    }

}
