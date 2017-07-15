package com.eightyeightysix.shourya.almondclient;


//gesture
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;

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
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.eightyeightysix.shourya.almondclient.gestureui.AlmondPagerSettings;
import com.eightyeightysix.shourya.almondclient.view.AlmondLayout;

/*
 * Created by shourya on 'we will never know'.
 */

public class FeedActivity extends BaseActivity implements ChatListFragment.StartChatListener{
    //TODO Put location requests in the tutorial pages. For now keep in feed page
    FragmentManager fragmentManager;
    private static final int NUM_PAGES  = 2;
    private final static String DEBUG_TAG = "AlmondLog:: " + FeedActivity.class.getSimpleName();
    private AlmondPagerSettings mPager;
    private static SwipeUpPagerAdapter mPagerAdapter;
    private View view1, view2;
    protected ChatListFragment chatListFragment;
    protected BroadCastFragment broadCastFragment;
    private static Context mContext;
    //protected CoordinatorLayout mainView;
    ///gesture 
    //ZonePinchSurfaceView pinchView;
    private Toolbar almondToolbar;

    //TODO add stuff to onStart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        //setup toolbar
        setupAppBar();

        AlmondLayout mainView = (AlmondLayout)findViewById(R.id.feed_layout);
        mainView.gestureInit();
        //gestureInit();
        mContext = getApplicationContext();

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


        Log.d(DEBUG_TAG, userId + userName + userEmail + displayName);
    }

    public void createNewBroadCast() {
        DialogFragment dialog = new NewBroadCastDialog();
        dialog.show(getFragmentManager(), "NewBroadCastDialog");
    }

    public void setupAppBar() {
        almondToolbar = (Toolbar)findViewById(R.id.almond_bar);
        setSupportActionBar(almondToolbar);
        almondToolbar.setTitle(null);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            almondToolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        almondToolbar.setTitle("");

    }
    
    ///gesture
    /*
    interface pinchListener{
        void setPinchRadius(PointF index, PointF thumb);
        void exitPinch();
        //void debug(PointF index);
    }*/
    //////

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
    
    ///gesture
    /*private void gestureInit() {
        //getScreenCenter();    //temp
        pinchView = (ZonePinchSurfaceView) findViewById(R.id.pinchView);
        pinchView.setZOrderOnTop(true);

        primary = new PointF(INVALID_POINTER,INVALID_POINTER);
        secondary = new PointF(INVALID_POINTER,INVALID_POINTER);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        if(id == R.id.map_zones) {
            startActivity(new Intent(FeedActivity.this, RequestZoneActivity.class));
            return true;
        }

        else if(id == R.id.all_requests) {
            if(currZoneRequestKeys.isEmpty())
                toastit("There are currently no active requests");
            else {
                startActivity(new Intent(FeedActivity.this, AllRequestsActivity.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                case 1: {
                    if (chatListFragment == null)
                        chatListFragment = new ChatListFragment();
                    return chatListFragment;
                }
                case 0:{
                    if(broadCastFragment == null)
                        broadCastFragment = new BroadCastFragment();
                    return broadCastFragment;
                }
                default: return chatListFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static int decideCircleIndex(int c) {
        /*
        * circle indices are in increasing order from innermost circle to outermost circle
        * outermost circle represents the country perferi and the nest largest is the city circle
        * these aren't present in the zone list data structure thus they have been given special indices to avoid mixup
        * */
        if(c > locationDetails.zonesList.size()-1) {
            return (c == locationDetails.zonesList.size())?CITY_INDEX:COUNTRY_INDEX;
        }
        return c;
    }

    //Callback from gesture
    public static void refreshCircleContent(int id) {
        id = decideCircleIndex(id);
        if(currCircle == id)
            Toast.makeText(mContext, "Current Circle", Toast.LENGTH_SHORT);
        else {
            currCircle = id;
            ((ChatListFragment) mPagerAdapter.getItem(1)).fetchOnlineUsers(id);
            ((BroadCastFragment) mPagerAdapter.getItem(0)).fetchCircleBroadCasts(id);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        userOnline();
        Log.d(DEBUG_TAG, "OnStart Called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "OnStop Called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userOffline();
        removeRequestsRefresher();
        Log.d(DEBUG_TAG, "onDestroyCalled");
    }
}
