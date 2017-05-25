package com.eightyeightysix.shourya.almondclient;

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

public class LoadingActivity extends AppCompatActivity{
    protected SharedPreferences mSettings;
    protected SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView();

        mSettings = getPreferences(Context.MODE_PRIVATE);
        String defaultName = "Unknown";
        String name = mSettings.getString("Name",defaultName);
        Intent login = new Intent(LoadingActivity.this, LoginActivity.class);

        if(name.equals(defaultName))
            startActivity(login);

        else {

        }
    }
}
