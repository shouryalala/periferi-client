package com.eightyeightysix.shourya.almondclient;

import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;

/*
 * Created by shourya on 22/5/17.
 */

public class LoginActivity extends FragmentActivity {
    private static String LOG_TAG = "shouryalala";
    Button continueLogin, gplusLogin;
    LoginButton facebookLogin;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.login_activity);
       // callbackManager = CallbackManager.Factory.create();

        if(findViewById(R.id.fragment_container) != null) {
            if(savedInstances != null) {
                return;
            }

            LoginFragmentOne firstFragment = new LoginFragmentOne();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,firstFragment).commit();


        }
    }


}
