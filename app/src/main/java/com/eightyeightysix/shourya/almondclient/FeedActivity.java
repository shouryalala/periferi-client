package com.eightyeightysix.shourya.almondclient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.eightyeightysix.shourya.almondclient.data.User;


public class FeedActivity extends BaseActivity implements ChatListFragment.StartChatListener{
    //TODO Put location requests in the tutorial pages. For now keep in feed page

    FragmentManager fragmentManager;
    private final static String DEBUG_TAG = "AlmondLog:: " + BaseActivity.class.getSimpleName();
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if(findViewById(R.id.fragment_container_feed) != null) {
            if(savedInstanceState != null) {
                //if restored from a previous state
                return;
            }
            //initialise fragment Manager
            fragmentManager = getSupportFragmentManager();
            Log.d(DEBUG_TAG, "Entering ChatListFragment");
            ChatListFragment chatListFragment = new ChatListFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container_feed, chatListFragment).commit();
        }

        //fetch location instantiation
        mLocator = new GPSLocator(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mLocationRequestReturned = false;
            requestAllPermissions(this);
        }

        Log.d(DEBUG_TAG, userId + userName + userEmail + displayName);


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
        fragmentManager.beginTransaction().replace(R.id.fragment_container_feed, chatFragment).commit();
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
        mLocator.connectClient();
        userOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocator.disconnectClient();
        userOffline();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
