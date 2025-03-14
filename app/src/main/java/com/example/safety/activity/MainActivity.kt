package com.example.safety.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
 *
 * This is the main dashboard/home screen of the application after login.
 * It provides access to core features of the Safety App.
 */
/**
 * MainActivity
 *
 * This is the main dashboard/home screen of the application after login.
 * It provides access to core features of the Safety App.
 * Enhanced with real-time updates for location, battery status, and connectivity.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // View binding for UI components
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var batteryReceiver: BroadcastReceiver
    private lateinit var connectivityReceiver: BroadcastReceiver
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPref: SharedPrefFile
    private var userData: UserModelItem? = null
    private var lastBatteryPercentage = -1
    private var lastConnectionInfo = ""

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

        // Initialize Firebase and SharedPreferences
        db = FirebaseFirestore.getInstance()
        sharedPref = SharedPrefFile
        sharedPref.init(this)
        userData = sharedPref.getUserData(Constants.SP_USERDATA)

        // Initialize location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

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

        // Create location callback
        setupLocationCallback()

        // Setup battery and connectivity receivers
        setupBatteryReceiver()
        setupConnectivityReceiver()

        // Check for permissions and location settings
        if (isAllPermissionGranted()) {
            if (isLocationEnabled(this)) {
                startLocationUpdates() // Start location updates
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
     * Sets up the location callback for receiving location updates
     */
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.d("LocationData", "Received location update")

                for (location in locationResult.locations) {
                    updateUserDataInFirestore(
                        location.latitude.toString(),
                        location.longitude.toString()
                    )
                }
            }
        }
    }

    /**
     * Sets up battery status receiver
     */
    private fun setupBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentBatteryPercentage = batteryPercentage()
                if (currentBatteryPercentage != lastBatteryPercentage) {
                    lastBatteryPercentage = currentBatteryPercentage
                    updateUserDataInFirestore(batteryPercentageOnly = true)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
    }

    /**
     * Sets up connectivity status receiver
     */
    private fun setupConnectivityReceiver() {
        connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val currentConnectionInfo = networkConnected()
                if (currentConnectionInfo != lastConnectionInfo) {
                    lastConnectionInfo = currentConnectionInfo
                    updateUserDataInFirestore(connectivityOnly = true)
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }
        registerReceiver(connectivityReceiver, filter)
    }

    /**
     * Updates user data in Firestore
     */
    private fun updateUserDataInFirestore(
        lat: String? = null,
        long: String? = null,
        batteryPercentageOnly: Boolean = false,
        connectivityOnly: Boolean = false
    ) {
        userData?.let { user ->
            val mail = user.email
            Log.d("FirestoreUpdate", "Updating data for user: $mail")

            val data = hashMapOf<String, Any>()

            // Only update what's changed to minimize Firestore writes
            if (lat != null && long != null) {
                data["lat"] = lat
                data["long"] = long
                Log.d("FirestoreUpdate", "Location update: $lat, $long")
            }

            if (batteryPercentageOnly || (!batteryPercentageOnly && !connectivityOnly)) {
                data["batPer"] = batteryPercentage()
                Log.d("FirestoreUpdate", "Battery update: ${data["batPer"]}")
            }

            if (connectivityOnly || (!batteryPercentageOnly && !connectivityOnly)) {
                data["connectionInfo"] = networkConnected()
                Log.d("FirestoreUpdate", "Connectivity update: ${data["connectionInfo"]}")
            }

            // Always include user info
            if (!batteryPercentageOnly && !connectivityOnly) {
                data["name"] = user.fullName
                data["phoneNumber"] = user.phoneNumber
            }

            if (data.isNotEmpty()) {
                db.collection(Constants.FIRESTORE_COLLECTION)
                    .document(mail)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("FirestoreUpdate", "Successfully updated data")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpdate", "Failed to update data", e)
                    }
            }
        }
    }

    /**
     * Starts location updates with high accuracy and frequency
     */
    private fun startLocationUpdates() {
        Log.d("LocationData", "Starting location updates")

        val locationRequest = LocationRequest().apply {
            interval = 1000 // Update interval set to 1 second
            fastestInterval = 500 // Fastest update interval 500ms
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LocationData", "Permission check failed")
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionCode) {
            if (isAllPermissionGranted()) {
                Log.d("LocationData", "All permissions granted")
                if (isLocationEnabled(this)) {
                    startLocationUpdates()
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
            if (ActivityCompat.checkSelfPermission(
                    this,
                    items
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
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

    override fun onResume() {
        super.onResume()
        if (isAllPermissionGranted() && isLocationEnabled(this)) {
            startLocationUpdates()
        }
        // Get initial values
        lastBatteryPercentage = batteryPercentage()
        lastConnectionInfo = networkConnected()
    }

    override fun onPause() {
        super.onPause()
        // Stop location updates when app is in background to save battery
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister receivers
        try {
            unregisterReceiver(batteryReceiver)
            unregisterReceiver(connectivityReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering receivers", e)
        }
    }
}