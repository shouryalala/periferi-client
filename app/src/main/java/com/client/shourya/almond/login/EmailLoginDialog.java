package com.client.shourya.almond.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.client.shourya.almond.R;

import static com.client.shourya.almond.BaseActivity.validateEmail;
import static com.client.shourya.almond.BaseActivity.validatePassword;

public class EmailLoginDialog extends DialogFragment {
    private final String DEBUG_TAG = "AlmondLog:: " + EmailLoginDialog.class.getSimpleName() ;
    private EditText etEmail, etUserId, etPassword, etUserName;
    private Button btnLogin;
    private Boolean isExistingUser;
    private ProgressBar mProgressBar;
    EmailLoginListener emailLoginListener;

    interface EmailLoginListener{
        public void onSignInRequest(String email, String password, String name);
        public void onSignUpRequest(String userId, String email, String password, String name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.email_login_dialog, null);
        etEmail = (EditText) v.findViewById(R.id.user_email_editText);
        etUserId = (EditText) v.findViewById(R.id.user_id_editText);
        etPassword = (EditText) v.findViewById(R.id.user_password_editText);
        etUserName = (EditText) v.findViewById(R.id.user_name_editText);
        btnLogin = (Button) v.findViewById(R.id.dialog_login_button);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        mProgressBar.setVisibility(View.INVISIBLE);

        isExistingUser = false;
        try {
            isExistingUser = getArguments().getBoolean("existingUser");
        }catch (NullPointerException e) {
            Log.e(DEBUG_TAG, "No Bundle found.. Defaulting to false");
        }
        if(isExistingUser) {
            //fix email and remove new user fields
            etEmail.setEnabled(false);
            etUserId.setVisibility(View.GONE);
            etUserName.setVisibility(View.GONE);
            btnLogin.setText("Login");
        }
        else{
            etEmail.setEnabled(true);
            etUserId.setVisibility(View.VISIBLE);
            etUserName.setVisibility(View.VISIBLE);
            btnLogin.setText("SignUp");
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionLoginClick();
            }
        });
        builder.setView(v);
        return builder.create();
    }

    private void actionLoginClick() {
        if(isExistingUser) {
            if(!validatePassword(etPassword.getText().toString())) {
                etPassword.setError("Password should be more than 8 characters and should contain a digit and capital letter");
                return;
            }
            mProgressBar.setVisibility(View.VISIBLE);
            String uName = null;
            try{
                uName = getArguments().getString("userName");
            }catch (NullPointerException e) {
                Log.e(DEBUG_TAG, "Couldnt fetch user name from bundle. Defaulting to null. Needs to be fetched from server");
            }
            emailLoginListener.onSignInRequest(etEmail.getText().toString(), etPassword.getText().toString(), uName);
        }else {
            boolean flag = true;
            if (!validateEmail(etEmail.getText().toString())) {
                etEmail.setError("Please provide a valid email");
                flag = false;
            }
            if (!validatePassword(etPassword.getText().toString())) {
                etPassword.setError("Password should be more than 8 characters and should contain a digit and capital letter");
                flag = false;
            }
            String userId = etUserId.getText().toString();
            if (userId.isEmpty() || userId.length() < 6) {
                etUserId.setError("");
                flag = false;
            }
            String userName = etUserName.getText().toString();
            if (userName.isEmpty() || userName.length() < 6) {
                etUserName.setError("");
                flag = false;
            }
            if(!flag)return;
            mProgressBar.setVisibility(View.VISIBLE);
            emailLoginListener.onSignUpRequest(userId, etEmail.getText().toString(), etPassword.getText().toString(), userName);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            emailLoginListener = (EmailLoginListener)context;
        }catch (ClassCastException e) {
            Log.e(DEBUG_TAG, "Context: " + context + " not currently implementing EmailLoginListener");
            e.printStackTrace();
        }
    }
}
