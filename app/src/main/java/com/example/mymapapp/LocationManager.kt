package com.example.mymapapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class LocationManager(private val map : GoogleMap, private val context: Context) {


    private var locationProvider : FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    lateinit var lastKnownLocation : Location



    fun isLastKnownLocationInitialized() = ::lastKnownLocation.isInitialized

    fun locateDevice() {
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        val locationResult = locationProvider.lastLocation

        locationResult.addOnCompleteListener {
            Log.d("TAG_locateUser","onCompleteListener: ${it.isSuccessful}")
            if (it.isSuccessful && it.result != null) {
                lastKnownLocation = it.result!!
                map.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(
                            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                            14f))
                Log.d("TAG_locateUser", "${it.result}")
            } else {
                Log.e("TAG_locateUser", "${it.exception}")
            }
        }

    }

    fun getLocationByAddress(address : String) : LatLng {
        val geocoder = Geocoder(context)
        val addressResult = geocoder.getFromLocationName(address, 1)[0]

        return LatLng(addressResult.latitude, addressResult.longitude)
    }

    fun getAddressByLatLng(latLng: LatLng) : String {
        val geocoder = Geocoder(context)
        return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)[0].toString()
    }

    fun displayLocationOnMap(latLng: LatLng, title : String) {
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        getBitmapFromVectorDrawable(context))
                ))

        map.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(Constants.GEOFENCE_RADIUS_IN_METERS.toDouble())
                .strokeColor(Color.BLUE)
                .fillColor(Color.BLUE)
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        (context as MapsActivity).directionsBTN.visibility = View.VISIBLE
    }

    private fun getBitmapFromVectorDrawable(context: Context): Bitmap {
        var drawable = ContextCompat.getDrawable(context, R.drawable.ic_beenhere_24dp)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun getLocationRequest(numOfUpdates : Int) : LocationRequest {
        val request = LocationRequest()
        request.maxWaitTime = 5
        request.interval = 3
        request.numUpdates = numOfUpdates
        request.smallestDisplacement = 1f
        return request
    }

    fun getLocationUpdates(numOfUpdates: Int) {
        val locationRequest = getLocationRequest(numOfUpdates)
        val settingsRequest =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()

        val settingsClient = SettingsClient(context)
        settingsClient.checkLocationSettings(settingsRequest)
        locationProvider.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                val location = result?.locations?.get(0)
                val lat = location?.latitude
                val long = location?.longitude
                Log.d("TAG_onLocationResult", "$lat, $long")

                locationProvider.lastLocation.addOnSuccessListener {
                    Log.d("TAG_Last Location", "${it.latitude}, ${it.longitude}")
                }
            }

        }, Looper.myLooper())


    }


}