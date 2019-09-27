package com.example.mymapapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage = geofencingEvent.errorCode
            Log.e("TAG_GeoFenceBR", "$errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences)


            if (context != null) {
                createNotification(context, geofenceTransition)
            }

            Log.i("TAG_OnReceive", geofenceTransitionDetails)


        } else {
            Log.e("TAG_OnReceive", "invalid type $geofenceTransition")
        }

    }

    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {

        val geofenceTransitionString = getTransitionString(geofenceTransition)

        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        return "$geofenceTransitionString: $triggeringGeofencesIdsString"
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Geofence transition entered"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Geofence transition exit"
            else -> "Unknown geofence transition"
        }
    }

    private fun createNotification(context: Context, geofenceTransition: Int) {

        val contentText = if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
        { "Entering geofence" } else { "Leaving geofence" }

        val builder = NotificationCompat.Builder(context, MapsActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Geofence")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }

        Log.d("TAG_Notification", "notification")
    }

}