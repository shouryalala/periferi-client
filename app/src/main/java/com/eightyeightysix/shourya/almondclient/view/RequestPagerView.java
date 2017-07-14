package com.eightyeightysix.shourya.almondclient.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eightyeightysix.shourya.almondclient.R;

/*
 * Created by shourya on 12/7/17.
 */

public class RequestPagerView extends Fragment {
    public static final String ARG_PAGE = "position";
    public interface requestCallback {
        public void onRequestClick(boolean a);
    }
    requestCallback callback;

    public RequestPagerView() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (requestCallback)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.request_pager, container, false);
        Bundle args = getArguments();
        int position = args.getInt(ARG_PAGE);
        TextView header = (TextView)rootView.findViewById(R.id.request_name);
        final Button acc = (Button)rootView.findViewById(R.id.accept_request);
        final Button rej = (Button)rootView.findViewById(R.id.reject_request);
        final TextView zoneStatus = (TextView)rootView.findViewById(R.id.needed_approvals);

        String requestName = getArguments().getString("name", "UNAVAILABLE");
        final Boolean responded = getArguments().getBoolean("responded");
        if(responded) {
            acc.setVisibility(View.GONE);
            rej.setVisibility(View.GONE);
        }
        final Integer remaining = getArguments().getInt("requestCount");

        header.setText(requestName);
        if(remaining != 0) {
            zoneStatus.setText(remaining + "more approvals needed to create this Periferi");
        }
        else {
            zoneStatus.setText("This Periferi shall soon be created!");
        }
        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRequestClick(true);
                String updateText;
                if(remaining == 1) {
                    updateText = "Awesome! This zone shall be created shortly";
                }
                else {
                    int a = remaining-1;
                    updateText = a + "more approvals needed to create this Periferi";
                }
                zoneStatus.setText(updateText);

                acc.setVisibility(View.GONE);
                rej.setVisibility(View.GONE);
            }
        });

        rej.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRequestClick(false);
                acc.setVisibility(View.GONE);
                rej.setVisibility(View.GONE);
            }
        });



        //((TextView) rootView.findViewById(R.id.textView1)).setText(Integer.toString(args.getInt(ARG_PAGE)));
        return rootView;
    }
}

