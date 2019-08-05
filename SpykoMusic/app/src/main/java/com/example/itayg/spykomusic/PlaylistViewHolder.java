package com.example.itayg.spykomusic;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    public TextView playlistName, playlistSongCount;
    public LinearLayout layout;

    public PlaylistViewHolder(View itemView) {
        super(itemView);
        playlistName = itemView.findViewById(R.id.playlist_name_in_playlist);
        playlistSongCount = itemView.findViewById(R.id.playlist_song_count);
        layout = itemView.findViewById(R.id.layout_playlist);
    }
}
