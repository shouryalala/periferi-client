package com.eightyeightysix.shourya.almondclient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.eightyeightysix.shourya.almondclient.data.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 10/6/17.
 */

//Lists all Users who have signed up and initiates a chat on click
public class ChatListFragment extends Fragment {
    private static final String DEBUG_TAG = "AlmondLog:: " + ChatListFragment.class.getSimpleName();
    TextView noUsers;
    ArrayList<User> userArrayList;
    ArrayList<String> temp_chat_name;
    ListView listView;
    Context fContext;
    ProgressDialog progressDialog;
    DatabaseReference fetch_users;
    ValueEventListener userFetchListener;
    //temps
    Button bCountry, bCity, bZone;

    public interface StartChatListener{
        void startChat(User chatWith);
    }

    StartChatListener chatListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_list_view, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        chatListener = (StartChatListener)context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fContext = getContext();
        listView = (ListView) view.findViewById(R.id.chatList);
        noUsers = (TextView) view.findViewById(R.id.noChatsText);
        userArrayList = new ArrayList<>();
        temp_chat_name = new ArrayList<>(); //currently shows all users who signed up.

        progressDialog = new ProgressDialog(fContext);

        fetchOnlineUsers(BaseActivity.currCircle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(DEBUG_TAG, "View Destroy, Remove Event Listener");
        fetch_users.removeEventListener(userFetchListener);
    }

    private String formReference(int circle) {
        Map<String, String> params = new HashMap<>();
        String reference;
        switch(circle) {
            case 0: {
                //country users
                params.put("countryID", BaseActivity.locationDetails.getCountryID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_online_country), params);
                break;
            }
            case 1:{
                //city users
                params.put("cityID", BaseActivity.locationDetails.getCityID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_online_city), params);
                break;
            }
            default:{
                //zone
                params.put("zoneID", BaseActivity.locationDetails.zonesList.get(circle - 2));
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_online_zone), params);
                break;
            }
        }
        Log.d(DEBUG_TAG, "Switched to Circle: " + circle);
        return reference;
    }

    public void fetchOnlineUsers(int circle) {
        progressDialog.setMessage("Loading..");
        progressDialog.show();

        String ref = formReference(circle);

        if(fetch_users != null) {
            fetch_users.removeEventListener(userFetchListener);
        }
        fetch_users = BaseActivity.mDatabase.getReference(ref);
        userFetchListener = fetch_users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_TAG, dataSnapshot.toString());
                temp_chat_name.clear();
                if(dataSnapshot.getValue() != null) {
                    listView.setVisibility(View.VISIBLE);
                    noUsers.setVisibility(View.INVISIBLE);
                    for(DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                        String key = (String)userSnapshot.getKey();
                        if(!key.equals(BaseActivity.mUser.getUserId())) {
                            //userArrayList.add((String)userSnapshot.getValue());
                            temp_chat_name.add(key);
                        }
                        Log.d(DEBUG_TAG, userSnapshot.getValue().toString());
                    }
                    listView.setAdapter(new ArrayAdapter<String>(fContext, android.R.layout.simple_list_item_1, temp_chat_name));
                }
                else {
                    Log.d(DEBUG_TAG, "No users available");
                    listView.setVisibility(View.INVISIBLE);
                    noUsers.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> prms = new HashMap<String, String>();
                prms.put("userID", temp_chat_name.get(position));
                final String ref = BaseActivity.substituteString(getResources().getString(R.string.user_check), prms);
                BaseActivity.mDatabase.getReference(ref).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        chatListener.startChat(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
