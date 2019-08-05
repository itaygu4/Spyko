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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ChatMessageCustomAdapter extends RecyclerView.Adapter<ChatMessageViewHolder>{

    private Context context;
    private ArrayList<ChatMessage> chatMessages;

    public ChatMessageCustomAdapter(Context context, ArrayList<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chat_message_row_item, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatMessageViewHolder holder, int position) {
        holder.accountNickname.setText(this.chatMessages.get(position).nickname);
        holder.accountMsg.setText(this.chatMessages.get(position).message);

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(this.chatMessages.get(holder.getAdapterPosition()).uid+".jpg");

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
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
}
