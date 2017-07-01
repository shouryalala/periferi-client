package com.eightyeightysix.shourya.almondclient;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.gestureui.AlmondPagerSettings;


/*
 * Created by shourya on 'we will never know'.
 */

public class FeedActivity extends BaseActivity implements ChatListFragment.StartChatListener{
    //TODO Put location requests in the tutorial pages. For now keep in feed page
    FragmentManager fragmentManager;
    private static final int NUM_PAGES  = 2;
    private final static String DEBUG_TAG = "AlmondLog:: " + FeedActivity.class.getSimpleName();
    private AlmondPagerSettings mPager;
    private PagerAdapter mPagerAdapter;
    private View view1, view2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             createNewBroadCast();
            }
        });

        mPager  = (AlmondPagerSettings)findViewById(R.id.feed_pager);
        mPagerAdapter = new SwipeUpPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //initialise views
        view1 = (View)findViewById(R.id.fragment_container_feed);
        view2 = (View) findViewById(R.id.feed_pager);

        //initialise fragment Manager
        fragmentManager = getSupportFragmentManager();

        progressDialog = new ProgressDialog(this);

        Log.d(DEBUG_TAG, userId + userName + userEmail + displayName);

    }

    public void createNewBroadCast() {
        DialogFragment dialog = new NewBroadCastDialog();
        dialog.show(getFragmentManager(), "NewBroadCastDialog");
    }

    @Override
    public void startChat(User chatWith) {
        Log.d(DEBUG_TAG, "Entered startChat Listener");
        Log.d(DEBUG_TAG, "Friend name: " + chatWith.getDisplayName());
        String urlProvider;
        int sender_tag;
        mChatBuddy = chatWith;
        ChatFragment chatFragment = new ChatFragment();
        String me = mUser.getUserId();
        String friend = mChatBuddy.getUserId();

        //forms chatID child
        //storing messages as "0" and "1". The id which is alphabetically greater is set as "1"
        if(me.compareTo(friend) > 0) {
            urlProvider = me + "_" + friend;
            sender_tag = 0;
        }
        else{
            urlProvider = friend + "_" + me;
            sender_tag = 1;
        }
        Log.d(DEBUG_TAG, "ChatId formed: " + urlProvider);
        Bundle bundle = new Bundle();
        bundle.putString("urlProvider", urlProvider);
        bundle.putInt("sender_tag", sender_tag);
        //wont be required. Required as using all Users for chat
        bundle.putString("friend_name", mChatBuddy.getDisplayName());
        chatFragment.setArguments(bundle);
        Log.d(DEBUG_TAG, "Entering ChatFragment");
        /*fragmentManager.beginTransaction().replace(R.id.fragment_container_feed, chatFragment).commit();*/
        //TODO not the final model but made to present all elements configured till now

        Log.d(DEBUG_TAG, "Entering ChatFragment");
        fragmentManager.beginTransaction().add(R.id.fragment_container_feed, chatFragment)
                                            .addToBackStack("chatFragment")
                                            .commit();
        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //mLocator.connectClient();
        userOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TODO reconnect to client if user pauses the app. DO this is the onResume override
        userOffline();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "onDestroyCalled");
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(DEBUG_TAG, "Back pressed within pager");
            if (mPager.getCurrentItem() == 0) {
                finish();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
            } else {
                // Otherwise, select the previous step.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        }
        else {
            Log.d(DEBUG_TAG, "Arrived back from Chat fragment");
            fragmentManager.popBackStack();
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.VISIBLE);
        }
    }

    private class SwipeUpPagerAdapter extends FragmentStatePagerAdapter {
        public SwipeUpPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return new ChatListFragment();
                case 1: return new BroadCastFragment();
                default: return new ChatListFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    //temp functions
    public void setCountryOnline(View v) {
        if(mPager.getCurrentItem() == 1) {
            userOnlineStatusRefresh(0);

        }
    }

    public void setCityOnline(View v) {
        userOnlineStatusRefresh(1);
    }

    public void setZoneOnline(View v) {
        userOnlineStatusRefresh(2);
    }
}
