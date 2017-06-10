package com.eightyeightysix.shourya.almondclient.data;

/*
 * Created by shourya on 10/6/17.
 */

//TODO fireBase ID getting added as child title and is added in child contents.Causing Data redundancy
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private static final String DEBUG_TAG = "AlmondLog:: " + User.class.getSimpleName();
    public String mUserId;
    public String mFname;
    public String mLname;
    public String mDisplayName;
    public String mUserEmail;
    public String mDob;
    public String mGender;

    public User() {
        //required for Firebase setValue
    }

    public User(String userId, String fname, String lname, String displayName, String userEmail,
                String dob, String gender) {
        mUserId = userId;
        mFname = fname;
        mLname = lname;
        mDisplayName = displayName;
        mUserEmail = userEmail;
        mDob = dob;
        mGender = gender;
    }

    @Exclude
    public String getUserId() { return mUserId; }

    @Exclude
    public String getFname() { return mFname; }

    @Exclude
    public String getLname() { return mLname; }

    @Exclude
    public String getDisplayName() { return mDisplayName; }

    @Exclude
    public String getEmail() { return mUserEmail; }

    @Exclude
    public String getDob() { return mDob; }

    @Exclude
    public String getmGender() { return mGender; }
}
