package com.example.jura.muvisitor4;


import java.util.ArrayList;
import java.util.List;

import com.example.jura.muvisitor4.objects.RouteNode;
import com.example.jura.muvisitor4.util.ProjectConstant;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


public class GeofenceTransitionsIntentService extends IntentService{

    protected static final String TAG = "geofence-transitions";

    public GeofenceTransitionsIntentService() {
        super("name");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Toast.makeText(this, intent.getStringExtra("kuy"), Toast.LENGTH_LONG).show();
        String x  = intent.getStringExtra(MainActivity.ROUTE_NODE_INTENT);
        RouteNode rNode = (RouteNode) intent.getParcelableExtra (MainActivity.ROUTE_NODE_INTENT);

        Log.e(TAG, "OnHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
//            String errorMessage = GeoFenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
            Log.e(TAG, "Geofence Error");
            return;
        }
        Log.e("DEBUG", "No geofencingEvent error");

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.e("DEBUG", "getGeofenceTransition has been called");
        // Test that the reported transition was of interest.

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.e("DEBUG", "Geofence Transition enter or exit");
            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails,
                    intent.getIntExtra(MainActivity.ROUTE_ID_INTENT, 0),
                    intent.getIntExtra(MainActivity.NODE_ID_INTENT, 0),
                    intent.getIntExtra(MainActivity.NODE_NUMBER_INTENT, 0));
            Log.i(TAG, geofenceTransitionDetails);

            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ){
                intentToMapsActivity(true);
            }
            else
                intentToMapsActivity(false);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        Log.e("DEBUG", "getGeofenceTransitionDetails");
        String geofenceTransitionString = getTransitionString(geofenceTransition);
        Toast.makeText(getBaseContext(), geofenceTransitionString, Toast.LENGTH_LONG).show();

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            String place = geofence.getRequestId();
            triggeringGeofencesIdsList.add(place);
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return triggeringGeofencesIdsString  + ": " + geofenceTransitionString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails, int routeId, int nodeId, int nodeNumber) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
        notificationIntent.putExtra(MainActivity.ROUTE_ID_INTENT, Integer.toString(routeId));
        notificationIntent.putExtra(MainActivity.NODE_ID_INTENT, Integer.toString(nodeId));
        notificationIntent.putExtra(MainActivity.NODE_NUMBER_INTENT, Integer.toString(nodeNumber));

        Log.e("DEBUG", "sendNotification");

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapsActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.icon_photoloco)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.icon_photoloco))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent)
                .setVibrate(new long[] { 0, 200, 500, 500, 100 })
                .setLights(Color.CYAN, 3000, 3000);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        Log.e("DEBUG", "getTransitionString");
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
    private void intentToMapsActivity(boolean inTheArea){
        Intent localIntent =
                new Intent(ProjectConstant.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(ProjectConstant.EXTENDED_DATA_STATUS, inTheArea);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}