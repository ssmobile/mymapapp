package com.example.mymapapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.google.android.gms.location.*
import com.example.mymapapp.PermissionManager.OnPermissionResultCallback


class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback, OnPermissionResultCallback {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1
        const val CHANNEL_ID = "GEOFENCE_NOTIFICATION_CHANNEL"
    }

    private lateinit var mMap: GoogleMap
    private lateinit var lastSearchAddressLatLng : LatLng
    private lateinit var permissionManager : PermissionManager
    private lateinit var locationManager : LocationManager
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager : GeofenceManager

    override fun onStart() {
        super.onStart()
        createNotificationChannel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        configureUI()
        permissionManager = PermissionManager(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        removeGeofences()
    }


    private fun configureUI() {

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapTypes.check(R.id.normalMap)
        mapTypes.setOnCheckedChangeListener { radioGroup, id ->
            val radioButton = radioGroup?.findViewById<RadioButton>(id)
            val index = radioGroup.indexOfChild(radioButton)
            mMap.mapType = index

            val color = if (index == 2 || index == 4) {
                Color.parseColor("#FFFFFF")
            } else {
                Color.parseColor("#000000")
            }

            searchET.setTextColor(color)

            for (i in 0 until radioGroup.size) {
                (radioGroup[i] as RadioButton).setTextColor(color)
            }
        }

    }

    private fun addGeofences() {
        geofencingClient.addGeofences(geofenceManager.getGeofencingRequest(),
            geofencePendingIntent)?.run {
                addOnSuccessListener {
                    Log.d("TAG_addGeofences", "success")
                }

                addOnFailureListener {
                    Log.e("TAG_addGeofences", "failure", it)
                }
            }
    }

    private fun removeGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d("TAG_removeGeofences", "success")
            }

            addOnFailureListener {
                Log.e("TAG_removeGeofences", "failure", it)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val usa = LatLng(38.063741, -95.534281)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(usa, 3.3f))
        locationManager = LocationManager(mMap, this)
        locationManager.getLocationUpdates(1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionManager.getPermissionResults(requestCode, permissions, grantResults)
    }

    fun clickSearch(v : View) {

        val address = searchET.text.toString()

        if (address.isNotEmpty()) {
            lastSearchAddressLatLng = locationManager.getLocationByAddress(address)
            locationManager.displayLocationOnMap(lastSearchAddressLatLng,
                locationManager.getAddressByLatLng(lastSearchAddressLatLng))
        }
    }

    fun onLocationFABClick(v : View) {
        if (permissionManager.checkForPermission()) {
            Log.d("TAG_Permission", "Has been granted")

            if (permissionManager.locationServicesEnabled()) {
                locationManager.locateDevice()
//                locationManager.getLocationUpdates(100)
                if (::lastSearchAddressLatLng.isInitialized) {
                    geofenceManager = GeofenceManager(lastSearchAddressLatLng)
                    addGeofences()
                }

            } else {
                permissionManager.requestLocationServices()
            }
        }
    }

    fun onDirectionsClick(v : View) {

        if (locationManager.isLastKnownLocationInitialized()) {
            Log.d("TAG_onDirectionsClick", "clicked")
            val lastKnownLocation = locationManager.lastKnownLocation
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
        "http://maps.google.com/maps?" +
                "saddr=${lastKnownLocation.latitude},${lastKnownLocation.longitude}&" +
                "daddr=${lastSearchAddressLatLng.latitude},${lastSearchAddressLatLng.longitude}"
                )
            )
            startActivity(intent)
        } else {
            onLocationFABClick(v)
        }
    }


    private val geofencePendingIntent : PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        Log.d("TAG_PendingIntent", "by Lazy")
        PendingIntent
            .getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.description_text)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
                .apply { description = descriptionText }
            val notificationManager : NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }




    override fun onPermissionGranted() {
        locationManager.locateDevice()
    }

    override fun onPermissionDenied() {
        Toast.makeText(
            this,
            "Location permissions required to display your location",
            Toast.LENGTH_SHORT
        ).show()
    }
}
