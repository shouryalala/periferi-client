package com.eightyeightysix.shourya.almondclient;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.Zone;
import com.eightyeightysix.shourya.almondclient.data.ZonePerimeter;
import com.eightyeightysix.shourya.almondclient.data.ZoneRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class RequestZoneActivity extends BaseActivity implements OnMapReadyCallback,
                                                                    GoogleMap.OnMapClickListener,
                                                                    GoogleMap.OnMapLongClickListener,
                                                                    GoogleMap.OnMarkerDragListener,
                                                                    NewZoneRequestDialog.zoneRequestCallback{
    //TODO save instance so as to load it when there is a change in the screen
    private static final String DEBUG_TAG = "AlmondLog:: " + RequestZoneActivity.class.getSimpleName();
    private GoogleMap mMap;
    private static final double minLongitudeShift = 0.002;
    private static final double minLatitudeShift = -0.002;
    private static MarkerOptions coordinates[];
    private static LatLng a,b,c,d,t_a,t_b,t_c,t_d;
    private List<LatLng> points;
    private List<Marker> markers;
    private Polygon zonePolygon, zoneUpdatePolygon;
    private boolean longClick = false;
    private Button refresh, ins;
    private LatLng myLoc;

    //TODO figure out values for MAX and MIN
    private static final double MAX_ZONE_AREA= 9000000;
    private static final double MIN_ZONE_AREA= 10000;
    private static final int ZONE_EDGES = 4;
    private static boolean zoneAcceptedByRequests = true;
    private static boolean zoneAcceptedByZones = true;
    private String zoneConflict = null;
    private static double lMin, lMax, gMin, gMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_zone);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ins = (Button)findViewById(R.id.button_instruction_create);
        ins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstructionsDialog ins = new InstructionsDialog();
                Bundle b = new Bundle();
                b.putInt("ins_reqd", 69);       //69 is constant for reqd ins
                ins.setArguments(b);
                ins.show(getFragmentManager(), "InstructionsDialog");
            }
        });

        refresh = (Button) findViewById(R.id.button_refresh);
        //accept = (Button) findViewById(R.id.button_accept);

        //Floating Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.map_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRequest();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longClick = true;
                mMap.clear();
                points.clear();
                markers.clear();
                mMap.addMarker(new MarkerOptions().position(myLoc).title("Current Location"));
                displayCurrentZones();
            }
        });

        //current location
        myLoc = new LatLng(locationDetails.getCurrLatitutde(), locationDetails.getCurrLongitude());
        //invert status bar color
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        longClick = true;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 17));
        mMap.addMarker(new MarkerOptions().position(myLoc).title("Current Location"));

        coordinates = new MarkerOptions[4];
        points = new ArrayList<>(4);
        markers = new ArrayList<>(4);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);

        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_json));
        if (!success) {
            Log.d(DEBUG_TAG, "Style parsing failed.");
        }

        displayCurrentZones();
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    public void displayCurrentZones() {
        if(!locationDetails.zonesList.isEmpty()) {
            for(int i=0; i<locationDetails.zonesList.size(); i++) {
                ZonePerimeter zp = locationDetails.zonesList.get(i).getZonePerimeter();
                PolygonOptions po = new PolygonOptions();
                po.add(new LatLng(zp.latMax,zp.lngMin), new LatLng(zp.latMax, zp.lngMax), new LatLng(zp.latMin, zp.lngMax), new LatLng(zp.latMin, zp.lngMin));
                po.strokeColor(ContextCompat.getColor(this, R.color.mapOutlineColor));
                po.strokeWidth(5);
                po.fillColor(Color.argb(15,255,0,0));
                Polygon p = mMap.addPolygon(po);
                p.setTag(zp.getZoneName());
                p.setClickable(true);
            }
            mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
                @Override
                public void onPolygonClick(Polygon polygon) {
                    Toast.makeText(getApplicationContext(), "Periferi: " + polygon.getTag(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    public void createMinRectangle(LatLng coordinate) {
        PolygonOptions minRectangle = new PolygonOptions();
        PolygonOptions mintempRectangle = new PolygonOptions();

        //double originLat = coordinate.latitude;
        //double originLong = coordinate.longitude;
        double originLat = myLoc.latitude - (minLatitudeShift/2);
        double originLng = myLoc.longitude - (minLongitudeShift/2);

        a = new LatLng(originLat, originLng);
        b = new LatLng(originLat, originLng + minLongitudeShift);
        c = new LatLng(originLat + minLatitudeShift, originLng + minLongitudeShift);
        d = new LatLng(originLat + minLatitudeShift, originLng);

        points.add(a);
        points.add(b);
        points.add(c);
        points.add(d);

        coordinates[0] = new MarkerOptions().position(a).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.almond_marker));
        coordinates[1] = new MarkerOptions().position(b).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.almond_marker));
        coordinates[2] = new MarkerOptions().position(c).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.almond_marker));
        coordinates[3] = new MarkerOptions().position(d).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.almond_marker));

        minRectangle.add(a,b,c,d);
        minRectangle.strokeColor(ContextCompat.getColor(this, R.color.mapOutlineColor));// getResources().getColor(R.color.opaque_red));
        //minRectangle.fillColor(ContextCompat.getColor(this, R.color.mapBoxColor));//getResources().getColor(R.color.translucent_red));
        minRectangle.fillColor(Color.argb(200,255,235,238));
        minRectangle.strokeWidth(8);

        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot(), new Gap(20), new Dash(30), new Gap(20));
        mintempRectangle.add(a,b,c,d);
        mintempRectangle.strokeColor(ContextCompat.getColor(this, R.color.mapBoxColor));
        mintempRectangle.strokeWidth(8);
        mintempRectangle.strokePattern(pattern);

        zonePolygon = mMap.addPolygon(minRectangle);
        zoneUpdatePolygon = mMap.addPolygon(mintempRectangle);
    }

    public void updateShape(Marker marker) {
        int point = (Integer)marker.getTag();
        LatLng x = marker.getPosition();
        switch(point) {
            case 0: {
                points.set(0, x);
                points.set(1, new LatLng(x.latitude, points.get(1).longitude));
                points.set(3, new LatLng(points.get(3).latitude, x.longitude));
                break;
            }
            case 1: {
                points.set(0, new LatLng(x.latitude, points.get(0).longitude));
                points.set(1, x);
                points.set(2, new LatLng(points.get(2).latitude, x.longitude));
                break;
            }
            case 2: {
                points.set(1, new LatLng(points.get(1).latitude, x.longitude));
                points.set(2, x);
                points.set(3, new LatLng(x.latitude, points.get(3).longitude));
                break;
            }
            case 3: {
                points.set(0, new LatLng(points.get(0).latitude, x.longitude));
                points.set(2, new LatLng(x.latitude, points.get(2).longitude));
                points.set(3, x);
                break;
            }
            default: {
                Log.d(DEBUG_TAG, "Going to default");
            }
        }
        zoneUpdatePolygon.setPoints(points);
        updateMarkers();
    }

    public void updateMarkers() {
        markers.get(0).setPosition(points.get(0));
        markers.get(1).setPosition(points.get(1));
        markers.get(2).setPosition(points.get(2));
        markers.get(3).setPosition(points.get(3));
    }

    public void placeMarkers() {
        Marker a = mMap.addMarker(coordinates[0]);
        Marker b = mMap.addMarker(coordinates[1]);
        Marker c = mMap.addMarker(coordinates[2]);
        Marker d = mMap.addMarker(coordinates[3]);

        a.setTag(0);
        b.setTag(1);
        c.setTag(2);
        d.setTag(3);

        markers.add(a);
        markers.add(b);
        markers.add(c);
        markers.add(d);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(longClick) {
            longClick = false;
            //TODO calculate diagonal coordinate based on the fact that minimum area can be (?)
            createMinRectangle(latLng);
            placeMarkers();
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        t_a = points.get(0);
        t_b = points.get(1);
        t_c = points.get(2);
        t_d = points.get(3);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(DEBUG_TAG,"onMarkerDrag");
        updateShape(marker);
    }

    public void areaViolation() {
        points.set(0, t_a);
        points.set(1, t_b);
        points.set(2, t_c);
        points.set(3, t_d);
        zoneUpdatePolygon.setPoints(points);
        updateMarkers();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        double area = computeZoneArea(points);
        if(area > MAX_ZONE_AREA){
           toastit("Periferi area too great");
            areaViolation();
        }
        else if(area < MIN_ZONE_AREA) {
            toastit("Periferi area too small");
            areaViolation();
        }
        else if(points.get(0).latitude < myLoc.latitude || points.get(0).longitude > myLoc.longitude ||
                points.get(2).latitude > myLoc.latitude || points.get(2).longitude < myLoc.longitude) {
            toastit("You can only create a Periferi around your location");
            areaViolation();
        }
        else {
            t_a = points.get(0);
            t_b = points.get(1);
            t_c = points.get(2);
            t_d = points.get(3);

            zonePolygon.setPoints(points);
        }
    }

    //area calculation methods
    public static double computeZoneArea(List<LatLng> edgeCoordinates) {
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

    private void submitRequest() {
        zoneAcceptedByRequests = true;
        zoneAcceptedByZones = true;
        if(zonePolygon  == null || markers.isEmpty() || points.isEmpty()){
            toastit("Create a Periferi first");
        }
        else{
            //check already created zones and requested zones
            showProgressDialog("Checking for Conflicts", this);
            lMin = points.get(2).latitude;
            lMax = points.get(0).latitude;
            gMin = points.get(0).longitude;
            gMax = points.get(2).longitude;

            checkForConflicts();
        }
    }

    //callback on receiving data
    public void checkForConflicts() {
        //first check already created Requests
        for(ZoneRequest zr : currZoneRequests) {
            if(!zr.getFactor(lMin, lMax, gMin, gMax)) {
                zoneAcceptedByRequests = false;
                zoneConflict = zr.getzName();
                break;
            }
        }

        //if no conflict, check existing zones
        if(zoneAcceptedByRequests) {
            for (Zone z : locationDetails.zonesList) {
                ZonePerimeter zeus = z.zoneBounds;
                if(zeus.getFactor(lMin, lMax, gMin, gMax) < 4.0) {
                    zoneAcceptedByZones = false;
                    zoneConflict = zeus.getZoneName();
                }
            }
        }
        Log.d(DEBUG_TAG, "ZoneAcceptedByRequests: " + zoneAcceptedByRequests +
        "ZoneAcceptedByZones: " + zoneAcceptedByZones);
        if(zoneAcceptedByRequests && zoneAcceptedByZones) {
            //get zoneRequestname
            DialogFragment dialog = new NewZoneRequestDialog();
            dialog.show(getFragmentManager(), "NewZoneRequestDialog");
        }
        else{
            toastit("A similar Periferi already exists: " + zoneConflict);
        }
        dismissProgressDialog();
    }

    //callback from dialog
    @Override
    public void onSubmit(String name) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cityID", locationDetails.getCityID());
        final String get_requests = substituteString(getResources().getString(R.string.all_zone_requests), params);
        final DatabaseReference req = mDatabase.getReference(get_requests);

        ZoneRequest request = new ZoneRequest(mUser.getUserId(), name, lMin, lMax, gMin, gMax);
        currZoneRequests.add(request);
        String key = req.push().getKey();
        req.child(key).setValue(request);
        currZoneRequestKeys.put(request, key);
        toastit("Great! Periferi request created!");
        toastit("Check out the requests tab to monitor its status");
    }
}
