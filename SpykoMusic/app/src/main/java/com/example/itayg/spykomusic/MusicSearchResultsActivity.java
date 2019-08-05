package com.example.itayg.spykomusic;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;

public class MusicSearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;


    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    public static YouTube getYouTubeService() throws IOException {
        try {
            HTTP_TRANSPORT = new com.google.api.client.http.javanet.NetHttpTransport();
        }catch (Exception e){
            Log.e("failed", "in life");
            System.exit(1);}


        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        })
                .setApplicationName("spyko-music")
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_search_results);

        final TextView noSearchResultsFoundMessage = findViewById(R.id.no_results_text);
        noSearchResultsFoundMessage.setVisibility(View.GONE);

        ProgressBar progressBar = findViewById(R.id.loading_spinner);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        final Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        final String id = intent.getStringExtra("playlist_id");
        try{
            YouTube youtube = getYouTubeService();
            YouTube.Search.List searchListByKeywordRequest = youtube.search().list("snippet");
            searchListByKeywordRequest.setKey(Constants.getKey());
            searchListByKeywordRequest.setQ(query);
            searchListByKeywordRequest.setType("video");
            Long maxResults = Long.valueOf(40);
            searchListByKeywordRequest.setMaxResults(maxResults);
            RetrieveSearchResultsTask task = new RetrieveSearchResultsTask(new RetrieveSearchResultsTask.AsyncResponse() {
                @Override
                public void processFinish(ArrayList<SearchResult> searchResults) {
                    for (SearchResult sr : searchResults) {
                        Log.e("item", sr.getSnippet().getTitle());
                    }
                    setUpRecyclerView();
                    if (searchResults.size() == 0)
                        noSearchResultsFoundMessage.setVisibility(View.VISIBLE);
                    else {
                        YoutubeVideoAdapter adapter = new YoutubeVideoAdapter(MusicSearchResultsActivity.this, searchResults, id);
                        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
                        recyclerView.setAdapter(adapter);
                    }
                }
            });
            task.setProgressBar(progressBar);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchListByKeywordRequest);


            /*recyclerView.addOnItemTouchListener(new RecyclerViewOnClickListener(this, new RecyclerViewOnClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    //start youtube player activity by passing selected video id via intent
                    startActivity(new Intent(MusicSearchResultsActivity.this, YoutubePlayerActivity.class)
                            .putExtra("video_id", searchResultArrayList.get(position).getId().getVideoId()));

                }
            }));*/

        }catch (Exception e){Log.e("shittt",e.toString());}
    }





    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_results_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    public void backClick(View view) {
        finish();
    }


}
