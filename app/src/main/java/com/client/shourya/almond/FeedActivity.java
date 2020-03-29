package com.client.shourya.almond;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.Log;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.shourya.almond.data.User;
import com.client.shourya.almond.gestureui.AlmondPagerSettings;
import com.client.shourya.almond.view.AlmondLayout;

import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/*
 * Created by shourya on 'we will never know'.
 */

public class FeedActivity extends BaseActivity implements ChatListFragment.StartChatListener{
    //TODO Put location requests in the tutorial pages. For now keep in feed page
    FragmentManager fragmentManager;
    private static final int NUM_PAGES  = 1;
    private final static String DEBUG_TAG = "AlmondLog:: " + FeedActivity.class.getSimpleName();
    private AlmondPagerSettings mPager;
    private static SwipeUpPagerAdapter mPagerAdapter;
    private View view1, view2;
    //protected ChatListFragment chatListFragment;
    public static boolean pinchTourActive;
    protected BroadCastFragment broadCastFragment;
    private FloatingActionButton fab;
    private Context mContext;
    private static boolean tutorialActive = false;
    private int tut_screens_count = 0;
    private static TourGuide abc;
    private ImageView mAddPeriferi, mRequests;
    private Animation mEnterAnimation, mExitAnimation;
    private ChainTourGuide mTutorial;
    private SharedPreferences prefs;
    TextView tutTxt;
    //protected CoordinatorLayout mainView;
    ///gesture 
    //ZonePinchSurfaceView pinchView;
    private Toolbar almondToolbar;

    //TODO add stuff to onStart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate Called");
        setContentView(R.layout.activity_feed);
        //setup toolbar
        setupAppBar();
        mContext = getApplicationContext();
        imageUploader = new ImageUploader(mFireUser.getUid(), getContentResolver());

