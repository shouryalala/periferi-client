package com.eightyeightysix.shourya.almondclient;

import android.os.Bundle;

/*
 * Created by shourya on 31/5/17.
 */

public class LoadingActivity extends BaseActivity implements BaseActivity.permissionsListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocator = new GPSLocator(this);
        requestAllPermissions(this);
        getScreenCenter();
        setContentView(R.layout.activity_feed);
    }

    @Override
    protected void onStart() {
        mLocator.connectClient();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mLocator.disconnectClient();
        super.onStop();
    }

    @Override
    public void locationListener() {
        if(permissionLocation) {
            mLocator.getLocation();
            toastit("Latitude: " + mLocator.getLatitude() + "\nLongitude: " + mLocator.getLongitude());
        }
        else {
            toastit("Almond cant function without Location");
        }
    }
}
