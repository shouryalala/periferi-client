package com.eightyeightysix.shourya.almondclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/*
 * Created by shourya on 17/7/17.
 */

public class InstructionsDialog extends DialogFragment {
    public static final int CREATE_PERIFERI_INSTRUCTIONS = 69;
    public static final int PERIFERI_REQUESTS_INSTRUCTIONS = 96;

    public InstructionsDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int choice = getArguments().getInt("ins_reqd");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View li = getActivity().getLayoutInflater().inflate(R.layout.instructions_dialog, null);
        TextView txt = (TextView)li.findViewById(R.id.instructions_text);
        if(choice == CREATE_PERIFERI_INSTRUCTIONS) {
            txt.setText(getResources().getString(R.string.instruction_create_request));
        }
        else if(choice == PERIFERI_REQUESTS_INSTRUCTIONS) {
            txt.setText(getResources().getString(R.string.instruction_periferi_request));
        }

        builder.setView(li);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
