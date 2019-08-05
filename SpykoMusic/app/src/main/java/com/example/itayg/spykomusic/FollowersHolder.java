package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FollowersHolder extends RecyclerView.ViewHolder {

    public ImageView followerRemove;
    public ImageView accountImg;
    public TextView accountName, accountNickname;
    public RelativeLayout layout;

    public FollowersHolder(View itemView) {
        super(itemView);
        accountImg = itemView.findViewById(R.id.follower_account_image);
        accountName = itemView.findViewById(R.id.follower_full_name);
        accountNickname = itemView.findViewById(R.id.follower_account_nickname);
        layout = itemView.findViewById(R.id.followers_layout);
        followerRemove = itemView.findViewById(R.id.follower_remove_button);
    }
}
