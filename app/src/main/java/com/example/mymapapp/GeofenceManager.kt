package com.example.mymapapp

import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

class GeofenceManager(latLng: LatLng) {


    private var geofenceList = ArrayList<Geofence>()

    init {
        geofenceList.add(
            Geofence.Builder()
                .setRequestId("REQUEST_ID")
                .setCircularRegion(
                    latLng.latitude,
                    latLng.longitude,
                    Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())

        Log.d("TAG_GeofenceList", geofenceList.toString())
    }

    fun getGeofencingRequest(): GeofencingRequest =
         GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()


}