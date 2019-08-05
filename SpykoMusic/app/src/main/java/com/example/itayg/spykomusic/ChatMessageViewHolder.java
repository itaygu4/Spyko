package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    public ImageView accountImg;
    public TextView accountNickname, accountMsg;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        accountImg = itemView.findViewById(R.id.chat_account_image);
        accountMsg = itemView.findViewById(R.id.chat_account_message);
        accountNickname = itemView.findViewById(R.id.chat_account_nickname);
    }
}
