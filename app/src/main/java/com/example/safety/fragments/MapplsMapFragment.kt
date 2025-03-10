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

/**
 * MapplsMapFragment
 */
class MapplsMapFragment : Fragment() {

    // Variables to store location and marker details
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var markertitle: String = ""
    private var isOnDashboard: Boolean = false

    lateinit var binding: FragmentMapsBinding // View binding instance for UI components
    private lateinit var mapplsMap: MapplsMap // Map instance

    // Callback for when the Map is ready
    private val mapplsCallback = object : OnMapReadyCallback {
        override fun onMapReady(map: MapplsMap) {
            mapplsMap = map

            // Ensure location permissions are granted
            if (!hasLocationPermission()) {
                requestLocationPermission()
                return
            }

            // Enable location tracking on the map
            enableUserLocation(mapplsMap)

            if (isOnDashboard) {
                // Fetch and display all users' locations on the map
                fetchAllUsersLocations()
            } else {
                // Show a single marker on the map
                showLocationWithMarker(LocationMarkerModel(latitude, longitude, markertitle))
            }
        }

        override fun onMapError(errorCode: Int, errorMessage: String?) {
            Log.e(Constants.TAG, "Map error: $errorMessage")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using view binding
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize map fragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(mapplsCallback)

        // Retrieve arguments (latitude, longitude, marker title, and dashboard status)
        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
            markertitle = it.getString(ARG_MARKER_TITLE).orEmpty()
            isOnDashboard = it.getBoolean(IS_ON_DASHBOARD)
        }
    }

    /**
     * Checks if the app has the required location permissions
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(), ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests location permissions from the user
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Enables user location tracking on the map
     */
    private fun enableUserLocation(map: MapplsMap) {
        val locationComponent = map.locationComponent
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(requireContext(), map.style!!).build()
        )
        locationComponent.isLocationComponentEnabled = true
    }

    /**
     * Fetches all users' locations from Firestore and displays them as markers
     */
    private fun fetchAllUsersLocations() {
        val sharedPref = SharedPrefFile.getAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG)
        Log.d(Constants.TAG, "All users: $sharedPref")

        val firestoreTasks = mutableListOf<Task<DocumentSnapshot>>()
        val locationMarkers = mutableListOf<LocationMarkerModel>()

        for (user in sharedPref!!) {
            val task = FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION)
                .document(user.email)
                .get()
            firestoreTasks.add(task)
        }

        // Execute all Firestore requests in parallel and process the results
        Tasks.whenAllSuccess<DocumentSnapshot>(firestoreTasks)
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject(Users::class.java)
                    if (user != null) {
                        locationMarkers.add(LocationMarkerModel(
                            user.lat.toDouble(),
                            user.long.toDouble(),
                            user.name
                        ))
                    }
                }
                // Display all user markers on the map
                showMultipleLocationWithMarker(ArrayList(locationMarkers))
            }
            .addOnFailureListener { exception ->
                Log.e(Constants.TAG, "Error fetching locations", exception)
                Toast.makeText(requireContext(), "Failed to load all locations", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Displays a single location marker on the map
     */
    private fun showLocationWithMarker(location: LocationMarkerModel) {
        val point = LatLng(location.lat, location.lng)
        val cameraPosition = CameraPosition.Builder()
            .target(point)
            .zoom(10.0)
            .tilt(0.0)
            .build()

        mapplsMap.cameraPosition = cameraPosition
        val markerOptions = MarkerOptions()
            .position(point)
            .icon(IconFactory.getInstance(requireContext()).fromBitmap(
                getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_person_pin_circle_24)
            ))
            .title(location.markerTitle)

        mapplsMap.addMarker(markerOptions)
    }

    /**
     * Displays multiple location markers on the map
     */
    private fun showMultipleLocationWithMarker(locations: ArrayList<LocationMarkerModel>) {
        for (location in locations) {
            val point = LatLng(location.lat, location.lng)
            val cameraPosition = CameraPosition.Builder()
                .target(point)
                .zoom(10.0)
                .tilt(0.0)
                .build()

            mapplsMap.cameraPosition = cameraPosition
            val markerOptions = MarkerOptions()
                .position(point)
                .icon(IconFactory.getInstance(requireContext()).fromBitmap(
                    getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_person_pin_circle_24)
                ))
                .title(location.markerTitle)

            mapplsMap.addMarker(markerOptions)
        }
    }

    /**
     * Converts a vector drawable into a Bitmap for use as a marker icon
     */
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val ARG_LATITUDE = "arg_latitude"
        private const val ARG_LONGITUDE = "arg_longitude"
        private const val ARG_MARKER_TITLE = "marker_title"
        private const val IS_ON_DASHBOARD = "is_on_dashboard"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        /**
         * Creates a new instance of the fragment with provided arguments
         */
        fun newInstance(latitude: Double?, longitude: Double?, markerTitle: String?, isOnDashboard: Boolean): MapplsMapFragment {
            return MapplsMapFragment().apply {
                arguments = Bundle().apply {
                    latitude?.let { putDouble(ARG_LATITUDE, it) }
                    longitude?.let { putDouble(ARG_LONGITUDE, it) }
                    markerTitle?.let { putString(ARG_MARKER_TITLE, it) }
                    putBoolean(IS_ON_DASHBOARD, isOnDashboard)
                }
            }
        }
    }
}

