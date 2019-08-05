package com.example.itayg.spykomusic;

import com.google.android.youtube.player.YouTubePlayer;

import java.util.ArrayList;

public class ItemsForThread {
    private ArrayList<String> params;
    private YouTubePlayer player;
    private String videoID;
    private YouTubePlayer listenerPlayer;
    private boolean shouldRun = true;

    public ItemsForThread(ArrayList<String> params, YouTubePlayer player, String videoID, YouTubePlayer listenerPlayer) {
        this.params = params;
        this.player = player;
        this.videoID = videoID;
        this.listenerPlayer = listenerPlayer;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    public YouTubePlayer getPlayer() {
        return player;
    }

    public String getVideoID() {
        return videoID;
    }

    public YouTubePlayer getListenerPlayer() {
        return listenerPlayer;
    }

    public void stopRunning(){
        shouldRun = false;
    }

    public boolean isRunning(){
        return shouldRun;
    }
}
