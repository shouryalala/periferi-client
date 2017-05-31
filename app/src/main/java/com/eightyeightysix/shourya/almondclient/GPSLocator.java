package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 31/5/17.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPSLocator implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private final Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean connected = false;
    private boolean canGetLocation = false;

    private double latitude;
    private double longitude;

    public GPSLocator(Context context) {
        mContext = context;
        //instantiate GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void connectClient() {
        mGoogleApiClient.connect();
    }

    public void disconnectClient() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        connected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        connected = false;
        //TODO
    }

    public boolean CanGetLocation() {
        return this.canGetLocation;
    }


    public Location getLocation() {
        if(!connected) {
            canGetLocation = false;
            return null;
        }
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            canGetLocation = false;
            return null;
        }
        canGetLocation = true;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return mLastLocation;
    }

    public double getLatitude() {
        if(mLastLocation != null) {
            latitude =  mLastLocation.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if(mLastLocation != null) {
            longitude = mLastLocation.getLongitude();
        }
        return longitude;
    }

}
