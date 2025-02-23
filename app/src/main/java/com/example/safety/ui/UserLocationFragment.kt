package com.example.safety.ui

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
import com.mappls.sdk.maps.MapplsMap
import com.mappls.sdk.maps.OnMapReadyCallback
import com.mappls.sdk.maps.SupportMapFragment
import com.mappls.sdk.maps.annotations.IconFactory
import com.mappls.sdk.maps.annotations.MarkerOptions
import com.mappls.sdk.maps.camera.CameraPosition
import com.mappls.sdk.maps.geometry.LatLng
import com.mappls.sdk.maps.location.LocationComponentActivationOptions

class UserLocationFragment : Fragment() {

    private lateinit var mapplsMap: MapplsMap

    //    private lateinit var location: LatLng
    private val callback = object : OnMapReadyCallback {
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



                Log.d("Rupam", "Current Location - Lat: ${latitude}, Lng: ${longitude}")
                showLocationWithMarker(
                    latitude,
                    longitude,
                    "current location"
                )

            }
        }

        override fun onMapError(p0: Int, p1: String?) {
            TODO("Not yet implemented")
        }


    }
//        googleMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_user_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        arguments?.let {
            latitude = it.getDouble(ARG_LATITUDE)
            longitude = it.getDouble(ARG_LONGITUDE)
        }
    }

    private fun showLocationWithMarker(lat: Double, lng: Double, markerTitle: String) {
        val point = LatLng(lat, lng)
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
        ).title(markerTitle)

        markerOptions.snippet = "This is a Marker"
        mapplsMap.addMarker(markerOptions)
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

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        private const val ARG_LATITUDE = "arg_latitude"
        private const val ARG_LONGITUDE = "arg_longitude"

        fun newInstance(latitude: Double, longitude: Double): UserLocationFragment {
            val fragment = UserLocationFragment()
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            fragment.arguments = args
            return fragment

        }
//    override fun onItemClick(lat: Double, long: Double) {
//        location = LatLng(lat, long)
//    }
    }
}