package com.eightyeightysix.shourya.almondclient.login;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.R;

/*
 * Created by shourya on 23/5/17.
 */

public class LoginFragmentTwo extends Fragment{
    EditText pwd;
    Button pwdButton;
    private FragmentTwoListener mListener;

    public interface FragmentTwoListener {
        void passwordListener(String a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstances) {
        return inflater.inflate(R.layout.login_view_deux, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle savedInstances){
        super.onViewCreated(view, savedInstances);
        pwd = (EditText) view.findViewById(R.id.password);
        pwdButton = (Button) view.findViewById(R.id.button_password);

        pwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pwd.getText().toString();
                if(password.length() < 6)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.invalid_password),Toast.LENGTH_LONG).show();
                else
                    mListener.passwordListener(password);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentTwoListener) context;
    }
}
