package com.example.mymapapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymapapp.PermissionManager.Companion.MY_REQUEST_ACCESS_FINE_LOCATION

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

}