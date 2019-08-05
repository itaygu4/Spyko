package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeThumbnailView;

public class PlaylistCellViewHolder  extends RecyclerView.ViewHolder {

    public TextView playlistName;
    public ImageView playlistImage;
    public RelativeLayout layout;
    public ImageView coverScreen;

    public PlaylistCellViewHolder(View itemView) {
        super(itemView);
        playlistName = itemView.findViewById(R.id.playlist_name);
        playlistImage = itemView.findViewById(R.id.playlist_image);
        layout = itemView.findViewById(R.id.playlist_main_layout);
        coverScreen = itemView.findViewById(R.id.cover_screen);
    }
}