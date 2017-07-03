package com.eightyeightysix.shourya.almondclient.fcm;

/*
 * Created by shourya on 20/6/17.
 */

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AlmondFirebaseMessagingService extends FirebaseMessagingService {
    private static final String DEBUG_TAG = "AlmondLog:: " + AlmondFirebaseMessagingService.class.getSimpleName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(DEBUG_TAG, "From: " + remoteMessage.getFrom());
        Log.d(DEBUG_TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
