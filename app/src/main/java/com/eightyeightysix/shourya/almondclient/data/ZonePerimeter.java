package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 30/6/17.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class ZonePerimeter {
    private static final String DEBUG_TAG = "AlmondLog:: " + ZonePerimeter.class.getSimpleName();
    public double latMin;
    public double latMax;
    public double lngMin;
    public double lngMax;
    public String zoneName;

    public ZonePerimeter() {
        //Firebase
    }
    //put zoneName and zone description  in a different table later
    public ZonePerimeter(double lMin, double lMax, double gMin, double gMax, String name){
        latMin = lMin;
        latMax = lMax;
        lngMin = gMin;
        lngMax = gMax;
        zoneName = name;
    }

    @Exclude
    public boolean insideZone(double currLat, double currLng) {
        //boolean a = (currLat <= this.latMax && currLat >= this.latMin);
        //boolean b = (currLng <= this.lngMax && currLng >= this.lngMin);
        //return a&b;
        return !(currLat > this.latMax || currLat < this.latMin || currLng > this.lngMax || currLng < this.lngMin);
    }

    @Exclude
    public double getFactor(double lMin, double lMax, double gMin, double gMax) {
        //think
        double factor = ((double)10000/(double)3);
        double a = Math.abs(latMax-lMax);
        double b = Math.abs(latMin-lMin);
        double c = Math.abs(lngMax-gMax);
        double d = Math.abs(lngMin-gMin);
        return (a+b+c+d)*factor;
    }

    @Exclude
    public String getZoneName() {return this.zoneName;}

}
