package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RequestingToFollowItemViewHolder extends RecyclerView.ViewHolder{
    public ImageView accountImage, ignoreButton;
    public TextView accountName, accountNickname, acceptButton;

    public RequestingToFollowItemViewHolder(View itemView) {
        super(itemView);
        accountImage = itemView.findViewById(R.id.requesting_to_follow_image);
        ignoreButton = itemView.findViewById(R.id.requesting_to_follow_ignore_button);
        accountName = itemView.findViewById(R.id.requesting_to_follow_name);
        accountNickname = itemView.findViewById(R.id.requesting_to_follow_nickname);
        acceptButton = itemView.findViewById(R.id.requesting_to_follow_accept_button);
    }
}
