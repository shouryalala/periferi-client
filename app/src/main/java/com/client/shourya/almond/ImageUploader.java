package com.client.shourya.almond;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;

public class ImageUploader{
    private final static String DEBUG_TAG = "AlmondLog:: " + ImageUploader.class.getSimpleName();
    StorageReference storageRef;
    String[] filePathColumn = { MediaStore.Images.Media.DATA };
    Bitmap sImage;
    private final String FEED_CHILD_PATH = "feed";
    private UploaderTask task;
    private ContentResolver contentResolver;
    private String userId;
    public interface UploaderTask{
        public void onUploadComplete(String downloadUrl);
        public void onFailed();
    }

    public ImageUploader(String userId, ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.userId = userId;
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void setUploadTask(UploaderTask task){
        this.task = task;
    }

    public void uploadImage(Uri imageUri) {
        // Get the cursor
        Cursor cursor = contentResolver.query(imageUri, filePathColumn, null, null, null);        // Move to first row
        if(cursor == null) {
            if(this.task != null)task.onFailed();
            return;
        }
        cursor.moveToFirst();
        //Get the column index of MediaStore.Images.Media.DATA
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        //Gets the String value in the column
        String imgDecodableString = cursor.getString(columnIndex);
        if(imgDecodableString == null || imgDecodableString.isEmpty()){
            if(this.task != null)task.onFailed();
            return;
        }
        sImage = BitmapFactory.decodeFile(imgDecodableString);
        cursor.close();
        initiateUpload(sImage);
    }

    private void initiateUpload(Bitmap image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if(image == null) {
            if(this.task != null)task.onFailed();
            return;
        }
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        final StorageReference uploadRef = storageRef.child(FEED_CHILD_PATH).child(userId+".jpeg");
        UploadTask uploadTask = uploadRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(task != null)task.onFailed();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(DEBUG_TAG,"Uploaded File successfully");
                onFetchDownloadUrl(uploadRef);
            }
        });

    }

    private void onFetchDownloadUrl(StorageReference ref) {
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(DEBUG_TAG, "Fetched download url successfully: " + uri.toString());
                if(task != null)task.onUploadComplete(uri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(task != null)task.onFailed();
            }
        });
    }
}
