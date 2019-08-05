package com.example.itayg.spykomusic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;

import java.net.Socket;
import java.util.ArrayList;

public class OtherPeoplesAccountActivity extends AppCompatActivity {

    String uid;

    Socket socket;

    boolean privateProfile = false, found = false;

    private RecyclerView recyclerView;

    DatabaseReference playlistsRef;

    private ArrayList<String> playlistIDs = new ArrayList<>();
    private boolean cancelAnimation = false;

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            playlistIDs = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                playlistIDs.add(snapshot.getKey());
            }
            Log.e("playlists",playlistIDs.size() + "");

            //PlaylistViewAdapter adapter = new PlaylistViewAdapter(OtherPeoplesAccountActivity.this, playlistIDs, uid);
            PlaylistsAdapter adapter = new PlaylistsAdapter(OtherPeoplesAccountActivity.this, playlistIDs, uid);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_peoples_account);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Intent intent = getIntent();
        uid = intent.getStringExtra("user_uid");

        final float growTo = 1.2f;
        final long duration = 1200;

        final ScaleAnimation growAnim = new ScaleAnimation(1, growTo, 1, growTo,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(growTo, 1, growTo, 1,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        final ImageView listenImage = findViewById(R.id.listen_image);
        final ToolTipRelativeLayout toolTipRelativeLayout = findViewById(R.id.listen_tooltip);
        final ToolTip toolTip = new ToolTip()
                .withText("This user is live!")
                .withColor(Color.LTGRAY)
                .withShadow()
                .withAnimationType(ToolTip.AnimationType.FROM_TOP);




        RelativeLayout mainLayout = findViewById(R.id.scroll_view);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTip.withAnimationType(ToolTip.AnimationType.NONE);
                toolTipRelativeLayout.setVisibility(View.GONE);
            }
        });




        setUpRecyclerView();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        playlistsRef = ref.child("playlists");

        playlistsRef.addValueEventListener(listener);





        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView otherPeopleAccountNameView = findViewById(R.id.other_people_account_name);
                TextView otherPeopleAccountNicknameView = findViewById(R.id.other_people_account_nickname);



                User user = dataSnapshot.getValue(User.class);
                if ((user != null)) {
                    String fullName = user.fullName;

                    otherPeopleAccountNameView.setText(fullName);
                    otherPeopleAccountNicknameView.setText(user.nickname);


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                TextView followersCount = findViewById(R.id.num_followers);
                followersCount.setText(count + " followers");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ImageView plusView = findViewById(R.id.plus_add_friend);
                TextView addFriendTextView = findViewById(R.id.add_friend_text);
                ImageView privateProfileView = findViewById(R.id.private_profile);
                TextView followingCount = findViewById(R.id.num_following);
                TextView followersCount = findViewById(R.id.num_followers);
                found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals(uid)){
                        found = true;
                        if (snapshot.getValue().toString().equals("false")){
                            privateProfile = true;
                            plusView.setImageResource(R.drawable.requested2);
                            addFriendTextView.setText("Requested");
                            privateProfileView.setVisibility(View.VISIBLE);
                            followingCount.setClickable(false);
                            followersCount.setClickable(false);
                            break;
                        }
                        else {
                            privateProfile = false;
                            plusView.setImageResource(R.drawable.redx);
                            addFriendTextView.setText("Unfollow");
                            privateProfileView.setVisibility(View.GONE);
                            followingCount.setClickable(true);
                            followersCount.setClickable(true);
                            break;
                        }
                    }

                }
                if(!found){
                    DatabaseReference privateRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("private");
                    privateRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals("true")) {
                                ImageView privateProfileView = findViewById(R.id.private_profile);
                                privateProfileView.setVisibility(View.VISIBLE);
                                privateProfile = true;
                                DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("live");
                                liveRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        growAnim.cancel();
                                        shrinkAnim.cancel();
                                        listenImage.setVisibility(View.GONE);
                                        toolTipRelativeLayout.setVisibility(View.GONE);
                                        cancelAnimation = true;


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("live");
                liveRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(privateProfile || (dataSnapshot.getValue() == null || (dataSnapshot.getValue() != null && dataSnapshot.getValue().equals("false")))){
                            growAnim.cancel();
                            shrinkAnim.cancel();
                            listenImage.setVisibility(View.GONE);
                            toolTipRelativeLayout.setVisibility(View.GONE);
                            cancelAnimation = true;

                        }
                        else{
                            cancelAnimation = false;
                            listenImage.setVisibility(View.VISIBLE);
                            toolTipRelativeLayout.setVisibility(View.VISIBLE);
                            toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.listen_image));
                            growAnim.setDuration(2000);
                            shrinkAnim.setDuration(2000);


                            listenImage.setAnimation(growAnim);
                            growAnim.start();

                            growAnim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if(!cancelAnimation) {
                                        listenImage.setAnimation(shrinkAnim);
                                        shrinkAnim.start();
                                    }
                                }
                            });
                            shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if(!cancelAnimation) {
                                        listenImage.setAnimation(growAnim);
                                        growAnim.start();
                                    }
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(uid + ".jpg");
        Log.v("result", storageReference.toString());


        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView otherPeopleAccountImageView = findViewById(R.id.other_user_image);
                RequestBuilder<Drawable> i = Glide.with(OtherPeoplesAccountActivity.this).load(uri);
                i.apply(RequestOptions.circleCropTransform()).into(otherPeopleAccountImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });


    }

    public void backClick(View view) {
        finish();
    }

    public void addFriendClick(View view) {
        final ImageView plusView = findViewById(R.id.plus_add_friend);
        final TextView addFriendTextView = findViewById(R.id.add_friend_text);

        if(addFriendTextView.getText().equals("Follow")) {

            DatabaseReference privateRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("private");
            privateRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null && dataSnapshot.getValue().toString().equals("true")){
                        plusView.setImageResource(R.drawable.requested2);
                        addFriendTextView.setText("Requested");
                        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");

                        followingRef.child(uid).setValue("false");

                        final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Followers");

                        followersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("false");
                    }
                    else{
                        plusView.setImageResource(R.drawable.redx);
                        addFriendTextView.setText("Unfollow");

                        final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");

                        followingRef.child(uid).setValue("true");

                        final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Followers");

                        followersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("true");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        else{


            plusView.setImageResource(R.drawable.plus);
            addFriendTextView.setText("Follow");

            final DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");
            final DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Followers");


            followingRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if (snapshot.getKey().equals(uid)){
                            followingRef.child(uid).removeValue();
                            followersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        }
                    }
                    followingRef.removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    followingRef.removeEventListener(this);
                }
            });
        }

    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.other_account_playlists);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecyclerViewItemDecorator(2, 2, false));
    }

    public void buttonClick(View view) {
        ExpandableRelativeLayout expandableLayout  =  findViewById(R.id.expandableLayout);
        ExpandableRelativeLayout emptyExpandableLayout = findViewById(R.id.expandableEmptyLayout);
        ExpandableRelativeLayout currentLayout;



        ImageView dropdown = findViewById(R.id.drop_down);
        ImageView dropup = findViewById(R.id.drop_up);

        TextView textView = findViewById(R.id.other_people_account_nickname);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        if(playlistIDs.size() == 0 || privateProfile) {
            expandableLayout.setVisibility(View.GONE);
            emptyExpandableLayout.setVisibility(View.VISIBLE);
            currentLayout = emptyExpandableLayout;
        }
        else {
            emptyExpandableLayout.setVisibility(View.GONE);
            expandableLayout.setVisibility(View.VISIBLE);
            currentLayout = expandableLayout;
        }

        params.addRule(RelativeLayout.BELOW, currentLayout.getId());
        params.topMargin = 85;
        textView.setLayoutParams(params);
        currentLayout.toggle();
        if(currentLayout.isExpanded()) {
            dropdown.setVisibility(View.VISIBLE);
            dropup.setVisibility(View.GONE);

        }
        else{
            dropdown.setVisibility(View.GONE);
            dropup.setVisibility(View.VISIBLE);
        }

    }

    public void followingClick(View view) {
        Intent intent = new Intent(this, FollowingOrFollowersInfo.class);
        intent.putExtra("uid", uid);
        intent.putExtra("content", "Following");
        startActivity(intent);
    }

    public void followersClick(View view) {
        Intent intent = new Intent(this, FollowingOrFollowersInfo.class);
        intent.putExtra("uid", uid);
        intent.putExtra("content", "Followers");
        startActivity(intent);
    }

    public void listenClick(View view) {
        Intent intent = new Intent(OtherPeoplesAccountActivity.this, ListenToOthersActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
