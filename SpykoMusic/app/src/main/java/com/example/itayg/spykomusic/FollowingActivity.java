package com.example.itayg.spykomusic;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TabWidget;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.github.glomadrian.grav.GravView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.otaliastudios.autocomplete.Autocomplete;

import java.util.ArrayList;
import java.util.List;


public class FollowingActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    ArrayList <String> actuallyFollowing = new ArrayList<>();
    ArrayList<String> member_names = new ArrayList<>();
    ArrayList<String> users_uid = new ArrayList<>();
    ArrayList<String> accounts_nicknames = new ArrayList<>();

    ValueEventListener uidListener;
    ValueEventListener namesListener;



    ArrayList<RowItem> rowItems = new ArrayList<>();
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_search);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Toolbar toolbar = findViewById(R.id.toolbar_add_friends);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        setUpRecyclerView();



        mDrawerLayout = findViewById(R.id.drawer_layout);
        final DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference().child("users");


        final ImageView drawerAccountImageView = findViewById(R.id.drawer_image);
        drawerAccountImageView.setImageResource(R.drawable.user);

        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");


        uidListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users_uid = new ArrayList<>();
                member_names = new ArrayList<>();
                accounts_nicknames = new ArrayList<>();
                actuallyFollowing = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(!users_uid.contains(snapshot.getKey())) {
                        users_uid.add(snapshot.getKey());
                        actuallyFollowing.add(snapshot.getValue().toString());
                    }
                    friendsRef.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }




        };

        namesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                TextView nameView = mDrawerLayout.findViewById(R.id.drawer_name);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);

                    /*check if the user wasn't added before*/


                    if ((user != null) && user.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        String nickname = user.nickname;
                        nameView.setText(nickname);


                    }
                    String nickname;
                    if(user != null) {
                        nickname = user.nickname;
                        String fullName = user.fullName;

                        for(int i=0; i<users_uid.size(); i++){
                            for(int j=i+1; j<users_uid.size(); j++){
                                if(users_uid.get(i).equals(users_uid.get(j)))
                                    users_uid.remove(j);
                            }
                        }

                        for (int i = 0; i < users_uid.size(); i++) {
                            if (users_uid.get(i).equals(snapshot.getKey())) {
                                member_names.add(fullName);
                                accounts_nicknames.add(nickname);
                            }
                        }
                    }
                }

                rowItems = new ArrayList<>();

                for (int i = 0; i < users_uid.size(); i++) {
                    RowItem item = new RowItem(users_uid.get(i), member_names.get(i), accounts_nicknames.get(i), actuallyFollowing.get(i));
                    rowItems.add(item);


                }

                RelativeLayout mRelativeLayout = findViewById(R.id.zero_following_text);

                if(users_uid.size() == 0){
                    mRelativeLayout.setVisibility(View.VISIBLE);
                }
                else{
                    mRelativeLayout.setVisibility(View.GONE);
                }

                FollowingCustomAdapter adapter = new FollowingCustomAdapter(FollowingActivity.this, rowItems);
                recyclerView.setAdapter(adapter);

                accountRef.removeEventListener(this);


            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        friendsRef.addValueEventListener(uidListener);


        accountRef.addValueEventListener(namesListener);



        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()+".jpg");



        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                RequestBuilder<Drawable> i = Glide.with(FollowingActivity.this).load(uri);
                i.apply(RequestOptions.circleCropTransform()).into(drawerAccountImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.getMenu().findItem(R.id.nav_search_for_friends).setChecked(true);
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

                        if (R.id.nav_signout == menuItem.getItemId()) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(FollowingActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_playlists == menuItem.getItemId()) {
                            Intent intent = new Intent(FollowingActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_account == menuItem.getItemId()){
                            Intent intent = new Intent (FollowingActivity.this, AccountActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if (R.id.nav_followers == menuItem.getItemId()){
                            Intent intent = new Intent (FollowingActivity.this, FollowersActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_follow_requests == menuItem.getItemId()){
                            Intent intent = new Intent (FollowingActivity.this, FollowRequestsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                        }

                        if(R.id.nav_settings == menuItem.getItemId()){
                            Intent intent = new Intent(FollowingActivity.this, SettingsActivity.class);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        HelperMethods.changeSearchViewTextColor(searchView);

        MenuItem item = menu.findItem(R.id.action_search);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                RecyclerView recyclerView = findViewById(R.id.following_recycler_view);
                recyclerView.setVisibility(View.INVISIBLE);
                return true;
            }
        });


        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.clearFocus();
                RecyclerView recyclerView = findViewById(R.id.following_recycler_view);
                recyclerView.setVisibility(View.VISIBLE);
                return true;
            }
        });




        return true;
    }

    @Override
    protected void onRestart() {
        final DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference().child("users");

        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");

        friendsRef.addValueEventListener(uidListener);
        accountRef.addValueEventListener(namesListener);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_search_for_friends).setChecked(true);



        super.onRestart();
    }


    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.following_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

}
