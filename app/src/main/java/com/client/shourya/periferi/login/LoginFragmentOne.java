package com.client.shourya.periferi.login;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.client.shourya.periferi.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/*
 * Created by shourya on 22/5/17.
 */

public class LoginFragmentOne extends Fragment {
    //Facebook
    LoginButton loginButton;
    CallbackManager callbackManager;
    public static FragmentOneListener mListener;
    private boolean fb_email, fb_dob;
    public String fb_id, fb_fname, fb_lname, fb_sname, fb_gender, fb_emailText , fb_dobText ;
    public URL profile_pic;
    public Bitmap shourya;
    TextView welcome_text;
    private final String DEBUG_TAG = "AlmondLog:: " + LoginFragmentOne.class.getSimpleName() ;


    public interface FragmentOneListener {
        void fblistener(String token, String id, URL profile_url, String fname, String lname, String sname, String gender, String email, boolean emailAvailable, String dob, boolean dobAvailable);
    }

    private AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    private ProfileTracker profileTracker;

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            accessToken = loginResult.getAccessToken();
            Log.d(DEBUG_TAG, "UserID: " + accessToken.getUserId());

            GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(DEBUG_TAG, "GraphRequestCalled");
                        Log.d(DEBUG_TAG, response.toString());

                        try {
                            fb_id = object.getString("id");
                        }catch(JSONException e) {
                            Log.d(DEBUG_TAG, "id not received");
                        }
                        try {
                            profile_pic = new URL("https://graph.facebook.com/" + fb_id + "/picture?width=250&height=250");

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                        try {
                            fb_emailText = object.getString("email");
                        }catch (JSONException e){
                            Log.d(DEBUG_TAG, "email not received");
                            fb_email = false;
                        }
                        try {
                            fb_dobText = object.getString("birthday"); // 01/31/1980 format
                        }catch(JSONException e) {
                            Log.d(DEBUG_TAG, "dob not received");
                            fb_dob = false;
                        }
                        try {
                            fb_fname = object.getString("first_name");
                            Log.d(DEBUG_TAG, "First name: "+ fb_fname);
                        }catch (JSONException e){
                            Log.d(DEBUG_TAG, "fname not received");
                        }
                        try {
                            fb_lname = object.getString("last_name");
                        }catch(JSONException e) {
                            Log.d(DEBUG_TAG, "lname not received");
                        }
                        try {
                            fb_gender = object.getString("gender");
                        }catch(JSONException e) {
                            Log.d(DEBUG_TAG, "gender not received");
                        }
                        try {
                            fb_sname = object.getString("short_name");
                        }catch(JSONException e) {
                            Log.d(DEBUG_TAG, "sname not received");
                        }
                        Log.d(DEBUG_TAG, "access token: " + accessToken.getToken());
                        mListener.fblistener(accessToken.getToken(),fb_id, profile_pic,fb_fname,fb_lname,fb_sname,fb_gender,fb_emailText,fb_email,fb_dobText,fb_dob);
                    }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, birthday, email, gender, first_name, last_name, short_name");
            request.setParameters(parameters);
            request.executeAsync();
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
                Log.d(DEBUG_TAG, "CurrentProfileChangedCalled");
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
            Log.d(DEBUG_TAG, "context error");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstances) {
        return layoutInflater.inflate(R.layout.login_view_uno, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstances) {
        super.onViewCreated(view, savedInstances);

        loginButton = (LoginButton) view.findViewById(R.id.button_facebook);
        welcome_text = (TextView) view.findViewById(R.id.welcome_text_fb2);
        List<String> permissions = Arrays.asList("email","public_profile","user_birthday");

        loginButton.setReadPermissions(permissions);
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
