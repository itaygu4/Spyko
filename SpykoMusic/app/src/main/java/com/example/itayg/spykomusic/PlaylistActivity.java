package com.example.itayg.spykomusic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class PlaylistActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private RecyclerView recyclerView;

    ArrayList<String> youtubeVideosID = new ArrayList<>();

    private String id;
    private String uid;

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;


    public static YouTube getYouTubeService(){
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
        setContentView(R.layout.activity_playlist);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Toolbar toolbar = findViewById(R.id.toolbar_search_songs);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        Date date = TrueTime.isInitialized() ? TrueTime.now() : new Date();
        long time = date.getTime();
        Log.e("time", date.toString());


        toolbar.setPadding(0,0,0,0);//for tab otherwise give space in tab
        toolbar.setContentInsetsAbsolute(0,0);

        Intent myIntent = getIntent();
        id = myIntent.getStringExtra("playlist_id");
        uid = myIntent.getStringExtra("uid");



        //ImageView changeNameButton = findViewById(R.id.change_name_button);
        //ImageView deletePlaylistButton = findViewById(R.id.playlist_remove);
        TextView playlistName = findViewById(R.id.playlist_name);

        if(!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            //changeNameButton.setVisibility(View.GONE);
            //deletePlaylistButton.setVisibility(View.GONE);
            /*Toolbar.LayoutParams params = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            params.topMargin = 25;
            playlistName.setLayoutParams(params);
            playlistName.setGravity(View.TEXT_ALIGNMENT_CENTER);*/
            TextView text1 = findViewById(R.id.text1);
            TextView text2 = findViewById(R.id.text2);
            text1.setVisibility(View.GONE);
            text2.setVisibility(View.GONE);
        }



        setUpRecyclerView();


        DatabaseReference nameOfPlaylistRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id).child("name");

        nameOfPlaylistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView playlistName = findViewById(R.id.playlist_name);
                if(playlistName == null)
                    return;
                if(dataSnapshot.getValue() == null)
                    playlistName.setText("");
                else
                    playlistName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference specificPlaylistRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id).child("songs");

        specificPlaylistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //specificPlaylistRef.removeEventListener(this);
                ArrayList<String> tempYoutubeVideosID = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        tempYoutubeVideosID.add(snapshot.getKey());
                }
                /*handle video removed*/
                ArrayList <String> toRemove = new ArrayList<>();
                if(youtubeVideosID.size() != tempYoutubeVideosID.size()){
                    for(String ID : youtubeVideosID){
                        if(!tempYoutubeVideosID.contains(ID))
                            toRemove.add(ID);
                    }
                }
                youtubeVideosID.removeAll(toRemove);
                youtubeVideosID = tempYoutubeVideosID;
                try {
                    if(youtubeVideosID.size() == 0)
                        return;
                    YouTube youtube = getYouTubeService();

                    YouTube.Videos.List videosListMultipleIdsRequest = youtube.videos().list("snippet");
                    ArrayList <Video> playlistVideos = new ArrayList<>();
                    PlaylistAdapter adapter = new PlaylistAdapter(PlaylistActivity.this, playlistVideos, id, uid);
                    PlaylistTaskItems items = new PlaylistTaskItems(youtubeVideosID,playlistVideos, adapter, recyclerView, videosListMultipleIdsRequest, id, uid, PlaylistActivity.this);
                    /*String fullIdRequest = "";
                    playlistAdapter = null;
                    playlistVideos = new ArrayList<>();
                    for (int i=0; i<youtubeVideosID.size(); i++) {
                        if(i==0)
                            first = true;
                        if (i < youtubeVideosID.size() - 1) {
                            fullIdRequest += youtubeVideosID.get(i) + ',';
                            videosListMultipleIdsRequest.setId(fullIdRequest);
                            videosListMultipleIdsRequest.setKey(Constants.getKey());
                            /*new RetrievePlaylistTask(new RetrievePlaylistTask.AsyncResponse() {
                                @Override
                                public void processFinish(ArrayList<Video> output) {
                                     playlistVideos.add(output.get(0));
                                    if(first){
                                        playlistAdapter = new PlaylistAdapter(PlaylistActivity.this, playlistVideos, id, uid);
                                        recyclerView.setAdapter(playlistAdapter);
                                        first = false;
                                    }
                                    else{
                                        playlistAdapter.notifyItemInserted(playlistVideos.size()-1);
                                    }
                                }
                            }).execute(videosListMultipleIdsRequest);

                        }

                        else
                            fullIdRequest += youtubeVideosID.get(i);
                    }
                    videosListMultipleIdsRequest.setId(fullIdRequest);
                    videosListMultipleIdsRequest.setKey(Constants.getKey());*/
                    recyclerView.setAdapter(adapter);
                    RetrievePlaylistTask getVideosTask = new RetrievePlaylistTask(new RetrievePlaylistTask.AsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<Video> output) {

                        }
                    });
                    ProgressBar progressBar = findViewById(R.id.loading_bar);
                    getVideosTask.setProgressBar(progressBar);
                    getVideosTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);

                }catch (Exception e){e.printStackTrace();}

                ImageView playNormalOrder = findViewById(R.id.play_normal_order);
                ImageView shuffle = findViewById(R.id.shuffle);
                TextView text1 = findViewById(R.id.text1);
                TextView text2 = findViewById(R.id.text2);

                if(youtubeVideosID == null || youtubeVideosID.size()==0){


                    playNormalOrder.setVisibility(View.GONE);
                    shuffle.setVisibility(View.GONE);
                    text1.setVisibility(View.VISIBLE);
                    text2.setVisibility(View.VISIBLE);

                }
                else{
                    playNormalOrder.setVisibility(View.VISIBLE);
                    shuffle.setVisibility(View.VISIBLE);
                    text1.setVisibility(View.GONE);
                    text2.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("damb", databaseError.toString());
            }
        });

        String query = "you can cry";
        /*try{
            YouTube youtube = getYouTubeService();
            YouTube.Search.List searchListByKeywordRequest = youtube.search().list("snippet");
            searchListByKeywordRequest.setKey(Constants.getKey());
            searchListByKeywordRequest.setQ(query);
            searchListByKeywordRequest.setType("video");
            Long maxResults = Long.valueOf(10);
            searchListByKeywordRequest.setMaxResults(maxResults);
            ArrayList<SearchResult> searchResults = new RetrieveSearchResultsTask().execute(searchListByKeywordRequest).get();
            for(SearchResult sr : searchResults){
                Log.e("item", sr.getSnippet().getTitle());
            }
            setUpRecyclerView();
            final ArrayList<SearchResult> searchResultArrayList = searchResults;
            YoutubeVideoAdapter adapter = new YoutubeVideoAdapter(this, searchResultArrayList);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnItemTouchListener(new RecyclerViewOnClickListener(this, new RecyclerViewOnClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    //start youtube player activity by passing selected video id via intent
                    startActivity(new Intent(PlaylistActivity.this, YoutubePlayerActivity.class)
                            .putExtra("video_id", searchResultArrayList.get(position).getId().getVideoId()));

                }
            }));
        }catch (Exception e){Log.e("shittt",e.toString());}*/


    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rename_playlist:
                playlistChangeName();
                return true;
            case R.id.action_delete_playlist:
                playlistDeleteClick();
                return true;
            case android.R.id.home:
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.songs_options_menu, menu);
        //inflater.inflate(R.menu.playlist_options, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search_music).getActionView();
        if(!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            MenuItem item = menu.findItem(R.id.action_search_music);
            item.setVisible(false);
            return true;
        }
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableMusicActivity.class)));
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(this);

        HelperMethods.changeSearchViewTextColor(searchView);


        return true;
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        Intent searchIntent = new Intent(this, SearchableMusicActivity.class);
        searchIntent.putExtra(SearchManager.QUERY, query);
        searchIntent.putExtra("uid", uid);
        Bundle appData = new Bundle();
        appData.putString("playlist_id", id);
        searchIntent.putExtra(SearchManager.APP_DATA, appData); // pass the search context data
        searchIntent.setAction(Intent.ACTION_SEARCH);

        startActivity(searchIntent);

        return true; // we start the search activity manually
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }



    public void backClick(View view) {
        finish();
    }

    public void playlistChangeName() {

        final LinearLayout layout = findViewById(R.id.playlistActivityView);
        final EditText input = new EditText(PlaylistActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.requestFocus();
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        new AlertDialog.Builder(this)
                .setMessage("New playlist name:")
                .setView(input)
                .setPositiveButton("change", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        DatabaseReference playlistName = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id).child("name");
                        imm.toggleSoftInputFromWindow(input.getWindowToken(), 0,0);
                        playlistName.setValue(input.getText().toString());
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.toggleSoftInputFromWindow(input.getWindowToken(), 0,0);
                    }
                }).show();

    }

    public void playNormalOrderClick(View view) {
        String [] order = new String[youtubeVideosID.size()];
        int i=0;
        for (String id : youtubeVideosID){
            order[i] = id;
            i++;
        }
       startActivity(new Intent(this, YoutubePlayerActivity.class)
                .putExtra("video_id", youtubeVideosID.get(0))
                .putExtra("uid", uid)
                .putExtra("playlist_order", order));
    }

    public void shuffleClick(View view) {
        ArrayList<String> temp = new ArrayList<>(youtubeVideosID);
        Collections.shuffle(temp);

        String [] order = new String[temp.size()];
        int i=0;
        for (String id : temp){
            order[i] = id;
            i++;
        }
        startActivity(new Intent(this, YoutubePlayerActivity.class)
                .putExtra("video_id", temp.get(0))
                .putExtra("uid", uid)
                .putExtra("playlist_order", order));
    }


    public void playlistDeleteClick() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to permanently remove this playlist?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        final DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id);
                        playlistRef.removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                finish();
                            }
                        });
                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    @Override
    protected void onRestart() {
        /*ArrayList<String> params = new ArrayList<>();
        params.add("End");
        params.add(uid);
        ItemsForThread items = new ItemsForThread(params, null, null, null);
        new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
            @Override
            public void processFinish(Socket output) {
            }
        }).execute(items);*/

        super.onRestart();
    }


}
