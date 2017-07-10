package com.eightyeightysix.shourya.almondclient.login;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eightyeightysix.shourya.almondclient.BaseActivity;
import com.eightyeightysix.shourya.almondclient.LoadingActivity;
import com.eightyeightysix.shourya.almondclient.R;
import com.eightyeightysix.shourya.almondclient.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
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
    private static final int NUM_PAGES = 3;
    private ViewPager nPager;
    private PagerAdapter mPagerAdapter;
    public static String fUid;
    Context mContext;
    private static String tId, tFname, tLname, tGender, tSname, tEmail, tDob;
    DatabaseReference create_user;
    ValueEventListener userListener;
    User temp_user;
    Thread t = null;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private static String tut_text[] = new String[2];

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        //nPager = (NonSwipeableViewPager) findViewById(R.id.pager2);
        nPager = (ViewPager)findViewById(R.id.pager);
        nPager.addOnPageChangeListener(new AnimateColorTransition());

        if (findViewById(R.id.fragment_container) != null) {
                mPagerAdapter = new LoginSlidePagerAdapter(getSupportFragmentManager());
                nPager.setAdapter(mPagerAdapter);
        }

        //request Location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions(this);
        }
        tut_text[0] = "Welcome to Almond. Meet friends on a platform centred around you";
        tut_text[1] = "Decide the exclusivity of your network by pinching into the zone of your choice!";
        setUpColors();
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

                Intent i = new Intent(LoginActivity.this, LoadingActivity.class);
                startActivity(i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        create_user.addListenerForSingleValueEvent(userListener);


    }

    @Override
    public void nameListener(String a, String b) {

    }

    private void setUpColors(){

        Integer color1 = ResourcesCompat.getColor(getResources(), R.color.tut_1_green, null);
        Integer color2 = ResourcesCompat.getColor(getResources(), R.color.tut_2_blue, null);
        Integer color3 = ResourcesCompat.getColor(getResources(), R.color.tut_3_purple, null);

        Integer[] colors_temp = {color1, color2, color3};
        colors = colors_temp;
    }


    private class LoginSlidePagerAdapter extends FragmentStatePagerAdapter {
        public LoginSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /*switch (position) {
                case 0: return new LoginFragmentOne();
                case 3: return new LoginFragmentFour();
                default: return new LoginFragmentOne();
            }*/
            if(position == 2)
                return new LoginFragmentOne();
            else{
                Fragment fragment = new TutorialObjectFragment();
                Bundle args = new Bundle();
                args.putInt(TutorialObjectFragment.ARG_PAGE,position);
                fragment.setArguments(args);
                return fragment;
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

    private class AnimateColorTransition implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(position < (mPagerAdapter.getCount() -1) && position < (colors.length - 1)) {

                nPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]));

            } else {

                // the last page color
                nPager.setBackgroundColor(colors[colors.length - 1]);

            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public static class TutorialObjectFragment extends Fragment{

        public static final String ARG_PAGE = "obj";

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.welcome_tutorial,container,false);
            Bundle args = getArguments();
            int position = args.getInt(ARG_PAGE);

            ((TextView) rootView.findViewById(R.id.welcome_text)).setText(tut_text[position]);
            //((TextView) rootView.findViewById(R.id.textView1)).setText(Integer.toString(args.getInt(ARG_PAGE)));
            return rootView;
        }
    }
}




