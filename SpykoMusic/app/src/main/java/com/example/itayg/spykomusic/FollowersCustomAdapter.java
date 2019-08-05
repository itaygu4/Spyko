package com.example.itayg.spykomusic;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FollowersCustomAdapter extends RecyclerView.Adapter<FollowersHolder> {
    private Context context;
    private ArrayList<RowItem> row;


    public FollowersCustomAdapter(Context context, ArrayList<RowItem> row) {
        this.context = context;
        this.row = row;
    }

    @Override
    public FollowersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.followers_row_item, parent, false);
        return new FollowersHolder(view);
    }

    @Override
    public void onBindViewHolder(final FollowersHolder holder, final int position) {

        holder.accountName.setText("(" + row.get(position).getName() + ")");
        holder.accountNickname.setText(row.get(position).getNickname());

        holder.followerRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to remove " + row.get(holder.getAdapterPosition()).getName() + " from your followers?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                DatabaseReference removedRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Followers").child(row.get(holder.getAdapterPosition()).getFirebaseUserUid());
                                removedRef.removeValue();
                                DatabaseReference removeFollowingRef = FirebaseDatabase.getInstance().getReference().child("users").child(row.get(holder.getAdapterPosition()).getFirebaseUserUid()).child("Following").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                removeFollowingRef.removeValue();
                                /*Intent intent = new Intent(context, FollowersActivity.class);
                                context.startActivity(intent);
                                ((Activity) context).finish();*/
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(row.get(holder.getAdapterPosition()).getFirebaseUserUid()+".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext()).load(uri).apply(RequestOptions.circleCropTransform()).into(holder.accountImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Glide.with(context.getApplicationContext()).load(R.drawable.user).into(holder.accountImg);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, OtherPeoplesAccountActivity.class)
                        .putExtra("user_uid", row.get(holder.getAdapterPosition()).getFirebaseUserUid()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return row != null ? row.size() : 0;
    }
}

