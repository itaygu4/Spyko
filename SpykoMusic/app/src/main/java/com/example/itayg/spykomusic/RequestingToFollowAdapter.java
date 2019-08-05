package com.example.itayg.spykomusic;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RequestingToFollowAdapter extends RecyclerView.Adapter<RequestingToFollowItemViewHolder> {

    private Context context;
    private ArrayList<String> requestsUid;
    private String userUid;

    public RequestingToFollowAdapter(Context context, ArrayList<String> requestsUid, String userUid) {
        this.context = context;
        this.requestsUid = requestsUid;
        this.userUid = userUid;
    }

    @NonNull
    @Override
    public RequestingToFollowItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.requesting_to_follow_cell, parent, false);
        return new RequestingToFollowItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestingToFollowItemViewHolder holder, final int position) {
        if(requestsUid == null || requestsUid.size() == 0)
            return;
        final String requestUid = requestsUid.get(position);
        final DatabaseReference infoRef = FirebaseDatabase.getInstance().getReference().child("users").child(requestUid);
        infoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                infoRef.removeEventListener(this);
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals("nickname")) {
                        holder.accountNickname.setText(data.getValue().toString());
                    }
                    if (data.getKey().equals("fullName")) {
                        holder.accountName.setText(data.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(requestUid + ".jpg");
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext()).load(uri).apply(RequestOptions.circleCropTransform()).into(holder.accountImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Glide.with(context.getApplicationContext()).load(R.drawable.user).into(holder.accountImage);
            }
        });

        holder.ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestsUid.remove(holder.getAdapterPosition());
                DatabaseReference followerRequestRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUid).child("Followers").child(requestUid);
                followerRequestRef.removeValue();
                DatabaseReference followerRef = FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("Following").child(userUid);
                followerRef.setValue("false");
                notifyDataSetChanged();
            }
        });

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestsUid.remove(holder.getAdapterPosition());
                DatabaseReference followerRequestRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUid).child("Followers").child(requestUid);
                followerRequestRef.setValue("true");
                DatabaseReference followerRef = FirebaseDatabase.getInstance().getReference().child("users").child(requestUid).child("Following").child(userUid);
                followerRef.setValue("true");
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestsUid.size();
    }
}
