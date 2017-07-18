package com.eightyeightysix.shourya.almondclient;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.Display;
import android.view.MotionEvent;

import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.data.Zone;
import com.eightyeightysix.shourya.almondclient.data.ZonePerimeter;
import com.eightyeightysix.shourya.almondclient.data.ZoneRequest;
import com.eightyeightysix.shourya.almondclient.location.CurrentLocationDetails;
import com.eightyeightysix.shourya.almondclient.location.GPSLocator;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public static FirebaseDatabase mDatabase;

    public static DatabaseReference almondRequests;
    public static ValueEventListener requestsRefresh;

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

    public static final int CITY_INDEX = 69;
    public static final int COUNTRY_INDEX = 420;

    public static Bitmap userProfilePic;
    private static ProgressDialog progressDialog;

    private final static String DEBUG_TAG = "AlmondLog:: " + BaseActivity.class.getSimpleName();
    private static final int MY_REQUEST_ACCESS_FINE_LOCATION = 69;

    //gesture
/*    public static final int INVALID_POINTER = -1;
    public int mDiaPrimary = INVALID_POINTER;
    public int mDiaSecondary = INVALID_POINTER;
    public static Point SCREEN_CENTER;
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    PointF primary;
    PointF secondary;
    ////////////
*/
    public static boolean permissionLocation = true;


    //All user Details
    public static User mUser;

    //Current ChatUser - no need to take import all user data
    public static User mChatBuddy;
    public static Map<String, String> friendIds;        //Name, ID

    public static CurrentLocationDetails locationDetails;   //stores current location details
    public static ArrayList<ZoneRequest> currZoneRequests;
    public static HashMap<ZoneRequest, String> currZoneRequestKeys;
    public static boolean zoneRequestsPresent = false;


    public static int currCircle;

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
                    toastit("Sorry, Almond cant function without Location");
                    //TODO tell the user that this functionality is a core necessity for the functioning of the application
                }
            }
        }
    }

    public static String substituteString(String template, Map<String, String> substitutions) {
        String result = template;
        for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
            String pattern = "{" + substitution.getKey() + "}";
            result = result.replace(pattern, substitution.getValue());
        }
        return result;
    }


    public void toastit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static void setDefaults(String id, String fname, String lname, String sname, String email) {
        userId = id;
        userName = fname + " " + lname;
        if(sname != null)displayName = sname;
        else
            displayName = fname;
        userEmail = email;
    }

    public static String getChatID(String id1, String id2) {
        if(id1.compareTo(id2)>0){
            return (id1 + "_" + id2);
        }
        else
            return (id2 + "_" + id1);
    }

    public String extractFriendId(String chatId) {
        String myId = mUser.getUserId();
        if(myId != null) {
            String[] allIds = chatId.split("_",2);
            if(allIds[0].equals(myId))
                return allIds[1];
            else
                return allIds[0];
        }
        Log.d(DEBUG_TAG, "No user found!!");
        return null;
    }

    public void userOnline() {
        String uID = mUser.getUserId();
        Map<String, String> paramsCountry = new HashMap<>();
        paramsCountry.put("countryID", locationDetails.getCountryID());
        paramsCountry.put("userID", uID);

        Map<String, String> paramsCity = new HashMap<>();
        paramsCity.put("cityID", locationDetails.getCityID());
        paramsCity.put("userID", uID);

        final String onlineCountryRef = substituteString(getResources().getString(R.string.add_online_country), paramsCountry);
        final String onlineCityRef = substituteString(getResources().getString(R.string.add_online_city), paramsCity);

        mDatabase.getReference(onlineCountryRef).setValue(true);
        mDatabase.getReference(onlineCityRef).setValue(true);
        Log.d(DEBUG_TAG, "Added User Online");
        if(locationDetails.getZonesStatus()) {
            Map<String, String> paramsZone = new HashMap<>();
            for(Zone z: locationDetails.zonesList) {
                String s = z.getZoneKey();
                paramsZone.clear();
                paramsZone.put("zoneID", s);
                paramsZone.put("userID", uID);
                String ref = substituteString(getResources().getString(R.string.add_online_zone), paramsZone);
                mDatabase.getReference(ref).setValue(true);
            }
        }
    }

    /*
    public void userOnlineStatusRefresh(int circle) {
        currCircle = circle;
        toastit("Current Circle " + circle);
    }
*/

    //for pinch circle initialization
    public static int fetchCurrentCircleCount() {
        if(locationDetails.getZonesStatus()){
            return (2 + locationDetails.zonesList.size());
        }
        return 2;
    }

    public static HashMap<Integer, String> getZoneNames() {
        HashMap<Integer, String> names = new HashMap<>();
        int size = fetchCurrentCircleCount();
        names.put(size-1, locationDetails.getCountryName());
        names.put(size-2, locationDetails.getAdminAreaName());
        if(size > 2) {
            for(int i= 0; i<locationDetails.zonesList.size(); i++) {
                names.put(i, locationDetails.zonesList.get(i).zoneBounds.getZoneName());
            }
        }
        return names;
    }

    public static void sortZoneListByArea() {
        Log.d(DEBUG_TAG, "sorting all zones by area");

        String debug1 = "";
        for(Zone a : locationDetails.zonesList) {
            debug1 = debug1.concat(a.zoneBounds.getZoneName());
        }
        Log.d(DEBUG_TAG, "Before sorting: " + debug1);

        if(locationDetails.zonesList.size() > 1) {
            Collections.sort(locationDetails.zonesList, new Comparator<Zone>() {
                @Override
                public int compare(Zone z1, Zone z2) {
                    return (int)(getZoneArea(z1.zoneBounds) - getZoneArea(z2.zoneBounds));
                }
            });
        }
        String debug = "";
        for(Zone a : locationDetails.zonesList) {
            debug = debug.concat(a.zoneBounds.getZoneName());
        }
        Log.d(DEBUG_TAG, "After sorting: " + debug);
    }

    public void userOffline() {
        String uID = mUser.getUserId();
        Map<String, String> paramsCountry = new HashMap<>();
        paramsCountry.put("countryID", locationDetails.getCountryID());
        paramsCountry.put("userID", uID);

        Map<String, String> paramsCity = new HashMap<>();
        paramsCity.put("cityID", locationDetails.getCityID());
        paramsCity.put("userID", uID);

        final String onlineCountryRef = substituteString(getResources().getString(R.string.add_online_country), paramsCountry);
        final String onlineCityRef = substituteString(getResources().getString(R.string.add_online_city), paramsCity);

        mDatabase.getReference(onlineCountryRef).removeValue();
        mDatabase.getReference(onlineCityRef).removeValue();

        if(locationDetails.getZonesStatus()) {
            Map<String, String> paramsZone = new HashMap<>();
            for(Zone z: locationDetails.zonesList) {
                String s = z.getZoneKey();
                paramsZone.clear();
                paramsZone.put("zoneID", s);
                paramsZone.put("userID", uID);
                String ref = substituteString(getResources().getString(R.string.add_online_zone), paramsZone);
                mDatabase.getReference(ref).removeValue();
            }
        }

    }

    public static void showProgressDialog(String message, Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public boolean getConnectivityStatus() {
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    public void backgroundRefreshRequestsList() {
        //create almondzonerequests reference
        HashMap<String, String> params = new HashMap<>();
        params.put("cityID", locationDetails.getCityID());
        final String get_requests = substituteString(getResources().getString(R.string.all_zone_requests), params);
        almondRequests = mDatabase.getReference(get_requests);
        requestsRefresh = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_TAG, "Fetching Requests");
                currZoneRequests.clear();
                currZoneRequestKeys.clear();
                if(dataSnapshot != null) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ZoneRequest z = ds.getValue(ZoneRequest.class);
                        if(z.insideZone(mLocator.getLatitude(), mLocator.getLongitude())){
                            Log.d(DEBUG_TAG, "Found Zone Requests: " + z.toString());
                            currZoneRequests.add(z);
                            currZoneRequestKeys.put(z,ds.getKey());
                            zoneRequestsPresent = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        almondRequests.addValueEventListener(requestsRefresh);
    }
    //TODO incorrect/jugad code. should use child event listener and callback to this array when temp array is updated
    /*
      requestsRefresh = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(DEBUG_TAG, "Fetching Requests");
                if(dataSnapshot != null) {
                    //for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        ZoneRequest z = dataSnapshot.getValue(ZoneRequest.class);
                        if(z.insideZone(mLocator.getLatitude(), mLocator.getLongitude())){
                            Log.d(DEBUG_TAG, "Found Zone Requests: " + z.toString());
                            currZoneRequests.add(z);
                            currZoneRequestKeys.put(z,dataSnapshot.getKey());
                            zoneRequestsPresent = true;
                        }
                    //}
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                ZoneRequest z = dataSnapshot.getValue(ZoneRequest.class);

            }

        };
        almondRequests.addChildEventListener(requestsRefresh);

     */

    public static void removeRequestsRefresher() {
        almondRequests.removeEventListener(requestsRefresh);
    }

    public static List<LatLng> coordinateToList(ZonePerimeter zp) {
        List<LatLng> coordinates = new ArrayList<>(4);
        coordinates.add(new LatLng(zp.latMax, zp.lngMin));
        coordinates.add(new LatLng(zp.latMax, zp.lngMax));
        coordinates.add(new LatLng(zp.latMin, zp.lngMax));
        coordinates.add(new LatLng(zp.latMin, zp.lngMin));

        return coordinates;
    }


    //area calculation methods
    public static double getZoneArea(ZonePerimeter zonePerimeter) {
        List<LatLng> edgeCoordinates = coordinateToList(zonePerimeter);
        final int ZONE_EDGES = 4;
        double radius = 6371009.0D;
        double total = 0.0D;
        LatLng prev = (LatLng)edgeCoordinates.get(ZONE_EDGES - 1);
        double prevTanLat = Math.tan((1.5707963267948966D - Math.toRadians(prev.latitude)) / 2.0D);
        double prevLng = Math.toRadians(prev.longitude);

        double lng;
        for(Iterator var11 = edgeCoordinates.iterator(); var11.hasNext(); prevLng = lng) {
            LatLng point = (LatLng)var11.next();
            double tanLat = Math.tan((1.5707963267948966D - Math.toRadians(point.latitude)) / 2.0D);
            lng = Math.toRadians(point.longitude);
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
        }
        total = total * radius * radius;
        return Math.abs(total);
    }

    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2.0D * Math.atan2(t * Math.sin(deltaLng), 1.0D + t * Math.cos(deltaLng));
    }

}

