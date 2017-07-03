package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 24/6/17.
 */

public class Zone {
    public String zName;
    public double uLat;
    public double uLng;
    public double lLat;
    public double lLng;

    public Zone() {
        //required for firebase
    }

    public Zone(String zName, double uLat, double uLng, double lLat, double lLng) {
        this.zName =  zName;
        this.uLat = uLat;
        this.uLng = uLng;
        this.lLat = lLat;
        this.lLng = lLng;
    }
}
