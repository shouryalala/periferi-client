package com.eightyeightysix.shourya.almondclient.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.eightyeightysix.shourya.almondclient.R;

import java.util.Calendar;

/*
 * Created by shourya on 23/5/17.
 */

public class LoginFragmentThree extends Fragment {
    DatePicker datePicker;
    Button dateButton;
    private FragmentThreeListener mListener;

    public interface FragmentThreeListener {
        void dobListener(String a);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstances) {
        return inflater.inflate(R.layout.login_view_trois, viewGroup, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentThreeListener) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        datePicker = (DatePicker) view.findViewById(R.id.datePicker1);
        dateButton  = (Button) view.findViewById(R.id.button_dob);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = datePicker.getMonth() + "/" + datePicker.getDayOfMonth() + "/"
                        + datePicker.getYear();

                Calendar dob = Calendar.getInstance();
                Calendar today = Calendar.getInstance();

                dob.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
                    age--;

                if(age < 15)
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.young_age),Toast.LENGTH_LONG).show();
                else
                    mListener.dobListener(date);
            }
        });

    }
}
