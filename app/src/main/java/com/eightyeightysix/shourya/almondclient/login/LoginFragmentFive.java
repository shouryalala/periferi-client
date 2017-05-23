package com.eightyeightysix.shourya.almondclient.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class LoginFragmentFive extends Fragment {
    private FragmentFiveListener mListener;
    private EditText uname;
    private Button signupButton;

    public interface FragmentFiveListener {
        void signupListener(String a);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstances) {
        return inflater.inflate(R.layout.login_view_quarte, viewGroup, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentFiveListener) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signupButton = (Button) view.findViewById(R.id.signup);
        uname = (EditText) view.findViewById(R.id.username);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unameText = uname.getText().toString();
                //TODO check databse if username is available or not
                if(unameText.length() == 0)
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.invalid_uname),Toast.LENGTH_LONG).show();
                else if(unameText.contains(" "))
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.uname_space),Toast.LENGTH_LONG).show();
                else
                    mListener.signupListener(unameText);
            }
        });
    }
}
