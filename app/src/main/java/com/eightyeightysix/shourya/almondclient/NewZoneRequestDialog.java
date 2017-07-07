package com.eightyeightysix.shourya.almondclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.data.BroadCast;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 5/7/17.
 */

public class NewZoneRequestDialog extends DialogFragment{
    public interface zoneRequestCallback{
        void onSubmit(String name);
    }

    private static final String DEBUG_TAG = "AlmondLog:: " + NewBroadCastDialog.class.getSimpleName();
    private static final String REQUIRED = "Required";
    EditText zName;
    Button submit;
    zoneRequestCallback callback;

    public NewZoneRequestDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (zoneRequestCallback)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inf2 = inflater.inflate(R.layout.new_zone_request_dialog, null);
        zName = (EditText) inf2.findViewById(R.id.zone_name);
        submit = (Button) inf2.findViewById(R.id.request_zone);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendName();
            }
        });

        builder.setView(inf2);
        return builder.create();
    }

    private void sendName() {
        final String title = zName.getText().toString();
        // Title is required
        if (TextUtils.isEmpty(title)) {
            zName.setError(REQUIRED);
            return;
        }
        else {
            callback.onSubmit(title);
        }
        dismiss();
    }
}