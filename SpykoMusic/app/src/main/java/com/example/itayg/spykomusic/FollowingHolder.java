package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeThumbnailView;

public class FollowingHolder extends RecyclerView.ViewHolder {

    public ImageView accountImg, requested;
    public TextView accountNickname, accountName;
    public RelativeLayout layout;

    public FollowingHolder(View itemView) {
        super(itemView);
        accountImg = itemView.findViewById(R.id.profile_picture);
        accountName = itemView.findViewById(R.id.account_full_name);
        accountNickname = itemView.findViewById(R.id.account_nickname);
        layout = itemView.findViewById(R.id.following_layout);
        requested = itemView.findViewById(R.id.follow_requested);
    }
}

