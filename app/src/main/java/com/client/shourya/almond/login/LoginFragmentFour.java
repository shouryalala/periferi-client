package com.client.shourya.almond.login;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.client.shourya.almond.R;

/*
 * Created by shourya on 23/5/17.
 */

public class LoginFragmentFour extends Fragment {
    private Button nameButton;
    EditText fname, lname;
    private FragmentFourListener mListener;

    public interface FragmentFourListener {
        void nameListener(String a, String b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstances) {
        return inflater.inflate(R.layout.login_view_quarte, viewGroup, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentFourListener) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //nameButton = (Button) view.findViewById(R.id.uname);
        fname = (EditText) view.findViewById(R.id.fname);
        lname = (EditText) view.findViewById(R.id.lname);

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.nameListener( fname.getText().toString(), lname.getText().toString());
            }
        });
    }
}
