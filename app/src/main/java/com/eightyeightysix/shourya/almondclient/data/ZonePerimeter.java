package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 30/6/17.
 */

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ZonePerimeter {
    private static final String DEBUG_TAG = "AlmondLog:: " + ZonePerimeter.class.getSimpleName();
    public double latMin;
    public double latMax;
    public double lngMin;
    public double lngMax;

    public ZonePerimeter() {
        //Firebase
    }

    public ZonePerimeter(double lMin, double lMax, double gMin, double gMax){
        latMin = lMin;
        latMax = lMax;
        lngMin = gMin;
        lngMax = gMax;
    }

    @Exclude
    public boolean insideZone(double currLat, double currLng) {
        //boolean a = (currLat <= this.latMax && currLat >= this.latMin);
        //boolean b = (currLng <= this.lngMax && currLng >= this.lngMin);
        //return a&b;
        return !(currLat > this.latMax || currLat < this.latMin || currLng > this.lngMax || currLng < this.lngMin);
    }
}
