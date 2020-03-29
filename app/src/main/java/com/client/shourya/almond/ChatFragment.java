package com.client.shourya.almond;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.client.shourya.almond.data.ChatMessage;
import com.client.shourya.almond.data.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 14/6/17.
 */

public class ChatFragment extends Fragment {
    private static final String DEBUG_TAG = "AlmondLog:: " + ChatFragment.class.getSimpleName();
    private int sender_tag, receiver_tag;
    User fChatBuddy;
    LinearLayout chat_layout;
    ImageButton sendButton;
    EditText messageArea;
    ScrollView scrollView;
    DatabaseReference userChat;
    ValueEventListener userChatListener;
    ChildEventListener chatListener;
    String chatId;
    String friend;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Implement the chat
        mContext = getContext();
        fChatBuddy = BaseActivity.mChatBuddy;
        chat_layout = (LinearLayout) view.findViewById(R.id.chat_area);
        sendButton = (ImageButton) view.findViewById(R.id.sendMessageButton);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        messageArea = (EditText) view.findViewById(R.id.messageArea);
        chatId = getArguments().getString("urlProvider");
        friend = getArguments().getString("friend_name");
        sender_tag = getArguments().getInt("sender_tag");
        receiver_tag = ((sender_tag == 0)?1:0);

        Map<String, String> params = new HashMap<String, String>();
        params.put("chatId", chatId);
        final String reference = BaseActivity.substituteString(getResources().getString(R.string.get_chat), params);
        userChat = BaseActivity.mDatabase.getReference(reference);

        chatListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(DEBUG_TAG, "Chatting for the first time");
                }
                else{
                    //TODO get all the previous chat but first check sql database
                    Log.d(DEBUG_TAG, "New Message Entered: " + dataSnapshot.toString());
                    ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                    if(msg.getTag() == sender_tag) {
                        addMessageBox("You: " + msg.getMessage());
                    }
                    else {
                        addMessageBox(friend + ": " + msg.getMessage());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userChat.addChildEventListener(chatListener);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageArea.getText().toString();
                if(!message.isEmpty()) {
                    ChatMessage newMsg = new ChatMessage(sender_tag, message);
                    userChat.push().setValue(newMsg);
                }
                else {
                    Log.d(DEBUG_TAG, "Chat Message Empty");
                }
            }
        });
    }

    public void addMessageBox(String messsage) {
        TextView textView = new TextView(mContext);
        textView.setText(messsage);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,0,0,10);
        textView.setLayoutParams(lp);
        chat_layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
