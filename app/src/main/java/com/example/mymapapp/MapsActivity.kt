package com.example.mymapapp

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    fun clickSearch(v : View) {

        val address = searchET.text.toString()

        if (address.isNotEmpty()) {
            mMap.setMinZoomPreference(15f)
            val latLng = getLocationByAddress(address)
            displayLocationOnMap(latLng, getAddressByLatLng(latLng))
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
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
