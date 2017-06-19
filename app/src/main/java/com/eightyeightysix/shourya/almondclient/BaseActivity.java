package com.eightyeightysix.shourya.almondclient;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Toast;

/*
 * Created by shourya on 31/5/17.
 */

public class BaseActivity extends AppCompatActivity{

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

    private static final int MY_REQUEST_ACCESS_FINE_LOCATION = 69;
    public static boolean permissionLocation = false;
    private static final String DEBUG_TAG = "AlmondLog::" + BaseActivity.class.getSimpleName();

    //gesture
    public static final int INVALID_POINTER = -1;
    public int mDiaPrimary = INVALID_POINTER;
    public int mDiaSecondary = INVALID_POINTER;
    public static Point SCREEN_CENTER;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    PointF primary;
    PointF secondary;

    public GPSLocator mLocator;

    interface permissionsListener{
        public void locationListener();
    }

    permissionsListener pListener;

    public void requestAllPermissions(Context context) {
        pListener = (permissionsListener) context;
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
                pListener.locationListener();
            }
        }
    }

    public void toastit(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    public void getScreenCenter() {
        //TODO not center!
        Display display = getWindowManager().getDefaultDisplay();
        SCREEN_CENTER = new Point();
        display.getSize(SCREEN_CENTER);
        SCREEN_CENTER.x /= 2;
        SCREEN_CENTER.y /= 2;
        Log.d(DEBUG_TAG, "Screen Dimensions = " + SCREEN_CENTER.x + "," + SCREEN_CENTER.y);
    }

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

    public void refreshGesture() {
        mDiaSecondary = INVALID_POINTER;
        mDiaPrimary = INVALID_POINTER;
        primary.set(INVALID_POINTER,INVALID_POINTER);
        secondary.set(INVALID_POINTER, INVALID_POINTER);
    }
}

