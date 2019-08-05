package com.example.itayg.spykomusic;

import com.google.android.youtube.player.YouTubePlayer;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class ItemsForListeningThread {
    private YouTubePlayer player;
    private String videoID;
    private BufferedReader in;
    private PrintWriter out;
    private boolean run;

    public ItemsForListeningThread(YouTubePlayer player, String videoID, BufferedReader in, PrintWriter out) {
        this.player = player;
        this.videoID = videoID;
        this.in = in;
        this.out = out;
        run = true;
    }

    public YouTubePlayer getPlayer() {
        return player;
    }

    public String getVideoID() {
        return videoID;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public boolean isRun() {
        return run;
    }

    public void stopRunning(){
        run = false;
    }
}
