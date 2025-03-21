package com.example.safety.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.safety.R
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.FragmentMapplsMapBinding
import com.example.safety.models.LocationMarkerModel
import com.example.safety.models.Users
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
 * Displays a Mappls map with user locations.
 */
class MapplsMapFragment : Fragment() {

    private var latitude: Double = 0.0 // Latitude
    private var longitude: Double = 0.0 // Longitude
    private var markerTitle: String = "" // Marker title
    private var isOnDashboard: Boolean = false // Flag to indicate if on dashboard

    private lateinit var binding: FragmentMapplsMapBinding // View binding
    private lateinit var mapplsMap: MapplsMap // Mappls map instance

    // Callback for when the Map is ready
    private val mapplsCallback = object : OnMapReadyCallback {
        override fun onMapReady(map: MapplsMap) {
            mapplsMap = map
            setupMap() // Set up the map after it's ready
        }

        override fun onMapError(errorCode: Int, errorMessage: String?) {
            Log.e(Constants.TAG, "Map error: $errorCode - $errorMessage") // Log map errors
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapplsMapBinding.inflate(inflater, container, false) // Inflate layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get arguments passed to the fragment
        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
            markerTitle = it.getString(ARG_MARKER_TITLE) ?: "" // Default to empty string
            isOnDashboard = it.getBoolean(IS_ON_DASHBOARD)
        }

        // Initialize map fragment and set callback
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(mapplsCallback)
    }

    // Sets up the map after it's ready.
    private fun setupMap() {
        if (!hasLocationPermission()) { // Check location permissions
            requestLocationPermission() // Request permissions if not granted
            return
        }

        enableUserLocation(mapplsMap) // Enable user location on the map

        if (isOnDashboard) {
            fetchAllUsersLocations() // Fetch and display all users if on dashboard
        } else {
            showLocationWithMarker(LocationMarkerModel(latitude, longitude, markerTitle)) // Show single marker
        }
    }

