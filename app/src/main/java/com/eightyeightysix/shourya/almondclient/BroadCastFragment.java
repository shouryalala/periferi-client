package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 20/6/17.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.eightyeightysix.shourya.almondclient.data.BroadCast;
import com.eightyeightysix.shourya.almondclient.viewholder.BroadCastViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class BroadCastFragment extends Fragment{
    private static final String DEBUG_TAG = "AlmondLog:: " + BroadCastFragment.class.getSimpleName();
    private static final int CITY_INDEX = 69;
    private static final int COUNTRY_INDEX = 420;

    private DatabaseReference mDatabase;
    private Context mContext;

    private FirebaseRecyclerAdapter<BroadCast, BroadCastViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private static ProgressDialog pd;

    public BroadCastFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.broadcast_view, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        pd = new ProgressDialog(mContext);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        fetchCircleBroadCasts(BaseActivity.currCircle);
    }

    private String formReference(int circle) {
        Map<String, String> params = new HashMap<>();
        String reference;
        switch(circle) {
            case COUNTRY_INDEX: {
                //country users
                params.put("countryID", BaseActivity.locationDetails.getCountryID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_country), params);
                break;
            }
            case CITY_INDEX:{
                //city users
                params.put("cityID", BaseActivity.locationDetails.getCityID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_city), params);
                break;
            }
            default:{
                //zone
                params.put("zoneID", BaseActivity.locationDetails.zonesList.get(circle).getZoneKey());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_zone), params);
                break;
            }
        }
        Log.d(DEBUG_TAG, "Switched to Circle: " + circle);
        return reference;
    }

    public void fetchCircleBroadCasts(int circle) {
        pd.setMessage("Populating feed..");
        pd.show();
        Log.d(DEBUG_TAG, "Refreshing broadcasts to circle: " + circle);
        final String ref = formReference(circle);

        if (mAdapter != null) {
            mAdapter.cleanup();
        }

        // Set up FirebaseRecyclerAdapter with the Query
        final Query postsQuery = BaseActivity.mDatabase.getReference(ref);

        mAdapter = new FirebaseRecyclerAdapter<BroadCast, BroadCastViewHolder>(BroadCast.class, R.layout.item_post,
                BroadCastViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final BroadCastViewHolder viewHolder, BroadCast model, int position) {
                final DatabaseReference postRef = getRef(position);
                String imgUrl = model.userImage;
                Log.d(DEBUG_TAG, "Fetched uri: " + imgUrl);
                        //add shit
                //clickable likes
                pd.dismiss();
                Glide.with(getContext()).load(imgUrl).into(viewHolder.pictureView);
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = BaseActivity.mDatabase.getReference(ref).child(postRef.getKey());
                        //DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());
                        // Run two transactions
                        onStarClicked(globalPostRef);
                        //onStarClicked(userPostRef);
                    }
                });
                if(model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.like_icon);
                }
                else{
                    viewHolder.starView.setImageResource(R.drawable.unlike_icon);
                }
            }

        };
        mRecycler.setAdapter(mAdapter);
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                BroadCast p = mutableData.getValue(BroadCast.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(DEBUG_TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
        pd.dismiss();
    }

    public String getUid() {
        return BaseActivity.mUser.getUserId();
    }

}
