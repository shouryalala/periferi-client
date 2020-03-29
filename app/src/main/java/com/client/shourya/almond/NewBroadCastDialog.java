package com.client.shourya.almond;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.client.shourya.almond.data.BroadCast;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import static com.client.shourya.almond.BaseActivity.GALLERY_REQUEST_CODE;

/*
 * Created by shourya on 20/6/17.
 */

public class NewBroadCastDialog extends DialogFragment{
    private static final String DEBUG_TAG = "AlmondLog:: " + NewBroadCastDialog.class.getSimpleName();
    private static final String REQUIRED = "Required";
    private static final String UNAVAILABLE = "Unavailable";
    //EditText fTitle;
    TextView cCircle;
    EditText fBody;
    TextView submit;
    public View inf2;
    DatabaseReference userPosts, allPosts;
    private String currentCircle;
    private Uri selectedImage;
    public static final int CITY_INDEX = 69;
    public static final int COUNTRY_INDEX = 420;

    public NewBroadCastDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        inf2 = inflater.inflate(R.layout.new_broadcast_dialog, null);
        cCircle = (TextView) inf2.findViewById(R.id.current_circle);
        currentCircle = getCircleText();
        cCircle.setText(currentCircle);
        fBody = (EditText) inf2.findViewById(R.id.field_body);
        submit = (TextView) inf2.findViewById(R.id.broadcast_send);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPush();
            }
        });
        builder.setView(inf2);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        //View view = inflater.inflate(R.layout.new_broadcast_dialog, container, false);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private String getCircleText() {
        if(BaseActivity.currCircle == COUNTRY_INDEX) {
             return BaseActivity.locationDetails.getCountryName();
        }
        else if(BaseActivity.currCircle == CITY_INDEX){
            return BaseActivity.locationDetails.getLocalityName();
        }
        else{
            //temp
            int x = BaseActivity.currCircle;
            return BaseActivity.locationDetails.zonesList.get(x).zoneBounds.getZoneName();
        }
    }

    private String formReference(int circle) {
        Map<String, String> params = new HashMap<>();
        String reference;
        switch(circle) {
            case COUNTRY_INDEX: {
                //country users
                params.put("countryID", BaseActivity.locationDetails.getCountryID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_country), params);
                break;
            }
            case CITY_INDEX:{
                //city users
                params.put("cityID", BaseActivity.locationDetails.getCityID());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_city), params);
                break;
            }
            default:{
                //zone
                params.put("zoneID", BaseActivity.locationDetails.zonesList.get(circle).getZoneKey());
                reference = BaseActivity.substituteString(getResources().getString(R.string.all_broadcasts_zone), params);
                break;
            }
        }
        return reference;
    }

    private void onPush() {
        //final String title = fTitle.getText().toString();
        final String body = fBody.getText().toString();
        // Body is required
        if (TextUtils.isEmpty(body) && selectedImage == null) {
            fBody.setError(REQUIRED);
            return;
        }

        if(selectedImage != null) {
            BaseActivity.imageUploader.setUploadTask(new ImageUploader.UploaderTask() {
                @Override
                public void onUploadComplete(String downloadUrl) {
                    pushBroadcast(body, downloadUrl);
                }

                @Override
                public void onFailed() {
                    Toast.makeText(getActivity(), "Failed to upload image. Please try again soon", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
            BaseActivity.imageUploader.uploadImage(selectedImage);
        }
        else{
            pushBroadcast(body, null);
        }
    }

    private void pushBroadcast(String message, String imageUrl) {
        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getActivity().getApplicationContext(), "Posting...", Toast.LENGTH_SHORT).show();

        //for all broadcasts
        final String bReference = formReference(BaseActivity.currCircle);

        //for all current user broadcasts
        Map<String, String> params = new HashMap<String, String>();
        params.put("userID", BaseActivity.mUser.getUserId());
        final String bUserReference = BaseActivity.substituteString(getResources().getString(R.string.user_broadcasts), params);
        //userPosts = BaseActivity.mDatabase.getReference(bUserReference);

        //create Reference and get key for later use
        allPosts = BaseActivity.mDatabase.getReference(bReference);
        String key = allPosts.push().getKey();

        BroadCast new_bc = new BroadCast(params.get("userID"), BaseActivity.mUser.getDisplayName(),
                currentCircle, message, BaseActivity.mUser.getImgUrl(), imageUrl);
        Map<String, Object> postValues = new_bc.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(bReference + "/" + key, postValues);
        childUpdates.put(bUserReference + "/" + key, new_bc.circle);

        BaseActivity.mDatabase.getReference().updateChildren(childUpdates);
        setEditingEnabled(true);
        dismiss();
    }

    private void pickImage() {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent,GALLERY_REQUEST_CODE);
    }

    private void setEditingEnabled(boolean enabled) {
        //fTitle.setEnabled(enabled);
        fBody.setEnabled(enabled);
        if (enabled) {
            submit.setVisibility(View.VISIBLE);
        } else {
            submit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            this.selectedImage = data.getData();
        }
        else super.onActivityResult(requestCode, resultCode, data);
    }
}
