package com.example.itayg.spykomusic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;

public class RetrieveMusicSearchResultsItems {
    public PlaylistAdapter adapter;
    public RecyclerView recyclerView;

    public String id, uid;
    public Context context;
    public ArrayList<Video> playlistVideos;
}
