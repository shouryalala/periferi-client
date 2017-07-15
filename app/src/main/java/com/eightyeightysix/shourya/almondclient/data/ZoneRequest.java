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
    public String zName;
    public int reqCount = 0;
    public Map<String, Boolean> requests = new HashMap<>();

    public ZoneRequest() {
        //Firebase
    }

    public ZoneRequest(String c, String name, double lMin, double lMax, double gMin, double gMax){
        creator = c;
        zName = name;
        latMin = lMin;
        latMax = lMax;
        lngMin = gMin;
        lngMax = gMax;
    }

    @Exclude
    public double getFactor(double lMin, double lMax, double gMin, double gMax) {
        //TODO incorrect factor calculation
        double factor = ((double)10000/(double)3);
        double a = Math.abs(latMax-lMax);
        double b = Math.abs(latMin-lMin);
        double c = Math.abs(lngMax-gMax);
        double d = Math.abs(lngMin-gMin);
        return (a+b+c+d)*factor;
    }

    @Exclude
    public boolean insideZone(double currLat, double currLng) {
        return !(currLat > this.latMax || currLat < this.latMin || currLng > this.lngMax || currLng < this.lngMin);
    }

    @Exclude
    public String getzName() {return zName;}
}
