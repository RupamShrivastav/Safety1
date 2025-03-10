package com.example.safety.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.safety.common.Constants
import com.example.safety.R
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityMainBinding
import com.example.safety.fragments.GuardFragment
import com.example.safety.fragments.HomeFragment
import com.example.safety.fragments.MapplsMapFragment
import com.example.safety.fragments.ProfileFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * MainActivity
 *
 * This is the main dashboard/home screen of the application after login.
 * It provides access to core features of the Safety App.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding // View binding for UI components

    // Array of permissions required for the app to function
    private val permissionArray = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.SEND_SMS
    )

    private val permissionCode = 23 // Request code for permission handling

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back button press behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNav.selectedItemId == R.id.btm_home) {
                    finish() // Exit the app if already on the home screen
                } else {
                    binding.bottomNav.selectedItemId = R.id.btm_home // Navigate to home screen
                }
            }
        })

        // Check for permissions and location settings
        if (isAllPermissionGranted()) {
            if (isLocationEnabled(this)) {
                setUpLocationListener(this) // Start location updates
            } else {
                showGPSNotEnabledDialog(this) // Prompt user to enable GPS
            }
        } else {
            askForPerm() // Request permissions
        }

        // Handle bottom navigation item selection
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.btm_guard -> {
                    inflateFragment(GuardFragment.newInstance())
                }
                R.id.btm_home -> {
                    inflateFragment(HomeFragment.newInstance())
                }
                R.id.btm_dashboard -> {
                    val tranc = supportFragmentManager.beginTransaction()
                    tranc.replace(R.id.container, MapplsMapFragment.newInstance(null, null, null, true))
                        .addToBackStack(null)
                        .commit()
                }
                R.id.btm_profile -> {
                    inflateFragment(ProfileFragment.newInstance())
                }
            }
            true
        }

        // Set default selected item to home
        binding.bottomNav.selectedItemId = R.id.btm_home
    }

    /**
     * Sets up the location listener to receive location updates
     */
    private fun setUpLocationListener(context: Context) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        Log.d("LocationData", "Starting location setup")

        val locationRequest = LocationRequest().apply {
            interval = 2000 // Update interval in milliseconds
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationData", "Permission check failed")
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    Log.d("LocationData", "Received location update")

                    for (location in locationResult.locations) {
                        Log.d("FirestoreUpdate", "Processing location: ${location.latitude}, ${location.longitude}")

                        val db = FirebaseFirestore.getInstance()
                        val sharedPref = SharedPrefFile
                        sharedPref.init(context)

                        val userData = sharedPref.getUserData(Constants.SP_USERDATA)

                        userData?.let {
                            val mail = userData.email
                            Log.d("FirestoreUpdate", "User email: $mail")

                            // Data to be stored in Firestore
                            val data = hashMapOf(
                                "lat" to location.latitude.toString(),
                                "long" to location.longitude.toString(),
                                "connectionInfo" to networkConnected(),
                                "batPer" to batteryPercentage(),
                                "name" to userData.fullName,
                                "phoneNumber" to userData.phoneNumber
                            )

                            Log.d("FirestoreUpdate", "Attempting to update with data: $data")

                            // Store user location and details in Firestore
                            db.collection(Constants.FIRESTORE_COLLECTION)
                                .document(mail)
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d("FirestoreUpdate", "Successfully updated location")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreUpdate", "Failed to update location", e)
                                }
                        }
                    }
                }
            },
            Looper.myLooper()
        )
    }

    /**
     * Requests necessary permissions from the user
     */
    private fun askForPerm() {
        Log.d("LocationData", "Requesting permissions")
        ActivityCompat.requestPermissions(this, permissionArray, permissionCode)
    }

    /**
     * Replaces the current fragment with a new one
     */
    private fun inflateFragment(newInstance: Fragment) {
        val tranc = supportFragmentManager.beginTransaction()
        tranc.replace(R.id.container, newInstance)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Handles the result of permission requests
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionCode) {
            if (isAllPermissionGranted()) {
                Log.d("LocationData", "All permissions granted")
                if (isLocationEnabled(this)) {
                    setUpLocationListener(this)
                } else {
                    showGPSNotEnabledDialog(this)
                }
            }
        }
    }

    /**
     * Returns the current battery percentage
     */
    fun batteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val percentage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        Log.d("LocationData", "Battery percentage: $percentage%")
        return percentage
    }

    /**
     * Checks if all required permissions are granted
     */
    private fun isAllPermissionGranted(): Boolean {
        for (items in permissionArray) {
            if (ActivityCompat.checkSelfPermission(this, items) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Checks the network connection status
     */
    fun networkConnected(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        return when {
            networkInfo == null -> "Offline"
            networkInfo.type == ConnectivityManager.TYPE_WIFI -> "Wi-Fi"
            networkInfo.type == ConnectivityManager.TYPE_MOBILE -> "Data"
            else -> "NA"
        }
    }

    /**
     * Checks if location services are enabled
     */
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Shows a dialog prompting the user to enable GPS
     */
    private fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.enable_gps))
            .setMessage(context.getString(R.string.required_for_this_app))
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.enable_now)) { _, _ ->
                context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }
}
