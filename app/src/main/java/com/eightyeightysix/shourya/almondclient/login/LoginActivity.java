package com.eightyeightysix.shourya.almondclient.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.BaseActivity;
import com.eightyeightysix.shourya.almondclient.FeedActivity;
import com.eightyeightysix.shourya.almondclient.LoadingActivity;
import com.eightyeightysix.shourya.almondclient.R;

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
    private static String emailId, password, dob, uname, fname, lname;
    private Context mContext;

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
    public void fblistener(String id1, String fname1, String lname1, String sname1, String gender1, String email1, boolean emailAvailable, String dob1, boolean dobAvailable) {
        Log.d(DEBUG_TAG, "fbListener Callback called");
        if(emailAvailable) emailId = email1;
        else{
            //TODO add email fragment
        }
        if(dobAvailable) dob = dob1;
        else {
            nPager.setCurrentItem(3);
        }

        //set SharedPreferences Defaults
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id", id1);
        editor.putString("first_name", fname1);
        editor.putString("last_name", lname1);
        editor.putString("short_name", sname1);
        editor.putString("gender", gender1);
        if(emailAvailable)
            editor.putString("email", email1);
        if(dobAvailable)
            editor.putString("dob", dob1);
        editor.apply();

        //TODO send data to server

        setDefaults(id1,fname1,lname1,sname1,email1);

        Intent i = new Intent(LoginActivity.this, FeedActivity.class);
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
}




