package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 12/7/17.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.view.RequestPagerView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.w3c.dom.Text;

public class AllRequestsActivity extends BaseActivity implements OnMapReadyCallback,
                                                        RequestPagerView.requestCallback {
    private GoogleMap gMap;
    private ViewPager requestPager;
    private RequestPagerAdapter requestAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_requests);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_requests);
        mapFragment.getMapAsync(this);

        requestPager = (ViewPager)findViewById(R.id.request_pager);
        requestAdapter = new RequestPagerAdapter(getSupportFragmentManager());
        requestPager.setAdapter(requestAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    private class RequestPagerAdapter extends FragmentStatePagerAdapter {
        public RequestPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new RequestPagerView();
            Bundle b = new Bundle();
            b.putInt("position",position);
            fragment.setArguments(b);
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @Override
    public void onRequestClick(boolean a) {
        Toast.makeText(getApplicationContext(), requestPager.getCurrentItem() + " " + a, Toast.LENGTH_SHORT).show();
    }

}
