package com.example.itayg.spykomusic;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.util.ArrayList;
import java.util.HashMap;

public class RetrievePlaylistTask extends AsyncTask<PlaylistTaskItems, ArrayList <Video> , ArrayList<Video>> {

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        void processFinish(ArrayList<Video> output);
    }



    public AsyncResponse delegate = null;
    private ArrayList <Video> playlistVideos;
    PlaylistAdapter playlistAdapter;

    private ProgressBar progressBar;
    public void setProgressBar(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
        super.onPreExecute();
    }

    public RetrievePlaylistTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(ArrayList<Video> videos) {
        delegate.processFinish(videos);
    }

    @Override
    protected ArrayList<Video> doInBackground(PlaylistTaskItems... items) {
        try {
            playlistVideos = items[0].playlistVideos;
            playlistAdapter = items[0].adapter;
            for(String id : items[0].videosID) {
                items[0].videoRequest.setId(id);
                items[0].videoRequest.setKey(Constants.getKey());
                VideoListResponse response = items[0].videoRequest.execute();
                playlistVideos.add(response.getItems().get(0));
                publishProgress(playlistVideos);


            }

        }catch (Exception e){
            e.printStackTrace();
            this.progressBar.setVisibility(View.GONE);}

        return null;
    }

    @Override
    protected void onProgressUpdate(ArrayList<Video>... values) {
        this.progressBar.setVisibility(View.GONE);
        Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                playlistAdapter.notifyItemInserted(playlistVideos.size()-1);
            }
        };

        handler.post(r);
        super.onProgressUpdate(values);
    }
}
