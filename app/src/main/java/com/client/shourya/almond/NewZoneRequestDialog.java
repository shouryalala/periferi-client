package com.client.shourya.almond;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

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
    TextView submit;
    zoneRequestCallback callback;

    public NewZoneRequestDialog() {}

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
        submit = (TextView) inf2.findViewById(R.id.request_zone);
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
