package com.eightyeightysix.shourya.almondclient;

/*
 * Created by shourya on 20/6/17.
 */

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eightyeightysix.shourya.almondclient.data.BroadCast;
import com.eightyeightysix.shourya.almondclient.viewholder.BroadCastViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;

public class BroadCastFragment extends Fragment{
    private static final String DEBUG_TAG = "AlmondLog:: " + BroadCastFragment.class.getSimpleName();

    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<BroadCast, BroadCastViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    ProgressDialog progressDialog;

    public BroadCastFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.broadcast_view, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        progressDialog = new ProgressDialog(getContext());
        return rootView;
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
            case 0: {
                //country users
                params.put("countryID", BaseActivity.locationDetails.getCountryID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_country), params);
                break;
            }
            case 1:{
                //city users
                params.put("cityID", BaseActivity.locationDetails.getCityID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_city), params);
                break;
            }
            default:{
                //zone
                params.put("zoneID", BaseActivity.locationDetails.zonesList.get(circle - 2));
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_zone), params);
                break;
            }
        }
        Log.d(DEBUG_TAG, "Switched to Circle: " + circle);
        return reference;
    }

    public void fetchCircleBroadCasts(int circle) {
        progressDialog.setMessage("Populating broadcasts..");
        progressDialog.show();
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
            protected void populateViewHolder(BroadCastViewHolder viewHolder, BroadCast model, int position) {

                final DatabaseReference postRef = getRef(position);
                //add shit
                //clickable likes
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
            }
        };
        mRecycler.setAdapter(mAdapter);

        progressDialog.dismiss();
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
    }

    public String getUid() {
        return BaseActivity.mUser.getUserId();
    }

}
