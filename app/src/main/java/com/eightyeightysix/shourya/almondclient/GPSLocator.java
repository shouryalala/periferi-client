package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 31/5/17.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Locale;


public class GPSLocator{
    private static final String DEBUG_TAG = "AlmondLog:: " + GPSLocator.class.getSimpleName();
    private final Activity mActivity;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private boolean canGetLocation = false;
    private Address mLastAddress = null;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static FusedLocationProviderClient mFusedLocationClient;

    private double latitude;
    private double longitude;
    private locationFetchedCallback callback;

    public interface locationFetchedCallback{
        void getCoordinates(double lat, double lng);
    }

    public GPSLocator(Activity activity) {
        mActivity = activity;
        callback = (locationFetchedCallback)mActivity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    @SuppressWarnings("MissingPermission")
    public void updateLocation(){
        mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task1) {
                //Log.d(DEBUG_TAG, "Inside on Complete + " + task1.getResult().toString());
                if(task1.isSuccessful() && task1.getResult()!= null) {
                    mLastLocation = task1.getResult();
                    canGetLocation = true;
                    Log.d(DEBUG_TAG, "Location received, callback called");
                    callback.getCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
                else{
                    Log.d(DEBUG_TAG, "Task Failed");
                    //TODO please improve this code
                    updateLocation();
                    canGetLocation = false;
                }
            }
        });
    }

    public void refreshLocation() {
        Log.d(DEBUG_TAG, "refreshLocation");
        //check Location Settings first
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient setingsClient = LocationServices.getSettingsClient(mActivity);
        Task<LocationSettingsResponse> task = setingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //updateLocation
                Log.d(DEBUG_TAG, "Settings in order");
                updateLocation();
            }
        });
        task.addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException)e).getStatusCode();
                switch(statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try{
                            Log.d(DEBUG_TAG, "Dialog called");
                            ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                            resolvableApiException.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                            //refreshLocation();
                        }catch (IntentSender.SendIntentException ex) {
                            //bleh
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //settings not satisfied but cant use a dialog
                        Toast.makeText(mActivity, "Please switch on location on our device", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    public void onDialogResult(int reqCode, int resCode) {
        Log.d(DEBUG_TAG, "Dialog result called");
        switch (reqCode)
        {
            case REQUEST_CHECK_SETTINGS:
                switch (resCode)
                {
                    case Activity.RESULT_OK:
                    {
                        Log.d(DEBUG_TAG,"time to update location");
                        updateLocation();
                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        Toast.makeText(mActivity, "Please switch on location manually for almond", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
        }
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
/*
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
*/
    public String getCityName() {
        return mLastAddress.getAdminArea();
    }

    public String getCountryName() {
        return mLastAddress.getCountryName();
    }

}
