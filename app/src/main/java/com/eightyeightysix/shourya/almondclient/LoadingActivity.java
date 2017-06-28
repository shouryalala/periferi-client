package com.eightyeightysix.shourya.almondclient;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.login.LoginActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/*
 * Created by shourya on 23/5/17.
 */

public class LoadingActivity extends BaseActivity implements GPSLocator.locationFetchedCallback{
    private static final String DEBUG_TAG = "AlmondLog:: " + LoadingActivity.class.getSimpleName();
    DatabaseReference loadChats;
    ValueEventListener loadChatListener;
    String city_id;
    String country_id;
    static boolean temp = false;
    private LocationRequest mLocationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //TODO get location permission before
        //fetch location instantiation
        //Firebase auth getInstance
        mAuth = FirebaseAuth.getInstance();
        mFireUser = mAuth.getCurrentUser();
        //Get database Reference
        mDatabase = FirebaseDatabase.getInstance();
        //refreshes location and places it in a callback
        mLocator = new GPSLocator(this);
        mLocator.refreshLocation();

        if(mFireUser != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mUser = new User(preferences.getString("id", UNAVAILABLE),
                    preferences.getString("first_name", UNAVAILABLE),
                    preferences.getString("last_name", UNAVAILABLE),
                    preferences.getString("short_name", UNAVAILABLE),
                    preferences.getString("email", UNAVAILABLE),
                    preferences.getString("dob", UNAVAILABLE),
                    preferences.getString("gender", UNAVAILABLE));

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
            userOnline();

            //inspectLocation();

        }
        else {
            Intent login = new Intent(LoadingActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }

    @Override
    public void getCoordinates(double lat, double lng) {
        Log.d(DEBUG_TAG, "Location Callback called");
        toastit("Latitude: " + lat + "Longitude: " + lng);
        Intent feed = new Intent(LoadingActivity.this, FeedActivity.class);
        startActivity(feed);
    }

    public void inspectLocation() {
        //TODO all location coodinates and reverse geocoding has to be done through listeners. IMPORTANT
        //Location mCurrent = mLocator.updateLocation();
        //Load saved location variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedCityName = preferences.getString("city_name", UNAVAILABLE);
        String savedCountryName = preferences.getString("country_name", UNAVAILABLE);
        city_id = preferences.getString("city_ID", UNAVAILABLE);
        country_id = preferences.getString("city_ID", UNAVAILABLE);

/*
        if(mCurrent == null) {
            Log.d(DEBUG_TAG, "Location not fetched");
        }*/
        //mLocator.updateLocationAddress(mCurrent.getLatitude(), mCurrent.getLongitude());

        String currentCityName = mLocator.getCityName();
        String currentCountryName = mLocator.getCountryName();

        if(currentCityName == null) {
            Log.d(DEBUG_TAG, "City Name not fetched");
        }

        if(currentCountryName == null) {
            Log.d(DEBUG_TAG, "Country Name not fetched");
        }

        if(!currentCountryName.equals(savedCountryName) || savedCountryName.equals(UNAVAILABLE)) {
            //either changed country(LOL) or entering app for first time
            refreshCountry(currentCountryName);
            Log.d(DEBUG_TAG,"Country Code is now:" + country_id);
            refreshCity(currentCityName);
        }

        else if(!currentCityName.equals(savedCityName) && currentCountryName.equals(savedCountryName)) {
            //changed cities
            refreshCity(currentCityName);
        }

        else {
            //fetch zones from current city

        }
    }

    public void refreshCountry(final String country) {
        final String get_countries = substituteString(getResources().getString(R.string.all_countries), new HashMap<String, String>());
        final DatabaseReference countryReference = mDatabase.getReference(get_countries);
        countryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean cExists = false;
                if(dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = (String)ds.child("name").getValue();
                        if(name.equals(country)){
                            country_id = ds.getKey();
                            Log.d(DEBUG_TAG, "Country ID: " + country_id);
                            cExists = true;
                        }
                    }
                }
                if(!cExists) {
                    country_id = countryReference.push().getKey();
                    countryReference.child(country_id).child("name").setValue(country);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshCity(final String cityName) {
        Map<String, String> params = new HashMap<>();
        params.put("countryID", country_id);
        final String country_spec = substituteString(getResources().getString(R.string.get_country), params);
        final DatabaseReference getCountry = mDatabase.getReference(country_spec);
        getCountry.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    city_id = getCountry.push().getKey();
                    getCountry.child(city_id).setValue(cityName);
                }
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
        mLocator.onDialogResult(requestCode, resultCode);

    }
}
