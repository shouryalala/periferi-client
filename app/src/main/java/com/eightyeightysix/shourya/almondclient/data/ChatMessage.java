package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 14/6/17.
 */

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ChatMessage {
    private static final String DEBUG_TAG = "AlmondLog:: " + ChatMessage.class.getSimpleName();
    public int mBy;
    public String message;
    //add timestamp

    public ChatMessage() {
        //required for Firebase setValue
    }

    public ChatMessage(int sBy, String sMessage) {
        mBy = sBy;
        message = sMessage;
    }

    @Exclude
    public int getTag() {return mBy;}

    @Exclude
    public String getMessage() {return message;}
}
