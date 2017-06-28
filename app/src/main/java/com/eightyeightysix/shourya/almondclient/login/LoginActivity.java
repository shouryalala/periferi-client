package com.eightyeightysix.shourya.almondclient.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.BaseActivity;
import com.eightyeightysix.shourya.almondclient.FeedActivity;
import com.eightyeightysix.shourya.almondclient.GPSLocator;
import com.eightyeightysix.shourya.almondclient.LoadingActivity;
import com.eightyeightysix.shourya.almondclient.R;
import com.eightyeightysix.shourya.almondclient.data.User;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 22/5/17.
 */

public class LoginActivity extends BaseActivity implements
        LoginFragmentOne.FragmentOneListener,
        LoginFragmentFour.FragmentFourListener{

    private final static String DEBUG_TAG = "AlmondLog:: " + LoginActivity.class.getSimpleName();
    private LoginFragmentOne firstFragment;
    private static final int NUM_PAGES = 2;
    private NonSwipeableViewPager nPager;
    private PagerAdapter mPagerAdapter;
    public static String fUid;
    Context mContext;
    private static String tId, tFname, tLname, tGender, tSname, tEmail, tDob;
    DatabaseReference create_user;
    ValueEventListener userListener;
    User temp_user;
    Thread t = null;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        nPager = (NonSwipeableViewPager) findViewById(R.id.pager2);

        if (findViewById(R.id.fragment_container) != null) {
                mPagerAdapter = new LoginSlidePagerAdapter(getSupportFragmentManager());
                nPager.setAdapter(mPagerAdapter);
        }

        //request Location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions(this);
        }
    }
    @Override
    public void onBackPressed() {
        if (nPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            nPager.setCurrentItem(nPager.getCurrentItem() - 1);
        }
    }


    @Override
    public void fblistener(String token, String id1, String fname1, String lname1, String sname1,
                           String gender1, String email1, boolean emailAvailable, String dob1,
                           boolean dobAvailable) {
        Log.d(DEBUG_TAG, "fbListener Callback called");
        if(!emailAvailable) {
            //TODO add email fragment
        }
        if(!dobAvailable) {
            nPager.setCurrentItem(3);
        }

        //set data to local variables
        tId = id1;
        tFname = fname1;
        tLname = lname1;
        tSname = sname1;
        tGender = gender1;
        tEmail = email1;
        tDob = dob1;

        //Firebase Auth
        fireBaseAuthenticate(token);

    }

    private void fireBaseAuthenticate(String token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        Log.d(DEBUG_TAG, "Firebase Credentials: " + credential.toString());
        Log.d(DEBUG_TAG, "Firebase Auth Instance: " + mAuth.toString());
      //  Log.d(DEBUG_TAG, "Firebase User Instance: " + mFireUser.toString());
        //TODO Not getting called
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(DEBUG_TAG, "Firebase CredentialSignup onComplete called");
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "signInWithCredential:success");
                            fUid = mAuth.getCurrentUser().getUid();
                            Log.d(DEBUG_TAG, "FirebaseAuth: " + fUid);
                            initiateRegistration();
                        } else {
                            Log.d(DEBUG_TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void initiateRegistration() {
        //Register User::
        final User userForm = new User(fUid, tFname, tLname, tSname, tEmail, tDob, tGender);

        //TODO do in separate thread
        //set SharedPreferences Defaults
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id", fUid);
        editor.putString("first_name", tFname);
        editor.putString("last_name", tLname);
        editor.putString("short_name", tSname);
        editor.putString("gender", tGender);
        editor.putString("email", tEmail);
        editor.putString("dob", tDob);
        editor.apply();

        //reference String
        Map<String, String> params = new HashMap<String, String>();
        Log.d(DEBUG_TAG, "fireID: " + fUid);
        params.put("userID", fUid);
        final String reference = substituteString(getResources().getString(R.string.user_check), params);
        //TODO redundant data: userID getting stored as the key and as one of the values as well
        create_user = mDatabase.getReference(reference);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_TAG, dataSnapshot.toString());
                if(dataSnapshot.getValue() == null) {
                    Log.d(DEBUG_TAG, "Creating a new User");
                    create_user.setValue(userForm);
                    mUser = userForm;
                }
                else {
                    Log.d(DEBUG_TAG, "accessing stored user info");
                    mUser = dataSnapshot.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        create_user.addListenerForSingleValueEvent(userListener);

        Intent i = new Intent(LoginActivity.this, LoadingActivity.class);
        startActivity(i);
    }

    @Override
    public void nameListener(String a, String b) {

    }

    private class LoginSlidePagerAdapter extends FragmentStatePagerAdapter {
        public LoginSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new LoginFragmentOne();
                case 3: return new LoginFragmentFour();
                default: return new LoginFragmentOne();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        create_user.removeEventListener(userListener);
    }
}




