package com.eightyeightysix.shourya.almondclient;

import android.os.Bundle;

/*
 * Created by shourya on 31/5/17.
 */
          
/*
 * Created by shourya on 23/5/17.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;

import com.eightyeightysix.shourya.almondclient.login.LoginActivity;

public class LoadingActivity extends BaseActivity implements BaseActivity.permissionsListener {
    protected SharedPreferences mSettings;
    protected SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocator = new GPSLocator(this);
        requestAllPermissions(this);
        
         mSettings = getPreferences(Context.MODE_PRIVATE);
        String defaultName = "Unknown";
        String name = mSettings.getString("Name",defaultName);
        Intent login = new Intent(LoadingActivity.this, LoginActivity.class);

        if(name.equals(defaultName))
            startActivity(login);
    }

    @Override
    protected void onStart() {
        mLocator.connectClient();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mLocator.disconnectClient();
        super.onStop();
    }

    @Override
    public void locationListener() {
        if(permissionLocation) {
            mLocator.getLocation();
            toastit("Latitude: " + mLocator.getLatitude() + "\nLongitude: " + mLocator.getLongitude());
        }
        else {
            toastit("Almond cant function without Location");
        }
    }
}
