package com.eightyeightysix.shourya.almondclient.viewholder;

/*
 * Created by shourya on 20/6/17.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eightyeightysix.shourya.almondclient.R;
import com.eightyeightysix.shourya.almondclient.data.BroadCast;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

public class BroadCastViewHolder extends RecyclerView.ViewHolder{
    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;
    public ImageView pictureView;

    public BroadCastViewHolder(View itemView) {
        super(itemView);
        //titleView = (TextView) itemView.findViewById(R.id.post_title);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        starView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        bodyView = (TextView) itemView.findViewById(R.id.post_body);
        pictureView = (ImageView) itemView.findViewById(R.id.profile_picture);
    }

    public void bindToPost(BroadCast broadCast, View.OnClickListener starClickListener) {
        //titleView.setText(broadCast.title);
        authorView.setText(broadCast.author);
        numStarsView.setText(String.valueOf(broadCast.starCount));
        bodyView.setText(broadCast.body);

        starView.setOnClickListener(starClickListener);
    }

}
