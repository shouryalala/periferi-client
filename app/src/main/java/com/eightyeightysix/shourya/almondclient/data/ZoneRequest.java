package com.eightyeightysix.shourya.almondclient.data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 4/7/17.
 */

public class ZoneRequest {
    private static final String DEBUG_TAG = "AlmondLog:: " + ZonePerimeter.class.getSimpleName();
    public double latMin;
    public double latMax;
    public double lngMin;
    public double lngMax;
    public String creator;
    public Map<String, Boolean> reqCount = new HashMap<>();

    public ZoneRequest() {
        //Firebase
    }

    public ZoneRequest(String c, double lMin, double lMax, double gMin, double gMax){
        creator = c;
        latMin = lMin;
        latMax = lMax;
        lngMin = gMin;
        lngMax = gMax;
    }

    @Exclude
    public double getFactor(double lMin, double lMax, double gMin, double gMax) {
        //think
        return ((latMax/lMax)+(latMin/lMin)+(lngMax/gMax)+(lngMin/gMin));
    }
}
