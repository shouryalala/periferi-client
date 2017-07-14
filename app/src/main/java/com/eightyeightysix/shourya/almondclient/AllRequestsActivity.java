package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 12/7/17.
 */

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.ZonePerimeter;
import com.eightyeightysix.shourya.almondclient.data.ZoneRequest;
import com.eightyeightysix.shourya.almondclient.view.RequestPagerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AllRequestsActivity extends BaseActivity implements OnMapReadyCallback,
                                                        RequestPagerView.requestCallback {
    private GoogleMap gMap;
    private ViewPager requestPager;
    private RequestPagerAdapter requestAdapter;
    private ArrayList<ZoneRequest> allZoneRequests;
    private LatLng myLoc;
    private Polygon mapRequestShape;
    private List<LatLng> coordinates;
    private static boolean polygonCreated;
    private static final String DEBUG_TAG = "AlmondLog:: " + AllRequestsActivity.class.getSimpleName();
    private static int NEEDED_REQUESTS = 2;

    protected static boolean flag = false;
    protected static ZoneRequest deleteNode = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_requests);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_requests);
        mapFragment.getMapAsync(this);

        allZoneRequests = new ArrayList<>();
        coordinates = new ArrayList<>(4);

        for(ZoneRequest zr : currZoneRequests) {        //done as currZoneRequests can keep changing if other users create new zones
            allZoneRequests.add(zr);
        }

        requestPager = (ViewPager)findViewById(R.id.request_pager);
        requestAdapter = new RequestPagerAdapter(getSupportFragmentManager());
        requestPager.setAdapter(requestAdapter);

        requestPager.addOnPageChangeListener(new SwapMapPolygon());

        myLoc = new LatLng(locationDetails.getCurrLatitutde(), locationDetails.getCurrLongitude());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 15));
        gMap.addMarker(new MarkerOptions().position(myLoc).title("Current Location"));
        polygonCreated = false;

        ZoneRequest currView = allZoneRequests.get(0);
        coordinates.add(new LatLng(currView.latMax, currView.lngMin));
        coordinates.add(new LatLng(currView.latMax, currView.lngMax));
        coordinates.add(new LatLng(currView.latMin, currView.lngMax));
        coordinates.add(new LatLng(currView.latMin, currView.lngMin));


        PolygonOptions minRectangle = new PolygonOptions();
        minRectangle.add(coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3));
        minRectangle.strokeColor(Color.BLUE);// getResources().getColor(R.color.opaque_red));
        minRectangle.fillColor(Color.CYAN);//getResources().getColor(R.color.translucent_red));
        minRectangle.strokeWidth(12);
        mapRequestShape = gMap.addPolygon(minRectangle);
    }

    private class SwapMapPolygon implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Log.d(DEBUG_TAG, "onPageSelected Called: Position: " + position);
            ZoneRequest currView = allZoneRequests.get(position);

            coordinates.clear();
            coordinates.add(new LatLng(currView.latMax, currView.lngMin));
            coordinates.add(new LatLng(currView.latMax, currView.lngMax));
            coordinates.add(new LatLng(currView.latMin, currView.lngMax));
            coordinates.add(new LatLng(currView.latMin, currView.lngMin));
            mapRequestShape.setPoints(coordinates);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }


    private class RequestPagerAdapter extends FragmentStatePagerAdapter {
        public RequestPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            RequestPagerView fragment = new RequestPagerView();
            Bundle b = new Bundle();
            if(allZoneRequests.get(position).requests.containsKey(mUser.getUserId())) {
                b.putBoolean("responded", true);
            }
            else{
                b.putBoolean("responded", false);
            }
            b.putString("name", allZoneRequests.get(position).zName);
            b.putInt("requestCount", (NEEDED_REQUESTS - allZoneRequests.get(position).reqCount));
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        public int getCount() {
            return allZoneRequests.size();
        }
    }

    //callback from onClick view
    @Override
    public void onRequestClick(boolean a) {
        Toast.makeText(getApplicationContext(), requestPager.getCurrentItem() + " " + a, Toast.LENGTH_SHORT).show();
        final boolean response = a;
        final String key = currZoneRequestKeys.get(allZoneRequests.get(requestPager.getCurrentItem()));
        HashMap<String, String> params = new HashMap<>();
        params.put("cityID", locationDetails.getCityID());
        params.put("requestID", key);
        String ref = substituteString(getResources().getString(R.string.get_zone_request), params);
        final DatabaseReference reference = mDatabase.getReference(ref);
        reference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ZoneRequest zUpdate = mutableData.getValue(ZoneRequest.class);
                 if(zUpdate == null) {
                     return Transaction.success(mutableData);
                 }
                 if(!zUpdate.requests.containsKey(mUser.getUserId())) {
                     zUpdate.requests.put(mUser.getUserId(), response);
                     if(response){
                         zUpdate.reqCount++;
                         Log.d(DEBUG_TAG, "Request Count: " + zUpdate.reqCount);
                         if(zUpdate.reqCount == NEEDED_REQUESTS){
                             flag = true;
                             deleteNode = zUpdate;
                             Log.d(DEBUG_TAG, "Create Zone");
                         }
                     }
                 }
                 else{
                     //shouldnt be called
                 }
                 mutableData.setValue(zUpdate);
                Log.d(DEBUG_TAG, "Flag Value: " + flag);
                if(flag) {
                    createZone(deleteNode, key);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public void createZone(ZoneRequest zr, String requestKey) {
        Log.d(DEBUG_TAG, "Create Zone Called");
        Log.d(DEBUG_TAG, "Request Key to be Deleted: " + requestKey);
        ZonePerimeter newZone = new ZonePerimeter(zr.latMin, zr.latMax, zr.lngMin, zr.lngMax, zr.getzName());

        HashMap<String, String> params = new HashMap<>();
        params.put("cityID", locationDetails.getCityID());
        String s = substituteString(getResources().getString(R.string.get_zones), params);
        String t = substituteString(getResources().getString(R.string.all_zone_requests),params);

        final DatabaseReference createZone = mDatabase.getReference(s);
        final DatabaseReference deleteRequest = mDatabase.getReference(t);

        createZone.push().setValue(newZone);
        deleteRequest.child(requestKey).removeValue();
        Log.d(DEBUG_TAG, "Removed request and added zone");
    }
}
