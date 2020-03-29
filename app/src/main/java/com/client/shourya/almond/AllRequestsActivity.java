package com.client.shourya.almond;

/*
 * Created by shourya on 12/7/17.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.client.shourya.almond.data.ZonePerimeter;
import com.client.shourya.almond.data.ZoneRequest;
import com.client.shourya.almond.view.RequestPagerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
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
    private static int NEEDED_REQUESTS = 10;
    private Button ins;
    protected static boolean flag = false;
    protected static ZoneRequest deleteNode = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_requests);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_requests);
        mapFragment.getMapAsync(this);

        ins = (Button)findViewById(R.id.button_instruction_request);
        ImageButton homeBtn = (ImageButton) findViewById(R.id.request_home_button);

        allZoneRequests = new ArrayList<>();
        coordinates = new ArrayList<>(4);

        for(ZoneRequest zr : currZoneRequests) {        //done as currZoneRequests can keep changing if other users create new zones
            Log.d(DEBUG_TAG, "temp request array content added: " + zr.zName);
            allZoneRequests.add(zr);
        }

        requestPager = (ViewPager)findViewById(R.id.request_pager);
        requestAdapter = new RequestPagerAdapter(getSupportFragmentManager());
        requestPager.setAdapter(requestAdapter);

        requestPager.addOnPageChangeListener(new SwapMapPolygon());

        myLoc = new LatLng(locationDetails.getCurrLatitutde(), locationDetails.getCurrLongitude());

        ins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstructionsDialog ins = new InstructionsDialog();
                Bundle b = new Bundle();
                b.putInt("ins_reqd", 96);       //96 is constant for reqd ins
                ins.setArguments(b);
                ins.show(getFragmentManager(), "InstructionsDialog");
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllRequestsActivity.this, FeedActivity.class));
            }
        });

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
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

        gMap.getUiSettings().setMapToolbarEnabled(false);

        PolygonOptions minRectangle = new PolygonOptions();
        minRectangle.add(coordinates.get(0), coordinates.get(1), coordinates.get(2), coordinates.get(3));
        minRectangle.strokeColor(ContextCompat.getColor(this, R.color.mapOutlineColor));// getResources().getColor(R.color.opaque_red));
        minRectangle.fillColor(ContextCompat.getColor(this, R.color.mapBoxColor));//getResources().getColor(R.color.translucent_red));
        minRectangle.strokeWidth(8);
        mapRequestShape = gMap.addPolygon(minRectangle);

        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json));

        if (!success) {
            Log.d(DEBUG_TAG, "Style parsing failed.");
        }

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
        //Toast.makeText(getApplicationContext(), requestPager.getCurrentItem() + " " + a, Toast.LENGTH_SHORT).show();
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