    // Checks if location permissions are granted.
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(), ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Requests location permissions.
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    // Handles permission request results.  This is important for the location permission request.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.  Set up the map again.
                setupMap()
            } else {
                // Permission denied.  Show a message to the user.
                Toast.makeText(requireContext(), "Location permission is required to show the map.", Toast.LENGTH_LONG).show()
                // Optionally, navigate back or close the fragment
            }
        }
    }

    // Enables user location tracking on the map.
    private fun enableUserLocation(map: MapplsMap) {
        val locationComponent = map.locationComponent  // Get the location component
        val mapStyle = map.style // Get the map style

        if (mapStyle == null) {
            Log.e(
                "${Constants.TAG} MapplsMapFragment",
                "Map style is null. Location component cannot be enabled yet."
            )
            return  // Return if map style is not ready
        }

        try { // Added try-catch to handle potential IllegalStateException
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(requireContext(), mapStyle).build()
            ) // Activate location component
            locationComponent.isLocationComponentEnabled = true // Enable location component
        } catch (e: IllegalStateException) {
            Log.e("${Constants.TAG} MapplsMapFragment", "Error activating location component: ${e.message}", e)
            Toast.makeText(requireContext(), "Error enabling location tracking.", Toast.LENGTH_SHORT).show()
        }
    }


    // Fetches locations of all users from Firestore and displays them.
    private fun fetchAllUsersLocations() {
        val sharedPref = SharedPrefFile // Access SharedPrefFile. No need for .getInstance()
        val allUsers = sharedPref.getAllUsersByOrg(Constants.SP_ALL_USERS_BY_ORG) // Get users

        if (allUsers == null) {
            Log.w(Constants.TAG, "No users found in SharedPreferences.") // Log if no users
            // Consider showing an empty state to the user here.
            return
        }

        val firestoreTasks = mutableListOf<Task<DocumentSnapshot>>() // Tasks for Firestore
        val locationMarkers = mutableListOf<LocationMarkerModel>() // List to store markers

        // Create a Firestore task for each user.
        for (user in allUsers) {
            val task = FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION)
                .document(user.email) // Get user document by email
                .get()
            firestoreTasks.add(task) // Add task to the list
        }

        // Execute all tasks and process results when all are complete.
        Tasks.whenAllSuccess<DocumentSnapshot>(firestoreTasks)
            .addOnSuccessListener { documents ->
                // Create LocationMarkerModel objects from the fetched data
                for (document in documents) {
                    val user = document.toObject(Users::class.java)
                    if (user != null) {  // Check if user data exists
                        val lat = user.lat.toDoubleOrNull() ?: continue // Safely convert to Double
                        val lng = user.long.toDoubleOrNull() ?: continue // Safely convert
                        locationMarkers.add(LocationMarkerModel(lat, lng, user.name))
                    }
                }
                showMultipleLocationWithMarker(locationMarkers) // Display markers
            }
            .addOnFailureListener { exception ->
                // Handle failure to fetch data
                Log.e(Constants.TAG, "Error fetching locations", exception)
                Toast.makeText(requireContext(), "Failed to load locations", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Displays a single location marker on the map.
    private fun showLocationWithMarker(location: LocationMarkerModel) {
        val point = LatLng(location.lat, location.lng) // Create LatLng from location
        // Set camera position to focus on the marker
        mapplsMap.cameraPosition = CameraPosition.Builder()
            .target(point) // Set target location
            .zoom(10.0) // Set zoom level
            .tilt(0.0) // Set tilt angle
            .build()

        // Create and add the marker
        val markerOptions = MarkerOptions()
            .position(point) // Set marker position
            .icon(
                IconFactory.getInstance(requireContext()).fromBitmap(
                    getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_person_pin_circle_24) // Use a custom icon
                )
            )
            .title(location.markerTitle) // Set marker title
        mapplsMap.addMarker(markerOptions) // Add marker to the map
    }

    // Displays multiple location markers on the map.
    private fun showMultipleLocationWithMarker(locations: List<LocationMarkerModel>) {
        if (!::mapplsMap.isInitialized) return; // Ensure map is initialized

        for (location in locations) { // Loop through all location markers
            val point = LatLng(location.lat, location.lng) // Create LatLng object
            val markerOptions = MarkerOptions() // Create marker options
                .position(point) // Set marker position
                .icon(
                    IconFactory.getInstance(requireContext()).fromBitmap(
                        getBitmapFromVectorDrawable(requireContext(), R.drawable.baseline_person_pin_circle_24) // Use custom icon
                    )
                )
                .title(location.markerTitle) // Set marker title

            mapplsMap.addMarker(markerOptions) // Add marker to the map
        }
        // Set a reasonable camera position to show *all* markers.  This is a very basic implementation;
        // a more robust solution would calculate the bounds of all markers and set the camera accordingly.
        if (locations.isNotEmpty()) {
            val firstPoint = LatLng(locations.first().lat, locations.first().lng)
            mapplsMap.cameraPosition = CameraPosition.Builder()
                .target(firstPoint)
                .zoom(5.0) // Start with a wider zoom to see multiple markers
                .tilt(0.0)
                .build()
        }
    }

    // Converts a vector drawable to a Bitmap for use as a marker icon.
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
            ?: throw IllegalArgumentException("Invalid drawable resource ID: $drawableId") // Handle null drawable

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) // Create bitmap
        val canvas = Canvas(bitmap) // Create canvas
        drawable.setBounds(0, 0, canvas.width, canvas.height) // Set bounds
        drawable.draw(canvas) // Draw drawable on canvas
        return bitmap // Return bitmap
    }

    companion object {
        private const val ARG_LATITUDE = "arg_latitude"
        private const val ARG_LONGITUDE = "arg_longitude"
        private const val ARG_MARKER_TITLE = "marker_title"
        private const val IS_ON_DASHBOARD = "is_on_dashboard"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        // Creates a new instance of the fragment with provided arguments.
        fun newInstance(
            latitude: Double? = null,
            longitude: Double? = null,
            markerTitle: String? = null,
            isOnDashBoard: Boolean = false
        ): MapplsMapFragment {
            return MapplsMapFragment().apply {
                arguments = Bundle().apply {
                    latitude?.let { putDouble(ARG_LATITUDE, it) } // Put latitude if not null
                    longitude?.let { putDouble(ARG_LONGITUDE, it) } // Put longitude if not null
                    markerTitle?.let { putString(ARG_MARKER_TITLE, it) } // Put title if not null
                    putBoolean(IS_ON_DASHBOARD, isOnDashBoard)
                }
            }
        }
    }
}