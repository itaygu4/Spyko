package com.example.itayg.spykomusic;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowingOrFollowersInfo extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> users_uid;
    ArrayList<String> member_names;
    ArrayList<RowItem> rowItems;
    ArrayList<String> accounts_nicknames;
    ArrayList <String> actuallyFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_or_followers_info);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Intent myIntent = getIntent();
        String uid = myIntent.getStringExtra("uid");
        final String content = myIntent.getStringExtra("content");

        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child(content);
        final DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference().child("users");

        ValueEventListener uidListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users_uid = new ArrayList<>();
                member_names = new ArrayList<>();
                accounts_nicknames = new ArrayList<>();
                actuallyFollowing = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(!users_uid.contains(snapshot.getKey()))
                        users_uid.add(snapshot.getKey());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }




        };

        ValueEventListener namesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);


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
                                if(content.equals("Following"))
                                    actuallyFollowing.add(snapshot.getValue().toString());
                                else
                                    actuallyFollowing.add(null);

                            }
                        }
                    }
                }

                rowItems = new ArrayList<>();

                for (int i = 0; i < users_uid.size(); i++) {
                    RowItem item = new RowItem(users_uid.get(i), member_names.get(i), accounts_nicknames.get(i), actuallyFollowing.get(i));
                    rowItems.add(item);


                }

                FollowingCustomAdapter adapter = new FollowingCustomAdapter(FollowingOrFollowersInfo.this, rowItems);
                recyclerView.setAdapter(adapter);


            }




            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        friendsRef.addValueEventListener(uidListener);


        accountRef.addValueEventListener(namesListener);

        setUpRecyclerView();
    }

    public void backClick(View view) {
        finish();
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }
}
