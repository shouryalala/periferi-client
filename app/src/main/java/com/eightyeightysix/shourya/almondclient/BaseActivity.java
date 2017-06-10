package com.eightyeightysix.shourya.almondclient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Created by shourya on 31/5/17.
 */

public class BaseActivity extends AppCompatActivity{
    public static String userId;
    public static String userName;
    public static String displayName;
    public static String userEmail;
    public static boolean mLocationRequestReturned = true;
    public static final String UNAVAILABLE = "error404";
    public GPSLocator mLocator;
    //Firebase
    public static FirebaseAuth mAuth;
    public static FirebaseUser mFireUser;
   // public static FirebaseDatabase mDatabase;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final int SECONDS = 1;
    private static final int MINUTES = 60 * SECONDS;
    private static final int HOURS = 60 * MINUTES;
    private static final int DAYS = 24 * HOURS;
    private static final int MONTHS = 30 * DAYS;
    private static final int YEARS = 365 * DAYS;

    private final static String DEBUG_TAG = "AlmondLog:: " + BaseActivity.class.getSimpleName();
    private static final int MY_REQUEST_ACCESS_FINE_LOCATION = 69;
    public static boolean permissionLocation = false;

    //location callback for later - tutorials
    interface permissionsListener{
        public void locationListener();
    }

    //permissionsListener pListener;

    public void requestAllPermissions(Context context) {
        //pListener = (permissionsListener) context;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case MY_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionLocation = true;
                }
                else {
                    permissionLocation = false;
                    //TODO tell the user that this functionality is a core necessity for the functioning of the application
                }
                locationListener(mLocator);
            }
        }
    }

    public void toastit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static void setDefaults(String id, String fname, String lname, String sname, String email) {
        userId = id;
        userName = fname + " " + lname;
        if(sname != null)displayName = sname;
        else
            displayName = fname;
        userEmail = email;
    }

    public void locationListener(GPSLocator locate) {
        mLocationRequestReturned = true;
        if(permissionLocation) {
            locate.getLocation();
            toastit("Latitude: " + locate.getLatitude() + "\nLongitude: " + locate.getLongitude());
        }
        else {
            toastit("Almond cant function without Location");
        }
    }

}