        AlmondLayout mainView = (AlmondLayout)findViewById(R.id.feed_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        tutTxt = (TextView)findViewById(R.id.no_posts_text);
        mPager  = (AlmondPagerSettings)findViewById(R.id.feed_pager);
        view1 = (View)findViewById(R.id.fragment_container_feed);
        view2 = (View) findViewById(R.id.feed_pager);

        //initialise fragment Manager
        fragmentManager = getSupportFragmentManager();
        mainView.gestureInit();
        pinchTourActive = false;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!tutorialActive)
                    createNewBroadCast();
            }
        });

        mPagerAdapter = new SwipeUpPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume Called");
    }

    //tutorial
    public void showUserHow() {
        //mTourguide.setVisibility(View.INVISIBLE);
        tutorialActive = true;
        mEnterAnimation = new AlphaAnimation(0f, 1f);
        mEnterAnimation.setDuration(600);
        mEnterAnimation.setFillAfter(true);

        mExitAnimation = new AlphaAnimation(1f, 0f);
        mExitAnimation.setDuration(600);
        mExitAnimation.setFillAfter(true);
        //ChainTourGuide mTutorial;

        ChainTourGuide mBroadcasttour = ChainTourGuide.init(this).with(TourGuide.Technique.VerticalDownward)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip()
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryRedAccent))
                        .setTitle("BroadCast").setDescription("in the current Periferi")
                .setGravity(Gravity.TOP | Gravity.LEFT))
                .setOverlay(new Overlay()
                            .setEnterAnimation(mEnterAnimation)
                            .setExitAnimation(mExitAnimation))
                .playLater(fab);
        ChainTourGuide mAddtour = ChainTourGuide.init(this).with(TourGuide.Technique.VerticalDownward)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip()
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryRedAccent))
                        .setTitle("Create").setDescription("a new Periferi to interact with new people around you")
                        .setGravity(Gravity.BOTTOM | Gravity.LEFT))
                .setOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation))
                .playLater(mAddPeriferi);
        ChainTourGuide mRequesttour = ChainTourGuide.init(this).with(TourGuide.Technique.VerticalDownward)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip()
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryRedAccent))
                        .setTitle("Requests").setDescription("Check out Periferi requests around you")
                        .setGravity(Gravity.BOTTOM | Gravity.LEFT))
                .setOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation))
                .playLater(mRequests);

        Sequence sequence = new Sequence.SequenceBuilder()
                .add(mBroadcasttour, mAddtour, mRequesttour)
                .setDefaultOverlay(new Overlay().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTutorial.next();
                        tut_screens_count++;
                        if(tut_screens_count == 3) {
                            startPinchTutorial();
                        }
                        Log.d(DEBUG_TAG, "Overlay Clicked , count=" + tut_screens_count);
                    }
                }))
                .setDefaultPointer(null)
                .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                .build();
        mTutorial = ChainTourGuide.init(this).playInSequence(sequence);

        //toggle preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("rookieFeed", false);
        editor.apply();
    }

    public void startPinchTutorial() {
        pinchTourActive = true;
        abc = TourGuide.init(this).with(TourGuide.Technique.VerticalDownward)
                .setPointer(new Pointer().setGravity(Gravity.TOP | Gravity.START))
                .setToolTip(new ToolTip()
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryRedAccent))
                        .setTitle("Pinch").setDescription("with 2 fingers to change your Periferi").setGravity(Gravity.TOP))
                .setOverlay(new Overlay()
                        .setEnterAnimation(mEnterAnimation)
                        .setExitAnimation(mExitAnimation)
                )
                .playOn(view2);
    }

    //called on pinch gesture
    public static void exitPinchTour() {
        pinchTourActive = false;
        abc.cleanUp();
        tutorialActive = false;
    }

    public void createNewBroadCast() {
        DialogFragment dialog = new NewBroadCastDialog();
        dialog.show(getFragmentManager(), "NewBroadCastDialog");
    }


    public void setupAppBar() {
        almondToolbar = (Toolbar)findViewById(R.id.almond_bar);
        setSupportActionBar(almondToolbar);
        almondToolbar.setLogo(R.mipmap.ic_periferi);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            almondToolbar.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }catch (NullPointerException e) {
            Log.d(DEBUG_TAG, "Didnt remove title from bar");
        }
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
        //MenuItem mAdd = menu.getItem(0);
        //MenuItem mRequest = menu.getItem(1);
        MenuItem mAdd = menu.findItem(R.id.map_zones);
        MenuItem mRequest = menu.findItem(R.id.all_requests);

        mAddPeriferi = (ImageView)mAdd.getActionView();
        mRequests = (ImageView)mRequest.getActionView();

        float density = getResources().getDisplayMetrics().density;
        int padding = (int)(density*10);
        mAddPeriferi.setPadding(padding,padding,padding,padding);
        mRequests.setPadding(padding, padding, padding, padding);

        mAddPeriferi.setImageDrawable(getResources().getDrawable(R.drawable.create_zone_icon, null));
        mRequests.setImageDrawable(getResources().getDrawable(R.drawable.periferi_requests, null));

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefs.getBoolean("rookieFeed", false)){
            showUserHow();
        }

        /*
        -keep public class com.client.shourya.periferi.BaseActivity
-keep public class com.client.shourya.periferi.FeedActivity
-keep public class com.client.shourya.periferi.InstructionsDialog
-keep public class com.client.shourya.periferi.LoadingActivity
-keep public class com.client.shourya.periferi.data.*
-keep public class com.client.shourya.periferi.Manifest
-keep public class com.client.shourya.periferi.NewBroadCastDialog
-keep public class com.client.shourya.periferi.NewZoneRequestDialog

-dontshrink
-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify


-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-keep class com.firebase.**
-keep class com.google.firebase.**
         */
        mAddPeriferi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tutorialActive)
                    startActivity(new Intent(FeedActivity.this, RequestZoneActivity.class));
            }
        });

        mRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tutorialActive) {
                    if (currZoneRequestKeys.isEmpty())
                        toastit("There are currently no active requests");
                    else
                        startActivity(new Intent(FeedActivity.this, AllRequestsActivity.class));
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
/*
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
*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*if(fragmentManager.getBackStackEntryCount() == 0) {
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
        }*/
        finish();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private class SwipeUpPagerAdapter extends FragmentStatePagerAdapter {
        public SwipeUpPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                /*case 1: {
                    if (chatListFragment == null)
                        chatListFragment = new ChatListFragment();
                    return chatListFragment;
                }*/
                case 0:{
                    if(broadCastFragment == null)
                        broadCastFragment = new BroadCastFragment();
                    return broadCastFragment;
                }
                default: return broadCastFragment;
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
        if(currCircle != id){
            currCircle = id;
            //((ChatListFragment) mPagerAdapter.getItem(1)).fetchOnlineUsers(id);
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
