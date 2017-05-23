package com.eightyeightysix.shourya.almondclient.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.eightyeightysix.shourya.almondclient.FeedActivity;
import com.eightyeightysix.shourya.almondclient.R;


/*
 * Created by shourya on 22/5/17.
 */

public class LoginActivity extends FragmentActivity implements
        LoginFragmentOne.FragmentOneListener,
        LoginFragmentTwo.FragmentTwoListener,
        LoginFragmentThree.FragmentThreeListener,
        LoginFragmentFour.FragmentFourListener,
        LoginFragmentFive.FragmentFiveListener{


    private static String LOG_TAG = "shouryalala";
    private LoginFragmentOne firstFragment;
    private static final int NUM_PAGES = 4;
    //private ViewPager mPager;
    private NonSwipeableViewPager nPager;
    private PagerAdapter mPagerAdapter;
    private static String emailId, password, dob, uname, fname, lname;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        // callbackManager = CallbackManager.Factory.create();
        //mPager = (ViewPager) findViewById(R.id.pager);
        nPager = (NonSwipeableViewPager) findViewById(R.id.pager2);

        if (findViewById(R.id.fragment_container) != null) {
//            if (savedInstances == null) {
                mPagerAdapter = new LoginSlidePagerAdapter(getSupportFragmentManager());
                //mPager.setAdapter(mPagerAdapter);
                nPager.setAdapter(mPagerAdapter);
//                firstFragment = new LoginFragmentOne();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, firstFragment).commit();
//            }
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
    public void emailListener(String a) {
        emailId = a;
        nPager.setCurrentItem(1);
    }

    @Override
    public void passwordListener(String a) {
        password = a;
        nPager.setCurrentItem(2);
    }

    @Override
    public void dobListener(String a) {
        dob = a;
        nPager.setCurrentItem(3);
    }

    @Override
    public void nameListener(String b, String c) {
        fname = b;
        lname = c;
        nPager.setCurrentItem(4);
    }

    @Override
    public void signupListener(String a) {
        uname = a;
        Log.d(LOG_TAG, emailId+password+dob+uname+fname+lname);
        //TODO send data to server and get accesstoken
        Intent feed_activity = new Intent(LoginActivity.this, FeedActivity.class);
        //Bundle args = new Bundle();
        //args.putString("accessToken", "hello");
        //feed_activity.put
    }

    @Override
    public void fblistener(String g,String a, boolean b, String c, boolean d, String e) {
        fname = g;
        lname = a;
        if(b) emailId = c;
        else{
            //TODO add email fragment
            //nPager.setCurrentItem(//4);
        }
        if(d) dob = e;
        else {
            nPager.setCurrentItem(3);
        }
        nPager.setCurrentItem(4);
    }

    private class LoginSlidePagerAdapter extends FragmentStatePagerAdapter {
        public LoginSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new LoginFragmentOne();
                case 1: return new LoginFragmentTwo();
                case 2: return new LoginFragmentThree();
                case 3: return new LoginFragmentFour();
                case 4: return new LoginFragmentFive();
                //case 5: return new fbEmailFragment();
                default: return new LoginFragmentOne();
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}




