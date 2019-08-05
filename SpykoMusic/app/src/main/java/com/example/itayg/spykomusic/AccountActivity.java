package com.example.itayg.spykomusic;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.tinify.Tinify;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.google.firebase.auth.FirebaseAuth.getInstance;


public class AccountActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    public static final int PICK_IMAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;
    private static DatabaseReference ref;
    private StorageReference mStorageRef;
    ImageView newPhotoView;
    ImageView drawerAccountImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        drawerAccountImageView = findViewById(R.id.drawer_image);
        newPhotoView = findViewById(R.id.new_photo);
        registerForContextMenu(newPhotoView);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Tinify.setKey("_6eGbrELTCQhsnNfnBpsumX12GhbBl19");

        Intent myIntent = getIntent();
        String action = myIntent.getStringExtra("action");

        Toolbar toolbar = findViewById(R.id.toolbar_account);
        if(action != null && action.equals("back")){
            toolbar.setVisibility(View.GONE);
            ImageView imageView = findViewById(R.id.back);
            imageView.setVisibility(View.VISIBLE);
        }
        else {
            setSupportActionBar(toolbar);
            ActionBar actionbar = getSupportActionBar();
            if (actionbar != null) {
                actionbar.setDisplayHomeAsUpEnabled(true);
                actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        }


        mDrawerLayout = findViewById(R.id.drawer_layout);





        ref = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView accountNicknameView = findViewById(R.id.account_nickname);
                TextView accountNameView = findViewById(R.id.account_name);
                TextView nameView = mDrawerLayout.findViewById(R.id.drawer_name);
                String fullName;
                String nickname;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if(snapshot.getKey().equals("fullName")) {
                        fullName = snapshot.getValue().toString();
                        accountNameView.setText(fullName);
                    }

                    if(snapshot.getKey().equals("nickname")){
                        nickname = snapshot.getValue().toString();
                        nameView.setText(nickname);
                        accountNicknameView.setText(nickname);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        setImages(drawerAccountImageView);

        DatabaseReference followingRef = ref.child("Following");

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getValue().toString().equals("true"))
                        count++;
                }
                TextView followingCount = findViewById(R.id.num_following);
                followingCount.setText(count + " following");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference followersRef = ref.child("Followers");

        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getValue().toString().equals("true"))
                        count++;
                }
                TextView followingCount = findViewById(R.id.num_followers);
                followingCount.setText(count + " followers");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        if (R.id.nav_signout == menuItem.getItemId()) {
                            getInstance().signOut();
                            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_playlists == menuItem.getItemId()) {
                            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_search_for_friends == menuItem.getItemId()) {
                            Intent intent = new Intent(AccountActivity.this, FollowingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_followers == menuItem.getItemId()){
                            Intent intent = new Intent (AccountActivity.this, FollowersActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_follow_requests == menuItem.getItemId()){
                            Intent intent = new Intent (AccountActivity.this, FollowRequestsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_settings == menuItem.getItemId()){
                            Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        return true;
                    }
                });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Intent getIntent;
    Intent chooserIntent;

    public void newPhotoClick(View view) {

        openContextMenu(newPhotoView);



    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int redColor = Color.parseColor("#FF5A5F");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    new StyleableToast.Builder(AccountActivity.this)
                            .text("Can't set new picture")
                            .backgroundColor(redColor)
                            .show();
                }
                return;
            }


        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String picturePath;
        if (requestCode == PICK_IMAGE) {
            if (data == null) {
                return;
            }

            Uri selectedImageUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            try {
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(projection[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();

                ImageView accountImageView = findViewById(R.id.user_image);






                /*Source source = Tinify.fromFile(new File(picturePath).getPath());
                source.toFile(new File(getFilesDir().getPath() + "profilePicture.jpg").getPath());
                Uri file = Uri.fromFile(new File(getFilesDir().getPath() + "profilePicture.jpg"));*/
                Uri file = Uri.fromFile(new File(picturePath));
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                StorageReference riversRef = mStorageRef.child(uid+".jpg");

                riversRef.putFile(file)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                ImageView drawerAccountImageView = findViewById(R.id.drawer_image);
                                setImages(drawerAccountImageView);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });

            }
            catch(Exception e) {
                Log.e("Path Error", e.toString());
            }




        }
    }


    private void setImages (final ImageView drawerAccountImage){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()+".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView accountImageView = findViewById(R.id.user_image);
                RequestBuilder<Drawable> i = Glide.with(AccountActivity.this).load(uri);
                i.apply(RequestOptions.circleCropTransform()).into(drawerAccountImage);
                i.apply(RequestOptions.circleCropTransform()).into(accountImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                drawerAccountImage.setImageResource(R.drawable.user);
            }
        });
    }

    @Override
    protected void onRestart() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
        super.onRestart();
    }


    public void followingClick(View view) {
        Intent intent = new Intent(this, FollowingActivity.class);
        startActivity(intent);
    }

    public void followersClick(View view) {
        Intent intent = new Intent(this, FollowersActivity.class);
        startActivity(intent);
    }

    public void backClick(View view) {
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_photo_click_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_photo:
                getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_STORAGE);

                }
                else{
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }
                return true;
            case R.id.remove_photo:
                drawerAccountImageView.setImageResource(R.drawable.user);
                ImageView accountImageView = findViewById(R.id.user_image);
                accountImageView.setImageResource(R.drawable.user);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()+".jpg");
                storageReference.delete();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
