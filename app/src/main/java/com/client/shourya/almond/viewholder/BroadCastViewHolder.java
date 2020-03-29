package com.client.shourya.almond.viewholder;

/*
 * Created by shourya on 20/6/17.
 */

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.shourya.almond.R;
import com.client.shourya.almond.data.BroadCast;

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
