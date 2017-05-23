package com.eightyeightysix.shourya.almondclient.login;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v4.media.session.MediaControllerCompat;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

/*
 * Created by shourya on 22/5/17.
 */

public class LoginFragmentOne extends Fragment {
    //Facebook
    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
    public static FragmentOneListener mListener;
    private static boolean fb_email, fb_dob;
    EditText email;
    Button cntButton;
    public static String fb_fname, fb_lname, fb_emailText = "", fb_dobText = "";

    private final String LOG_TAG = "shouryalala";


    public interface FragmentOneListener {
        void emailListener(String a);
        void fblistener(String a, String g, boolean b, String c, boolean d, String e);
    }

    private AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    private ProfileTracker profileTracker;

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            accessToken = loginResult.getAccessToken();
            accessToken.getUserId();
            Profile profile = Profile.getCurrentProfile();
            //fb_name = profile.getName();
            fb_fname = profile.getFirstName();
            fb_lname = profile.getLastName();
            GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.v("LoginActivity", response.toString());
                    try {
                        String mail = object.getString("email");
                        fb_emailText = fb_emailText.concat(mail);
                    }catch (JSONException e){
                        Log.d(LOG_TAG, "email not received");
                        fb_email = false;
                    }
                    try {
                        String dob = object.getString("birthday"); // 01/31/1980 format
                        fb_dobText = fb_dobText.concat(dob);
                    }catch(JSONException e) {
                        Log.d(LOG_TAG, "dob not received");
                        fb_dob = false;
                    }
                }
            });
            mListener.fblistener(fb_fname, fb_lname, fb_email, fb_emailText, fb_dob, fb_dobText);
        }

        @Override
        public void onCancel() {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.facebook_cancel), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.facebook_error), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker= new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                accessToken = newToken;
            }
        };
        fb_email = true;
        fb_dob = true;
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {

            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FragmentOneListener) context;
        }catch(ClassCastException e) {
            Log.d(LOG_TAG, "context error");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstances) {
        return layoutInflater.inflate(R.layout.login_view_uno, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstances) {
        super.onViewCreated(view, savedInstances);

        textView = (TextView) view.findViewById(R.id.textView);
        loginButton = (LoginButton) view.findViewById(R.id.button_facebook);
        cntButton = (Button) view.findViewById(R.id.button_continue);
        email = (EditText) view.findViewById(R.id.email);
        List<String> permissions = Arrays.asList("email","public_profile","user_birthday");

        loginButton.setReadPermissions(permissions);
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);

        //SignUp
       cntButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO check if account already present. If so login directly with password
                String emailId = email.getText().toString();
                if(emailId.length() == 0)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.field_empty),Toast.LENGTH_LONG).show();
                else if(!emailId.contains("@"))
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.invalid_email),Toast.LENGTH_LONG).show();
                else
                    mListener.emailListener(emailId);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
