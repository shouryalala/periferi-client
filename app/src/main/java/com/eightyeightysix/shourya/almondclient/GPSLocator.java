package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 31/5/17.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Locale;

public class GPSLocator implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String DEBUG_TAG = "AlmondLog:: " + GPSLocator.class.getSimpleName();
    private final Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean connected = false;
    private boolean canGetLocation = false;
    private Address mLastAddress = null;

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

    public Location updateLocation() {
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

    public void updateLocationAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try{
            mLastAddress = geocoder.getFromLocation(lat,lng,1).get(0);
        }catch (IOException e){
            Log.d(DEBUG_TAG, "geocode fucked up");
            e.printStackTrace();
        }catch(IllegalArgumentException e) {
            Log.d(DEBUG_TAG, "geocode fucked up");
            e.printStackTrace();
        }
        if(mLastAddress == null) {
            Log.d(DEBUG_TAG, "NULL invocation of mLastAddress");
        }
    }

    public String getCityName() {
        return mLastAddress.getAdminArea();
    }

    public String getCountryName() {
        return mLastAddress.getCountryName();
    }

}
