package com.example.itayg.spykomusic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;

import java.util.ArrayList;

public class PlaylistTaskItems {
    public ArrayList<String> videosID;
    public PlaylistAdapter adapter;
    public RecyclerView recyclerView;
    public YouTube.Videos.List videoRequest;
    public String id, uid;
    public Context context;
    public ArrayList <Video> playlistVideos;

    public PlaylistTaskItems(ArrayList<String> videosID, ArrayList <Video> playlistVideos, PlaylistAdapter adapter, RecyclerView recyclerView, YouTube.Videos.List videoRequest, String id, String uid, Context context) {
        this.videosID = videosID;
        this.playlistVideos = playlistVideos;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        this.videoRequest = videoRequest;
        this.id = id;
        this.uid = uid;
        this.context = context;
    }
}
