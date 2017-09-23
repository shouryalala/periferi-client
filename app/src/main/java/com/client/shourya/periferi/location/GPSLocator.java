package com.client.shourya.periferi.location;

/*
 * Created by shourya on 31/5/17.
 */

import android.app.Activity;
import android.content.IntentSender;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.client.shourya.periferi.BaseActivity;
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
        void onReceivingCoordinates();
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
                    Log.d(DEBUG_TAG, "Location received: " + mLastLocation.toString() + ", callback called");
                    BaseActivity.locationDetails.setCoordinates(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    callback.onReceivingCoordinates();
                }
                else{
                    Log.d(DEBUG_TAG, "Task Failed");
                    //TODO please improve this code
                    //when the location is disabled on a device, the locationsettingsrequest is called which enables gps and calls this method.
                    //But this does not immedietely enable the location an thus updateLocation's unsuccessful task gets called several times before execution
                    updateLocation();
                    canGetLocation = false;
                }
            }
        });
    }

    public Location getUpdatedLocation() {
        return mLastLocation;
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
                        Toast.makeText(mActivity, "Please switch location on and restart Periferi", Toast.LENGTH_SHORT).show();
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
        return mLastAddress.getLocality();
    }

    public String getCountryName() {
        return mLastAddress.getCountryName();
    }

}
