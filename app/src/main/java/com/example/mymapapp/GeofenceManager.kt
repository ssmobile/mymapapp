package com.example.mymapapp

import android.location.Location
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class GeofenceManager(location : Location) {


    private var geofenceList = ArrayList<Geofence>()

    init {
        geofenceList.add(
            Geofence.Builder()
                .setRequestId("REQUEST_ID")
                .setCircularRegion(
                    location.latitude,
                    location.longitude,
                    Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
        )

    }

    fun getGeofencingRequest() =
         GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()


}