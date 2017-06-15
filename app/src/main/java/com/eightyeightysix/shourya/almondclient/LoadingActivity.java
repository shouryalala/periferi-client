package com.eightyeightysix.shourya.almondclient;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 23/5/17.
 */

public class LoadingActivity extends BaseActivity {
    private static final String DEBUG_TAG = "AlmondLog:: " + LoadingActivity.class.getSimpleName();
    DatabaseReference loadChats;
    ValueEventListener loadChatListener;
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
            mUser = new User(preferences.getString("id", UNAVAILABLE),
                        preferences.getString("first_name", UNAVAILABLE),
                        preferences.getString("last_name", UNAVAILABLE),
                        preferences.getString("short_name", UNAVAILABLE),
                        preferences.getString("email", UNAVAILABLE),
                        preferences.getString("dob", UNAVAILABLE),
                        preferences.getString("gender", UNAVAILABLE));

            Map<String, String> params = new HashMap<>();
            params.put("userID", mUser.getUserId());
            final String reference = substituteString(getResources().getString(R.string.user_chats), params);
            loadChats = mDatabase.getReference(reference);
            loadChatListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    friendIds = new HashMap<>();
                    if(dataSnapshot.getValue() == null) {
                        Log.d(DEBUG_TAG, "No chats yet");
                    }
                    else {
                        for(DataSnapshot chatSnapShot: dataSnapshot.getChildren()){
                            String chatId = (String)chatSnapShot.getKey();
                            String friendName = (String)chatSnapShot.getValue();
                            friendIds.put(friendName, extractFriendId(chatId)); //Name, ID
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(DEBUG_TAG, "loadChat Cancelled");
                }
            };
            loadChats.addListenerForSingleValueEvent(loadChatListener);

            Intent feed = new Intent(LoadingActivity.this, FeedActivity.class);
            startActivity(feed);
        }
        else {
             Intent login = new Intent(LoadingActivity.this, LoginActivity.class);
             startActivity(login);
        }
    }

}
