package com.eightyeightysix.shourya.almondclient;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eightyeightysix.shourya.almondclient.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Created by shourya on 23/5/17.
 */

public class LoadingActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //Firebase auth getInstance
        mAuth = FirebaseAuth.getInstance();
        mFireUser = mAuth.getCurrentUser();
        //Get database Reference
        mDatabase = FirebaseDatabase.getInstance();

        if(mFireUser != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            setDefaults(preferences.getString("id", UNAVAILABLE),
                        preferences.getString("first_name", UNAVAILABLE),
                        preferences.getString("last_name", UNAVAILABLE),
                        preferences.getString("short_name", UNAVAILABLE),
                        preferences.getString("email", UNAVAILABLE));
            Intent feed = new Intent(LoadingActivity.this, FeedActivity.class);
            startActivity(feed);
        }
        else {
             Intent login = new Intent(LoadingActivity.this, LoginActivity.class);
             startActivity(login);
        }
    }

}
