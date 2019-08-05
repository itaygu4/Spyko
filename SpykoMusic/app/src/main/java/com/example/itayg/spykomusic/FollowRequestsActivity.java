package com.example.itayg.spykomusic;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
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

public class FollowRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_requests);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        setUpRecyclerView();

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        final ImageView drawerAccountImageView = findViewById(R.id.drawer_image);

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
                RequestBuilder<Drawable> i = Glide.with(FollowRequestsActivity.this).load(uri);
                i.apply(RequestOptions.circleCropTransform()).into(drawerAccountImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });

        final ArrayList<String> requestsUid = new ArrayList<>();
        final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followersRef.removeEventListener(this);
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getValue().equals("false")){
                        requestsUid.add(data.getKey());
                    }
                }
                RequestingToFollowAdapter adapter = new RequestingToFollowAdapter(FollowRequestsActivity.this, requestsUid, FirebaseAuth.getInstance().getCurrentUser().getUid());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.bringToFront();
        navigationView.getMenu().findItem(R.id.nav_follow_requests).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        if (R.id.nav_signout == menuItem.getItemId()){
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent (FollowRequestsActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_account == menuItem.getItemId()){
                            Intent intent = new Intent (FollowRequestsActivity.this, AccountActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_search_for_friends == menuItem.getItemId()){
                            Intent intent = new Intent (FollowRequestsActivity.this, FollowingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_followers == menuItem.getItemId()){
                            Intent intent = new Intent (FollowRequestsActivity.this, FollowersActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_follow_requests == menuItem.getItemId()){
                            Intent intent = new Intent (FollowRequestsActivity.this, FollowRequestsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_settings == menuItem.getItemId()){
                            Intent intent = new Intent(FollowRequestsActivity.this, SettingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_playlists == menuItem.getItemId()) {
                            Intent intent = new Intent(FollowRequestsActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        return true;
                    }
                });
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.follow_requests_recycler_view);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecyclerViewItemDecorator(2, 15, true));
    }

    @Override
    protected void onRestart() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_follow_requests).setChecked(true);
        super.onRestart();
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
}
