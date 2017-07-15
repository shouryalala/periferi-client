package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 24/6/17.
 */

public class Zone {
    public String zoneKey;
    public ZonePerimeter zoneBounds;

    public Zone(String key, ZonePerimeter zp) {
        this.zoneKey = key;
        this.zoneBounds = zp;
    }

    public ZonePerimeter getZonePerimeter() {return this.zoneBounds;}

    public String getZoneKey() {return this.zoneKey;}
}
