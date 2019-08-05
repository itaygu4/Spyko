package com.example.itayg.spykomusic;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1234;

    DatabaseReference playlistsRef;

    private DrawerLayout mDrawerLayout;

    private RecyclerView recyclerView;

    private ArrayList<String> playlistIDs = new ArrayList<>();

    private int clickedNavItem = 0;

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            playlistIDs = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                playlistIDs.add(snapshot.getKey());
            }
            Log.e("playlists",playlistIDs.size() + "");

            PlaylistsAdapter adapter = new PlaylistsAdapter(MainActivity.this, playlistIDs, FirebaseAuth.getInstance().getCurrentUser().getUid());
            //PlaylistViewAdapter adapter = new PlaylistViewAdapter(MainActivity.this, playlistIDs, FirebaseAuth.getInstance().getCurrentUser().getUid());
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));


            Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        setUpRecyclerView();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        playlistsRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("playlists");

        playlistsRef.addValueEventListener(listener);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        final ImageView drawerAccountImageView = findViewById(R.id.drawer_image);
        drawerAccountImageView.setImageResource(R.drawable.user);


        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");



        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView nameView = mDrawerLayout.findViewById(R.id.drawer_name);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){


                    User user = snapshot.getValue(User.class);


                    if ((user != null) && user.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        String nickname = user.nickname;
                        nameView.setText(nickname);


                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()+".jpg");
        Log.v("result",storageReference.toString());



        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                RequestBuilder<Drawable> i = Glide.with(MainActivity.this).load(uri);
                i.apply(RequestOptions.circleCropTransform()).into(drawerAccountImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });




        NavigationView navigationView = findViewById(R.id.nav_view);
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);


        navigationView.bringToFront();
        navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);


                        clickedNavItem = menuItem.getItemId();


                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (R.id.nav_signout == clickedNavItem){
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent (MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                if (R.id.nav_account == clickedNavItem){
                    Intent intent = new Intent (MainActivity.this, AccountActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                if (R.id.nav_search_for_friends == clickedNavItem){
                    Intent intent = new Intent (MainActivity.this, FollowingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                if (R.id.nav_followers == clickedNavItem){
                    Intent intent = new Intent (MainActivity.this, FollowersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                if(R.id.nav_follow_requests == clickedNavItem){
                    Intent intent = new Intent (MainActivity.this, FollowRequestsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }

                if(R.id.nav_settings == clickedNavItem){
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        RelativeLayout mylayout = findViewById(R.id.content_layout);
        FrameLayout frame = new FrameLayout(this);

        frame.setLayoutParams(new LinearLayout.LayoutParams(
                80,
                80
        ));

        frame.setBackgroundResource(R.drawable.circle);

        mylayout.addView(frame);


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

    public void PlaylistClick(View view) {
        playlistsRef.removeEventListener(listener);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
        DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists");
        String id = playlistRef.push().getKey();
        DatabaseReference playlistNameRef = playlistRef.child(id).child("name");
        playlistNameRef.setValue("New playlist");
        intent.putExtra("playlist_id", id);
        intent.putExtra("uid", uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE){
            playlistsRef.addValueEventListener(listener);
        }

    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.playlists);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecyclerViewItemDecorator(2, 1, false));
    }


    public void buttonClick(View view) {
        ExpandableRelativeLayout expandableLayout  =  findViewById(R.id.expandableLayout);

        expandableLayout.toggle();

    }

    @Override
    protected void onRestart() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
        super.onRestart();
    }
}
