package com.example.itayg.spykomusic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FollowingCustomAdapter extends RecyclerView.Adapter<FollowingHolder> {
    private Context context;
    private ArrayList<RowItem> row;


    public FollowingCustomAdapter(Context context, ArrayList<RowItem> row) {
        this.context = context;
        this.row = row;
    }

    @Override
    public FollowingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.following_row_item, parent, false);
        return new FollowingHolder(view);
    }

    @Override
    public void onBindViewHolder(final FollowingHolder holder, final int position) {

        holder.accountName.setText("(" + row.get(position).getName() + ")");
        holder.accountNickname.setText(row.get(position).getNickname());
        String actuallyFollowing = row.get(position).getActuallyFollowing();
        if((actuallyFollowing != null) &&actuallyFollowing.equals("false"))
            holder.requested.setVisibility(View.VISIBLE);
        else
            holder.requested.setVisibility(View.GONE);

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
                if(row.get(holder.getAdapterPosition()).getFirebaseUserUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    context.startActivity(new Intent(context, AccountActivity.class).putExtra("action", "back"));
                else
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

