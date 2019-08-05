package com.example.itayg.spykomusic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class HelperMethods {

    public static void setPicture(String picturePath, ImageView imageView, DatabaseReference ref, Context context){
        /*RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), getBitmap(picturePath));
        roundDrawable.setCircular(true);

        // Apply it to an ImageView.
        imageView.setImageDrawable(roundDrawable);*/

        Glide.with(context).load(getBitmap(picturePath)).apply(RequestOptions.circleCropTransform()).into(imageView);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if(ref.child("users").child(fbUser.getUid()).child("picturePath") == null)
            Log.v("Error","user is null");
        ref.child(fbUser.getUid()).child("picturePath").setValue(picturePath);
    }

    public static void setPicture(Bitmap bitmap, ImageView imageView, Context context){
        /*RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundDrawable.setCircular(true);


        // Apply it to an ImageView.
        imageView.setImageDrawable(roundDrawable);*/

        Glide.with(context).load(bitmap).apply(RequestOptions.circleCropTransform()).into(imageView);

    }

    public static Bitmap getBitmap(String path) {
        try {
            Bitmap bitmap = null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return getResizedBitmap(myBitmap,50,50);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }


    public static void changeSearchViewTextColor(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
                ((TextView) view).setHintTextColor(Color.rgb(168, 168, 168));
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



}
