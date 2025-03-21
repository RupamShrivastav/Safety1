package com.example.safety.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.safety.R
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityMainBinding
import com.example.safety.fragments.HomeFragment
import com.example.safety.fragments.MapplsMapFragment
import com.example.safety.fragments.ProfileFragment
import com.example.safety.fragments.ServiceFragment
import com.example.safety.models.UserModelItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * MainActivity
 * Main dashboard screen providing access to app features.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // View binding for activity_main.xml
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // Location provider client
    private lateinit var locationCallback: LocationCallback // Location callback
    private lateinit var batteryReceiver: BroadcastReceiver // Battery status receiver
    private lateinit var connectivityReceiver: BroadcastReceiver // Connectivity status receiver
    private lateinit var db: FirebaseFirestore // Firestore database instance
    private lateinit var sharedPref: SharedPrefFile // Shared preferences instance
    private var userData: UserModelItem? = null // User data from shared preferences
    private var lastBatteryPercentage = -1 // Last recorded battery percentage
    private var lastConnectionInfo = "" // Last recorded connection info

    // Required app permissions
    private val permissionArray = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.ACCESS_NETWORK_STATE,
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.SEND_SMS
    )
    private val permissionCode = 23 // Permission request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // Initialize view binding
        setContentView(binding.root)

        initServices() // Initialize services
        setupBackButton() // Setup back button behavior
        setupLocationCallback() // Setup location callback
        setupReceivers() // Setup battery and connectivity receivers
        checkPermissionsAndLocation() // Check permissions and location settings
        setupBottomNavigation() // Setup bottom navigation
    }

    // Initializes Firebase, SharedPref, LocationClient.
    private fun initServices() {
        db = FirebaseFirestore.getInstance() // Initialize Firestore
        sharedPref = SharedPrefFile // Initialize SharedPref
        sharedPref.init(this) // Initialize shared preferences
        userData = sharedPref.getUserData(Constants.SP_USERDATA) // Get user data
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this) // Initialize location client
    }

    // Handles back button to exit app from home screen.
    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNav.selectedItemId == R.id.btm_home) {
                    finish() // Finish activity if on home screen
                } else {
                    binding.bottomNav.selectedItemId = R.id.btm_home // Navigate to home screen
                }
            }
        })
    }

    // Sets up location update callback.
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.forEach { location ->
                    updateUserDataInFirestore(location.latitude.toString(), location.longitude.toString()) // Update Firestore with location
                }
            }
        }
    }

    // Sets up battery and connectivity receivers.
    private fun setupReceivers() {
        setupBatteryReceiver() // Setup battery receiver
        setupConnectivityReceiver() // Setup connectivity receiver
    }

    // Configures battery status receiver.
    private fun setupBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentBatteryPercentage = batteryPercentage() // Get current battery percentage
                if (currentBatteryPercentage != lastBatteryPercentage) { // Check if changed
                    lastBatteryPercentage = currentBatteryPercentage
                    updateUserDataInFirestore(batteryPercentageOnly = true) // Update Firestore
                }
            }
        }
        IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED) // Battery changed action
            addAction(Intent.ACTION_POWER_CONNECTED) // Power connected action
            addAction(Intent.ACTION_POWER_DISCONNECTED) // Power disconnected action
        }.also { filter ->
            registerReceiver(batteryReceiver, filter) // Register receiver
        }
    }

    // Configures connectivity status receiver.
    private fun setupConnectivityReceiver() {
        connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentConnectionInfo = networkConnected() // Get current connection info
                if (currentConnectionInfo != lastConnectionInfo) { // Check if changed
                    lastConnectionInfo = currentConnectionInfo
                    updateUserDataInFirestore(connectivityOnly = true) // Update Firestore
                }
            }
        }
        IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION) // Connectivity change action
        }.also { filter ->
            registerReceiver(connectivityReceiver, filter) // Register receiver
        }
    }

    // Checks permissions and location settings, requests if needed.
    private fun checkPermissionsAndLocation() {
        if (isAllPermissionGranted()) { // Check if all permissions are granted
            if (isLocationEnabled(this)) { // Check if location is enabled
                startLocationUpdates() // Start location updates
            } else {
                showGPSNotEnabledDialog(this) // Show dialog to enable GPS
            }
        } else {
            askForPerm() // Request permissions
        }
    }

    // Updates user data in Firestore.
    private fun updateUserDataInFirestore(
        lat: String? = null,
        long: String? = null,
        batteryPercentageOnly: Boolean = false,
        connectivityOnly: Boolean = false
    ) {
        userData?.let { user ->
            val data = hashMapOf<String, Any>() // Data to update

            if (lat != null && long != null) { // Update location if provided
                data["lat"] = lat
                data["long"] = long
            }
            if (batteryPercentageOnly || (!batteryPercentageOnly && !connectivityOnly)) { // Update battery if needed
                data["batPer"] = batteryPercentage()
            }
            if (connectivityOnly || (!batteryPercentageOnly && !connectivityOnly)) { // Update connection if needed
                data["connectionInfo"] = networkConnected()
            }
            if (!batteryPercentageOnly && !connectivityOnly) { // Update user info
                data["name"] = user.fullName
                data["phoneNumber"] = user.phoneNumber
            }

            if (data.isNotEmpty()) { // Update if data is not empty
                db.collection(Constants.FIRESTORE_COLLECTION)
                    .document(user.email)
                    .set(data, SetOptions.merge()) // Merge with existing data
                    .addOnSuccessListener { }
                    .addOnFailureListener { e -> Log.e("MainActivity", "Firestore update failed", e) }
            }
        }
    }

    // Starts location updates.
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // 1 second interval
            fastestInterval = 500 // 500ms fastest interval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // High accuracy
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return // Return if permission not granted
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()) // Request updates
    }

    // Requests permissions.
    private fun askForPerm() {
        ActivityCompat.requestPermissions(this, permissionArray, permissionCode) // Request permissions
    }

    // Sets up bottom navigation menu.
    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btm_guard -> inflateFragment(ServiceFragment.newInstance()) // Service fragment
                R.id.btm_home -> inflateFragment(HomeFragment.newInstance()) // Home fragment
                R.id.btm_dashboard -> inflateMapFragment() // Map fragment
                R.id.btm_profile -> inflateFragment(ProfileFragment.newInstance()) // Profile fragment
                else -> false // Do nothing for other items
            }
            true
        }
        binding.bottomNav.selectedItemId = R.id.btm_home // Set home as default
    }

    // Inflates specified fragment.
    private fun inflateFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment) // Replace fragment
            .addToBackStack(null) // Add to back stack
            .commit()
    }

    // Inflates MapplsMapFragment.
    private fun inflateMapFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MapplsMapFragment.newInstance(null, null, null, true)) // Replace with map fragment
            .addToBackStack(null) // Add to back stack
            .commit()
    }

    // Handles permission request results.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode && isAllPermissionGranted()) { // Check if all permissions granted
            if (isLocationEnabled(this)) { // Check if location is enabled
                startLocationUpdates() // Start location updates
            } else {
                showGPSNotEnabledDialog(this) // Show dialog to enable GPS
            }
        }
    }

    // Gets current battery percentage.
    private fun batteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) // Get battery level
    }

    // Checks if all permissions are granted.
    private fun isAllPermissionGranted(): Boolean {
        return permissionArray.all { // Check if all permissions are granted
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Gets network connection status.
    private fun networkConnected(): String {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.run { // Get network info
            when (type) {
                ConnectivityManager.TYPE_WIFI -> "Wi-Fi" // Wi-Fi connection
                ConnectivityManager.TYPE_MOBILE -> "Data" // Mobile data connection
                else -> "NA" // Other connection type
            }
        } ?: "Offline" // No connection
    }

    // Checks if location services are enabled.
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) // Check GPS and network providers
    }

    // Shows dialog to enable GPS.
    private fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.enable_gps)) // Dialog title
            .setMessage(context.getString(R.string.required_for_this_app)) // Dialog message
            .setCancelable(false) // Not cancelable
            .setPositiveButton(context.getString(R.string.enable_now)) { _, _ -> // Positive button
                context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS)) // Open location settings
            }
            .show() // Show dialog
    }

    override fun onResume() {
        super.onResume()
        if (isAllPermissionGranted() && isLocationEnabled(this)) {
            startLocationUpdates() // Start location updates on resume
        }
        lastBatteryPercentage = batteryPercentage() // Update battery percentage
        lastConnectionInfo = networkConnected() // Update connection info
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback) // Stop location updates on pause
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver) // Unregister battery receiver
            unregisterReceiver(connectivityReceiver) // Unregister connectivity receiver
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver unregister error", e) // Log error
        }
    }
}
