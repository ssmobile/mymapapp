package com.example.mymapapp

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.drawable.DrawableCompat
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapTypes.setOnCheckedChangeListener { radioGroup, id ->
            val radioButton = radioGroup?.findViewById<RadioButton>(id)
            val index = radioGroup.indexOfChild(radioButton)
            Log.d("TAG_MapType", "$id")
            Log.d("TAG_MapType", "${radioGroup.indexOfChild(radioButton)}")
            mMap.mapType = index

            val color = if (index == 2 || index == 4) {
                Color.parseColor("#FFFFFF")
            } else {
                Color.parseColor("#000000")
            }


            for (i in 0 until radioGroup.size) {
                (radioGroup[i] as RadioButton).setTextColor(color)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val usa = LatLng(38.063741, -95.534281)
        mMap.setMinZoomPreference(3.3f)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(usa))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions[0]==Manifest.permission.ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG_PermissionResult", "Has been granted")
                locateDevice()
            } else {
                Toast.makeText(this,
                    "Location permissions required to display your location",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clickSearch(v : View) {

        val address = searchET.text.toString()

        if (address.isNotEmpty()) {
            mMap.setMinZoomPreference(15f)
            val latLng = getLocationByAddress(address)
            displayLocationOnMap(latLng, getAddressByLatLng(latLng))
        }
    }

    fun onLocationFABClick(v : View) {
        val manager = PermissionManager(this)
        if (manager.checkForPermission()) {
            Log.d("TAG_Permission", "Has been granted")
            if (manager.locationServicesEnabled()) {
                locateDevice()
            } else {
                manager.requestLocationServices()
            }
        }
    }

    private fun locateDevice() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val locationResult = mFusedLocationProviderClient.lastLocation

        locationResult.addOnCompleteListener {
            Log.d("TAG_locateUser","onCompleteListener: ${it.isSuccessful}")
            if (it.isSuccessful && it.result != null) {
                val lastKnownLocation = it.result!!
                mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                        14f))
            } else {
                Log.e("TAG_locateUser", "${it.exception}")
            }
        }
    }

    private fun getLocationByAddress(address : String) : LatLng {
        val geocoder = Geocoder(this)
        val addressResult = geocoder.getFromLocationName(address, 1)[0]

        return LatLng(addressResult.latitude, addressResult.longitude)
    }

    private fun getAddressByLatLng(latLng: LatLng) : String {
        val geocoder = Geocoder(this)
        return geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)[0].toString()
    }


    private fun displayLocationOnMap(latLng: LatLng, title : String) {
        mMap.addMarker(MarkerOptions()
            .position(latLng)
            .title(title)
            .icon(BitmapDescriptorFactory.fromBitmap(
                getBitmapFromVectorDrawable(this, R.drawable.ic_beenhere_24dp))
            ))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)

        return bitmap
    }
}
