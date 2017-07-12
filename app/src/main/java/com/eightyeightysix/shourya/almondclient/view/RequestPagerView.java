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
        Button acc = (Button)rootView.findViewById(R.id.accept_request);
        Button rej = (Button)rootView.findViewById(R.id.reject_request);

        header.setText("Position: " + position);

        acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRequestClick(true);
            }
        });

        rej.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRequestClick(false);
            }
        });

        //((TextView) rootView.findViewById(R.id.textView1)).setText(Integer.toString(args.getInt(ARG_PAGE)));
        return rootView;
    }
}

