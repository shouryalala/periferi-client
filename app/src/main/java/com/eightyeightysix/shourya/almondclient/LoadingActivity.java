package com.eightyeightysix.shourya.almondclient;

import android.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.data.Zone;
import com.eightyeightysix.shourya.almondclient.data.ZonePerimeter;
import com.eightyeightysix.shourya.almondclient.data.ZoneRequest;
import com.eightyeightysix.shourya.almondclient.location.Constants;
import com.eightyeightysix.shourya.almondclient.location.CurrentLocationDetails;
import com.eightyeightysix.shourya.almondclient.location.GPSLocator;
import com.eightyeightysix.shourya.almondclient.location.ReverseGeocodeIntentService;
import com.eightyeightysix.shourya.almondclient.login.LoginActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.SettingsApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 23/5/17.
 */

public class LoadingActivity extends BaseActivity implements GPSLocator.locationFetchedCallback{
    private static final String DEBUG_TAG = "AlmondLog:: " + LoadingActivity.class.getSimpleName();
    DatabaseReference loadChats;
    ValueEventListener loadChatListener;
    protected static String city_id;
    protected static String country_id;
    static boolean temp = false;
    private LocationRequest mLocationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //TODO get location permission before
        //Firebase auth getInstance
        mAuth = FirebaseAuth.getInstance();
        mFireUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!getConnectivityStatus()) {
            toastit("Unable to connect. Please check your network settings");
            finish();
        }

        /*if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
        }*/

        /*
         * Initiate first page as city circle
         * countryCircle = 0
         * cityCircle = 1
         * zoneCircles = 2, 3 sorted according to area
        */
        currCircle = COUNTRY_INDEX;
        //TODO check internet and display message

        if(!preferences.getString("id",UNAVAILABLE).equals(UNAVAILABLE)) {
            //fetchLocation
            //refreshes location and places it in a callback
            locationDetails = new CurrentLocationDetails();
            //callback class for ReverseGeocode
            mResultReceiver = new AddressResultReceiver(new Handler());

            currZoneRequests = new ArrayList<>();
            currZoneRequestKeys = new HashMap<>();

            mLocator = new GPSLocator(this);
            mLocator.refreshLocation();


            mUser = new User(preferences.getString("id", UNAVAILABLE),
                    preferences.getString("first_name", UNAVAILABLE),
                    preferences.getString("last_name", UNAVAILABLE),
                    preferences.getString("short_name", UNAVAILABLE),
                    preferences.getString("email", UNAVAILABLE),
                    preferences.getString("dob", UNAVAILABLE),
                    preferences.getString("gender", UNAVAILABLE),
                    preferences.getString("profileUrl", UNAVAILABLE));

            Map<String, String> paramsUser = new HashMap<String, String>();
            paramsUser.put("userID", mUser.getUserId());
            final String chatReference = substituteString(getResources().getString(R.string.user_chats), paramsUser);
            loadChats = mDatabase.getReference(chatReference);
            loadChatListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendIds = new HashMap<>();
                    if(dataSnapshot.getValue() == null) {
                        Log.d(DEBUG_TAG, "No chats yet");
                    }
                    else {
                        for(DataSnapshot chatSnapShot: dataSnapshot.getChildren()){
                            String chatId = (String)chatSnapShot.getKey();
                            String friendName = (String)chatSnapShot.getValue();
                            friendIds.put(friendName, extractFriendId(chatId)); //Name, ID
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(DEBUG_TAG, "loadChat Cancelled");
                }
            };
            loadChats.addListenerForSingleValueEvent(loadChatListener);


            //Add user to list of online users
            //userOnline();
        }
        else {
            //login
            startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
        }

    }

    //callback from GPSLocator
    @Override
    public void onReceivingCoordinates() {
        Log.d(DEBUG_TAG, "Location Callback called");
        //toastit("Latitude: " + lat + "Longitude: " + lng);
        //initiate reverse geocoding
        startIntentService();
    }


    private void startIntentService() {
        Intent intent = new Intent(this, ReverseGeocodeIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocator.getUpdatedLocation());
        startService(intent);
    }

    public void inspectLocation() {
        //TODO all location coodinates and reverse geocoding has to be done through listeners. IMPORTANT
        //Load saved location variables
        String savedCityName = preferences.getString("city_name", UNAVAILABLE);
        String savedCountryName = preferences.getString("country_name", UNAVAILABLE);
        city_id = preferences.getString("city_ID", UNAVAILABLE);
        country_id = preferences.getString("country_ID", UNAVAILABLE);

        String currentCityName = locationDetails.getAdminAreaName();
        String currentCountryName = locationDetails.getCountryName();

        if(currentCityName == null) {
            Log.d(DEBUG_TAG, "City Name not fetched");
        }

        if(currentCountryName == null) {
            Log.d(DEBUG_TAG, "Country Name not fetched");
        }

        if(!currentCountryName.equals(savedCountryName) || savedCountryName.equals(UNAVAILABLE)) {
            //either changed country(LOL) or entering app for first time
            refreshCountryID(currentCountryName);
            //Log.d(DEBUG_TAG,"City Code is now:" + city_id);
        }

        else if(!currentCityName.equals(savedCityName)) {
            //changed cities
            refreshCityID(currentCityName);
            //Log.d(DEBUG_TAG,"City Code is now:" + city_id);
        }
        else {
            //fetch zones from current city
            refreshZonesList();
        }
    }

    public void refreshCountryID(final String country) {
        Log.d(DEBUG_TAG, "Refreshing country List");
        final String get_countries = substituteString(getResources().getString(R.string.all_countries), new HashMap<String, String>());
        final DatabaseReference countryReference = mDatabase.getReference(get_countries);
        countryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean cExists = false;
                if(dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = (String)ds.getValue();
                        if(name.equals(country)){
                            country_id = ds.getKey();
                            Log.d(DEBUG_TAG, "Country ID: " + country_id);
                            cExists = true;
                        }
                    }
                }
                if(!cExists) {
                    country_id = countryReference.push().getKey();
                    countryReference.child(country_id).setValue(country);
                }
                Log.d(DEBUG_TAG,"Country Code is now:" + country_id);
                refreshCityID(locationDetails.getAdminAreaName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshCityID(final String cityName) {
        Log.d(DEBUG_TAG, "Refreshing City List");
        Map<String, String> params = new HashMap<>();
        params.put("countryID", country_id);
        final String get_city_spec = substituteString(getResources().getString(R.string.get_city), params);
        final DatabaseReference getCityReference = mDatabase.getReference(get_city_spec);
        getCityReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean ctExists = false;
                if(dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if(ds.getValue().equals(cityName)){
                            city_id = ds.getKey();
                            Log.d(DEBUG_TAG, "City ID: " + city_id);
                            ctExists = true;
                        }
                    }
                }
                if(!ctExists) {
                    city_id = getCityReference.push().getKey();
                    getCityReference.child(city_id).setValue(cityName);
                }
                refreshZonesList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshZonesList() {
        Map<String, String> params = new HashMap<>();
        params.put("cityID", city_id);
        final String get_zones_spec = substituteString(getResources().getString(R.string.get_zones), params);
        final DatabaseReference getZoneReference = mDatabase.getReference(get_zones_spec);
        getZoneReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    boolean flag = true;
                    for(DataSnapshot ds: dataSnapshot.getChildren()) {
                        ZonePerimeter zp = ds.getValue(ZonePerimeter.class);
                        if(zp.insideZone(mLocator.getLatitude(), mLocator.getLongitude())){
                            flag = false;
                            Zone newZone = new Zone(ds.getKey(), zp);
                            locationDetails.zonesList.add(newZone);
                        }
                    }
                    if(!flag)locationDetails.setZonesAvailable();       //Zones available
                }
                else{
                    Log.d(DEBUG_TAG, "No zones in this city yet");
                }

                Log.d(DEBUG_TAG,"Zones Found:" + locationDetails.getZonesStatus());
                if(locationDetails.getZonesStatus()) {
                    //set first page as innermost zoneCircle
                    currCircle = 0;
                    sortZoneListByArea();
                }

                //update preferences values;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("city_name", locationDetails.getAdminAreaName());
                editor.putString("country_name", locationDetails.getCountryName());
                editor.putString("city_ID", city_id);
                editor.putString("country_ID", country_id);
                editor.apply();

                locationDetails.setCountryID(country_id);
                locationDetails.setCityID(city_id);

                backgroundRefreshRequestsList();        //refreshes RequestList in the background and updates on addition

                startActivity(new Intent(LoadingActivity.this, FeedActivity.class));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(DEBUG_TAG, "onActivtyResult called");
        //called if location is currently disabled
        mLocator.onDialogResult(requestCode, resultCode);
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from ReverseGeocodeIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle b) {

            // Display the address string or an error message sent from the intent service.
            //mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //toastit("Address: " + mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(DEBUG_TAG, "Address stored successfully");
                //Location fetched, address fetched, now fetch zones
                inspectLocation();
            }
            else {
                Log.d(DEBUG_TAG,"Address not received, location inspection suspended");
            }
        }
    }
}
