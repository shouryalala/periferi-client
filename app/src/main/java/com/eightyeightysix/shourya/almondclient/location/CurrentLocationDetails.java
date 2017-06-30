package com.eightyeightysix.shourya.almondclient.location;

/*
 * Created by shourya on 30/6/17.
 */

import java.util.ArrayList;

public class CurrentLocationDetails {
    private String adminAreaName;
    private String localityName;
    private String pincode;
    private String countryCode;
    private String countryName;
    private String featureName;
    private String subadminAreaName;
    private String countryID;
    private String cityID;
    //All current Zones
    public boolean noZonesAvailable;
    public ArrayList<String> zonesList;

    public CurrentLocationDetails(){
        adminAreaName = null;
        localityName = null;
        pincode = null;
        countryCode = null;
        countryName = null;
        featureName = null;
        subadminAreaName = null;
        countryID = null;
        cityID = null;
        noZonesAvailable = true;
        zonesList = new ArrayList<>();
    }

    public void setValues(String a, String b, String c, String d, String e, String f, String g){
        adminAreaName = a;
        localityName = b;
        pincode = c;
        countryCode = d;
        countryName = e;
        featureName = f;
        subadminAreaName = g;
    }

    public String getAdminAreaName() {return adminAreaName;}

    public String getCountryName() {return countryName;}

    public void setCountryID(String id){
        this.countryID = id;
    }

    public void setCityID(String id) {
        this.cityID = id;
    }

    public void setZonesAvailable(){
        noZonesAvailable = false;
    }

    public boolean getZonesStatus() {return !noZonesAvailable;}
}
