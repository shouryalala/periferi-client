package com.client.shourya.periferi.login;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.client.shourya.periferi.BaseActivity;
import com.client.shourya.periferi.LoadingActivity;
import com.client.shourya.periferi.R;
import com.client.shourya.periferi.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by shourya on 22/5/17.
 */

public class LoginActivity extends BaseActivity implements
        LoginFragmentOne.FragmentOneListener,
        LoginFragmentFour.FragmentFourListener{
    private final static String DEBUG_TAG = "AlmondLog:: " + LoginActivity.class.getSimpleName();
    private LoginFragmentOne firstFragment;
    private static final int NUM_PAGES = 4;
    private static ProgressDialog progressDialog;
    private static Window tutorialWindow;
    private ViewPager nPager;
    private NonSwipeableViewPager sPager;
    private PagerAdapter mPagerAdapter, sPagerAdapter;
    private LinearLayout pagerIndicatorStrip;
    private ImageView[] dots;
    public static String fUid;
    Context mContext;
    private static String tId, tFname, tLname, tGender, tSname, tEmail, tDob;
    //firebase
    private TextView next_button;
    private ImageView pixel_mockup;
    private NonSwipeableViewPager mobile_pager_screen;
    private DatabaseReference create_user;
    private ValueEventListener userListener;
    private StorageReference store_profile_picture;
    Integer[] colors = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private static String fb_token;
    private static String tut_text[] = new String[3];
    private static int image_res[] = new int[3];

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.login_activity);
        mContext = getApplicationContext();
        //nPager = (NonSwipeableViewPager) findViewById(R.id.pager2);
        nPager = (ViewPager)findViewById(R.id.pager);
        nPager.addOnPageChangeListener(new AnimateColorTransition());

        sPager = (NonSwipeableViewPager) findViewById(R.id.mobile_pager);
        next_button = (TextView) findViewById(R.id.next_tutorial);
        pixel_mockup = (ImageView) findViewById(R.id.pixel_mockup);
        mobile_pager_screen = (NonSwipeableViewPager) findViewById(R.id.mobile_pager);

        if (findViewById(R.id.fragment_container) != null) {
                mPagerAdapter = new LoginSlidePagerAdapter(getSupportFragmentManager());
                nPager.setAdapter(mPagerAdapter);

                sPagerAdapter = new MobileScreenPagerAdapter(getSupportFragmentManager());
                sPager.setAdapter(sPagerAdapter);
                //sPager.setClickable(false);
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        store_profile_picture = FirebaseStorage.getInstance().getReference().child("profilepictures");

        //request Location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions(this);
        }
        tut_text[0] = "Create or approve a Periferi to interact with people in that area";
        tut_text[1] = "Pinch to check out and socialize in another Periferi around you";
        tut_text[2] = "Accept requests for a Periferi to participate in its creation";

        image_res[0] = R.drawable.tutorial_mockup2;
        image_res[1] = R.drawable.tutorial_mockup1;
        image_res[2] = R.drawable.tutorial_mockup3;
        setUpColors();

        //status bar colors
        tutorialWindow = this.getWindow();
        tutorialWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        tutorialWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        tutorialWindow.setStatusBarColor(colors[0]);

        setUpIndicatorStrip();

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nPager.getCurrentItem() < NUM_PAGES-1) {
                    nPager.setCurrentItem(nPager.getCurrentItem() + 1);
                }
            }
        });


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.client.shourya.periferi",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(DEBUG_TAG, "HashKey: LoginActivity: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }


    @Override
    public void onBackPressed() {
        finish();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    @Override
    public void fblistener(String token, String id1, URL profile_picture, String fname1, String lname1, String sname1,
                           String gender1, String email1, boolean emailAvailable, String dob1,
                           boolean dobAvailable) {
        Log.d(DEBUG_TAG, "fbListener Callback called");
        progressDialog.setMessage("Setting up..");
        progressDialog.show();
        //set data to local variables
        tId = id1;
        tFname = fname1;
        tLname = lname1;
        tSname = sname1;
        tGender = gender1;
        tEmail = email1;
        tDob = dob1;

        //Firebase Auth
        new DownloadImageTask().execute(profile_picture);
        fb_token = token;
        //fireBaseAuthenticate(token);
    }

    private void fireBaseAuthenticate() {
        AuthCredential credential = FacebookAuthProvider.getCredential(fb_token);
        Log.d(DEBUG_TAG, "Firebase Credentials: " + credential.toString());
        Log.d(DEBUG_TAG, "Firebase Auth Instance: " + mAuth.toString());
      //  Log.d(DEBUG_TAG, "Firebase User Instance: " + mFireUser.toString());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(DEBUG_TAG, "Firebase CredentialSignup onComplete called");
                        if (task.isSuccessful()) {
                            Log.d(DEBUG_TAG, "signInWithCredential:success");
                            fUid = mAuth.getCurrentUser().getUid();
                            Log.d(DEBUG_TAG, "FirebaseAuth: " + fUid);
                            //initiateRegistration();
                            uploadProfilePicToFirebase(fUid);
                        } else {
                            Log.d(DEBUG_TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void initiateRegistration(String imgUrl) {
        //Register User::
        final User userForm = new User(fUid, tFname, tLname, tSname, tEmail, tDob, tGender, imgUrl);

        //TODO do in separate thread
        //set SharedPreferences Defaults
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("id", fUid);
        editor.putString("first_name", tFname);
        editor.putString("last_name", tLname);
        editor.putString("short_name", tSname);
        editor.putString("gender", tGender);
        editor.putString("email", tEmail);
        editor.putString("dob", tDob);
        editor.putString("profileUrl", imgUrl);
        editor.putBoolean("rookieFeed", true);
        editor.putBoolean("rookieMaps", true);
        editor.apply();

        //reference String
        Map<String, String> params = new HashMap<String, String>();
        Log.d(DEBUG_TAG, "fireID: " + fUid);
        params.put("userID", fUid);
        final String reference = substituteString(getResources().getString(R.string.user_check), params);
        //TODO redundant data: userID getting stored as the key and as one of the values as well
        create_user = mDatabase.getReference(reference);
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(DEBUG_TAG, dataSnapshot.toString());
                if(dataSnapshot.getValue() == null) {
                    Log.d(DEBUG_TAG, "Creating a new User");
                    create_user.setValue(userForm);
                    mUser = userForm;
                }
                else {
                    Log.d(DEBUG_TAG, "accessing stored user info");
                    mUser = dataSnapshot.getValue(User.class);
                }
                progressDialog.dismiss();
                Intent i = new Intent(LoginActivity.this, LoadingActivity.class);
                startActivity(i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        create_user.addListenerForSingleValueEvent(userListener);

    }

    @Override
    public void nameListener(String a, String b) {
        //TODO
    }

    private void setUpColors(){
        Integer color1 = ResourcesCompat.getColor(getResources(), R.color.tut_1_screen, null);
        Integer color2 = ResourcesCompat.getColor(getResources(), R.color.tut_2_screen, null);
        Integer color3 = ResourcesCompat.getColor(getResources(), R.color.tut_3_screen, null);
        Integer color4 = ResourcesCompat.getColor(getResources(), R.color.tut_4_screen, null);
        Integer[] colors_temp = {color1, color2, color3, color4};
        colors = colors_temp;
    }

    private class LoginSlidePagerAdapter extends FragmentStatePagerAdapter {
        public LoginSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /*switch (position) {
                case 0: return new LoginFragmentOne();
                case 3: return new LoginFragmentFour();
                default: return new LoginFragmentOne();
            }*/
            if(position == 3) {
                return new LoginFragmentOne();
            }
            else{
                Fragment fragment = new TutorialObjectFragment();
                Bundle args = new Bundle();
                args.putInt(TutorialObjectFragment.ARG_PAGE,position);
                fragment.setArguments(args);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private class AnimateColorTransition implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(position < (NUM_PAGES - 2)) {
                nPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]));
            }
            else if(position == 2) {
                //pixel_mockup.setImageAlpha((int)(positionOffset * 255));
                //mobile_pager_screen.setAlpha(positionOffset);
                //Log.d(DEBUG_TAG, "position 2");
                //pixel_mockup.setImageAlpha((int)((1-positionOffset)*255));
                pixel_mockup.setTranslationX(-1 * positionOffsetPixels);
                mobile_pager_screen.setTranslationX(-1 * positionOffsetPixels);

                //mobile_pager_screen.setAlpha(1-positionOffset);
                nPager.setBackgroundColor((Integer) argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]));
            }
            else {
                // the last page color
                //pixel_mockup.setImageAlpha((int)(positionOffset * 255));
                //mobile_pager_screen.setAlpha(positionOffset);
                nPager.setBackgroundColor(colors[colors.length - 1]);
            }
            //Log.d(DEBUG_TAG, "Position: " + position + "offset: " + positionOffset + "offsetPixels: " + positionOffsetPixels);
            //sPager.beginFakeDrag();
            //sPager.fakeDragBy(positionOffset);
            //sPager.endFakeDrag();
            //sPager.se
        }

        @Override
        public void onPageSelected(int position) {
            tutorialWindow.setStatusBarColor(colors[position]);
            for(int i=0; i< NUM_PAGES; i++) {
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.pager_indicator_not_selected, null));
            }
            dots[position].setImageDrawable(getResources().getDrawable(R.drawable.pager_indicator_selected, null));

            if(position < 3) {
                next_button.setVisibility(View.VISIBLE);
                sPager.setCurrentItem(position);
            }
            else{
                next_button.setVisibility(View.GONE);
                //pixel_mockup.setVisibility(View.GONE);
                //mobile_pager_screen.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            /*if(state == ViewPager.SCROLL_STATE_IDLE) {
                sPager.endFakeDrag();
            }
            else if(state == ViewPager.SCROLL_STATE_DRAGGING){
                sPager.beginFakeDrag();
            }*/
        }
    }

    private class MobileScreenPagerAdapter extends FragmentStatePagerAdapter {
        public MobileScreenPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new WireframeFragment();
            Bundle args = new Bundle();
            args.putInt(WireframeFragment.ARG_PAGE,position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES-1;
        }
    }

    public static class TutorialObjectFragment extends Fragment{
        public static final String ARG_PAGE = "obj";
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.welcome_tutorial,container,false);
            Bundle args = getArguments();
            int position = args.getInt(ARG_PAGE);
            //((ImageView)rootView.findViewById(R.id.tut_image)).setImageResource(image_res[position]);
            ((TextView) rootView.findViewById(R.id.welcome_text)).setText(tut_text[position]);
            //((TextView) rootView.findViewById(R.id.textView1)).setText(Integer.toString(args.getInt(ARG_PAGE)));
            return rootView;
        }
    }

    public static class WireframeFragment extends Fragment {
        public static final String ARG_PAGE = "obj2";
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View wireframe = inflater.inflate(R.layout.wireframe_fragment,container,false);
            if(getArguments().getInt(ARG_PAGE) < 3) {
                Log.d(DEBUG_TAG, "wireframe position: " + getArguments().getInt(ARG_PAGE));
                ((ImageView) wireframe.findViewById(R.id.screen_image)).setImageResource(image_res[getArguments().getInt(ARG_PAGE)]);
            }
            return wireframe;
        }
    }

    private void uploadProfilePicToFirebase(String userId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(userProfilePic != null) {
            userProfilePic.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();
            final StorageReference uploadRef = store_profile_picture.child(userId+".jpeg");
            UploadTask uploadTask = uploadRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(DEBUG_TAG, "Failed to upload to firebase");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(DEBUG_TAG,"Uploaded File successfully");
                    fetchStorageUri(uploadRef);
                }
            });
        }
        else{
            Log.d(DEBUG_TAG, "Profile picture not received in inputstream");
        }

    }

    private void fetchStorageUri(StorageReference ref) {
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                initiateRegistration(uri.toString());
                Log.d(DEBUG_TAG, "Profile_picture rl: "+ uri);
            }
        });

    }

    private void setUpIndicatorStrip() {
        pagerIndicatorStrip = (LinearLayout)findViewById(R.id.viewPagerIndicatorStrip);
        dots = new ImageView[NUM_PAGES];
        for (int i = 0; i < NUM_PAGES; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.pager_indicator_not_selected, null));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(28, 18, 28, 18);
            pagerIndicatorStrip.addView(dots[i], params);
        }
        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.pager_indicator_selected, null));
    }

    private class DownloadImageTask extends AsyncTask<URL, Void, Bitmap> {

        public DownloadImageTask() {
            //this.bmImage = bmImage;
        }

        protected void onPreExecute() {
            //mDialog = ProgressDialog.show(ChartActivity.this,"Please wait...", "Retrieving data ...", true);
        }

        protected Bitmap doInBackground(URL...urls) {
            Log.d(DEBUG_TAG, "Fetching image");
            URL a = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = a.openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", "image download error");
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //set image of your imageview
            userProfilePic = result;
            Log.d(DEBUG_TAG, "Image fetched, authenticating token");
            fireBaseAuthenticate();
        }
    }
}
