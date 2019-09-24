package com.example.mymapapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private var context: Context) {


    companion object {
        const val MY_REQUEST_ACCESS_FINE_LOCATION = 867
    }

    fun checkForPermission() : Boolean {
        return if (ContextCompat
                .checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermission()
            false
        } else {
            true
        }

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_REQUEST_ACCESS_FINE_LOCATION)
    }

    fun locationServicesEnabled() : Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    fun requestLocationServices() {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setMessage("Location Services not enabled")
            .setPositiveButton("Enable GPS") { _, _ ->
                (context as Activity).startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    MapsActivity.REQUEST_CHECK_SETTINGS, Bundle()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}