package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeThumbnailView;

public class YoutubeViewHolder extends RecyclerView.ViewHolder {

    public YouTubeThumbnailView videoThumbnailImageView;
    public TextView videoTitle, artistName;
    public ImageView addToPlaylistButton;
    public RelativeLayout layout;

    public YoutubeViewHolder(View itemView) {
        super(itemView);
        videoThumbnailImageView = itemView.findViewById(R.id.video_thumbnail_image_view);
        videoTitle = itemView.findViewById(R.id.video_title_label);
        artistName = itemView.findViewById(R.id.video_artist);
        addToPlaylistButton = itemView.findViewById(R.id.add_to_playlist);
        layout = itemView.findViewById(R.id.card_layout);
    }
}
