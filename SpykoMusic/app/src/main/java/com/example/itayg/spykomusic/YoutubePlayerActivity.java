package com.example.itayg.spykomusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.services.youtube.model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class YoutubePlayerActivity extends YouTubeBaseActivity {
    private static final String TAG = YoutubePlayerActivity.class.getSimpleName();
    private String videoID;
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer myYouTubePlayer;
    private String[] order = null;
    private Socket socket;
    private String uid;
    ItemsForThread items;
    AsyncTask<ItemsForThread, Void, Socket> task;
    private boolean playing = true;
    private boolean firstTime;
    RecyclerView recyclerView;
    private ChatMessageCustomAdapter adapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private boolean isEnding = false;

    @Override
    protected void onResume() {
        firstTime = true;
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        //get the video id


        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+

        getWindow().getDecorView().setSystemUiVisibility(flags);

        setUpRecyclerView();

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });

        videoID = getIntent().getStringExtra("video_id");
        uid = getIntent().getStringExtra("uid");
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        if(myYouTubePlayer != null)
            myYouTubePlayer.release();
        initializeYoutubePlayer();
        String [] temp = getIntent().getStringArrayExtra("playlist_order");
        if(temp != null)
            order = Arrays.copyOf(temp, temp.length);
        else {
            ImageView nextTrackButton = findViewById(R.id.nextTrackButton);
            ImageView lastTrackButton = findViewById(R.id.lastTrackButton);

            nextTrackButton.setVisibility(View.GONE);
            lastTrackButton.setVisibility(View.GONE);
        }


    }

    /**
     * initialize the youtube player
     */
    private void initializeYoutubePlayer() {
        youTubePlayerView.initialize(Constants.getKey(), new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
                                                boolean wasRestored) {


                //if initialization success then load the video id to youtube player
                if (!wasRestored) {
                    //set the player style here: like CHROMELESS, MINIMAL, DEFAULT
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

                    //load the video
                    youTubePlayer.loadVideo(videoID);

                    //OR

                    //cue the video
                    //youTubePlayer.cueVideo(videoID);

                    //if you want when activity start it should be in full screen uncomment below comment
                    //  youTubePlayer.setFullscreen(true);

                    //If you want the video should play automatically then uncomment below comment
                    youTubePlayer.play();

                    youTubePlayer.setShowFullscreenButton(false);

                    youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                        @Override
                        public void onPlaying() {
                            if(!playing){
                                task.cancel(true);
                                ArrayList<String> params = new ArrayList<>();
                                params.add("Continue");
                                params.add(uid);
                                items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
                                task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                                    @Override
                                    public void processFinish(Socket output) {
                                        socket = output;
                                    }
                                }).execute(items);
                                playing = true;
                            }
                        }

                        @Override
                        public void onPaused() {
                            if(isEnding)
                                return;
                            playing = false;
                            task.cancel(true);
                            ArrayList<String> params = new ArrayList<>();
                            params.add("Pause");
                            params.add(uid);
                            items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
                            task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                                @Override
                                public void processFinish(Socket output) {
                                    socket = output;
                                }
                            });
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);
                        }

                        @Override
                        public void onStopped() {

                        }

                        @Override
                        public void onBuffering(boolean b) {

                        }

                        @Override
                        public void onSeekTo(int i) {
                            task.cancel(true);
                            ArrayList<String> params = new ArrayList<>();
                            params.add("Seek");
                            params.add(uid);
                            params.add(String.valueOf(myYouTubePlayer.getCurrentTimeMillis()));
                            items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
                            task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                                @Override
                                public void processFinish(Socket output) {
                                    socket = output;
                                }
                            }).execute(items);
                        }
                    });

                    myYouTubePlayer = youTubePlayer;

                    myYouTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                        @Override
                        public void onLoading() {

                        }

                        @Override
                        public void onLoaded(String s) {

                        }

                        @Override
                        public void onAdStarted() {

                        }

                        @Override
                        public void onVideoStarted() {
                            if(firstTime) {
                                if (task != null)
                                    task.cancel(true);
                                DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("live");
                                liveRef.setValue("true");
                                ArrayList<String> params = new ArrayList<>();
                                params.add("Start");
                                params.add(uid);
                                items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
                                task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                                    @Override
                                    public void processFinish(Socket output) {

                                    }
                                }).execute(items);

                                ArrayList<String> paramsForChat = new ArrayList<>();
                                paramsForChat.add("ChatStart");
                                paramsForChat.add(uid);
                                paramsForChat.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                ItemsForChatThread items = new ItemsForChatThread(paramsForChat, adapter ,chatMessages, recyclerView, YoutubePlayerActivity.this);
                                AsyncTask<ItemsForChatThread, Void, Socket> chatTask = new ChatTask();

                                chatTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);



                                firstTime = false;
                            }


                        }

                        @Override
                        public void onVideoEnded() {
                            int nextI = 0;
                            if(order != null) {
                                for (int i = 0; i < order.length; i++) {
                                    if (order[i].equals(videoID)) {
                                        nextI = i + 1;
                                        break;
                                    }

                                }


                                if (nextI >= order.length) {
                                    nextI = 0;
                                }


                                videoID = order[nextI];


                                task.cancel(true);
                                ArrayList<String> params = new ArrayList<>();
                                params.add("New");
                                params.add(uid);
                                params.add(videoID);
                                items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
                                task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                                    @Override
                                    public void processFinish(Socket output) {
                                        socket = output;
                                    }
                                }).execute(items);

                                myYouTubePlayer.loadVideo(videoID);
                            }
                        }

                        @Override
                        public void onError(YouTubePlayer.ErrorReason errorReason) {

                        }
                    });


                    /*myYouTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                        @Override
                        public void onFullscreen(boolean arg0) {
                            // do full screen stuff here, or don't.
                            myYouTubePlayer.setFullscreen(true);
                            myYouTubePlayer.play();
                        }
                    });*/



                    //If you want to control the full screen event you can uncomment the below code
                    //Tell the player you want to control the fullscreen change
                   /*player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    //Tell the player how to control the change
                    player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                        @Override
                        public void onFullscreen(boolean arg0) {
                            // do full screen stuff here, or don't.
                            Log.e(TAG,"Full screen mode");
                        }
                    });*/

                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
                //print or show error if initialization failed
                Log.e(TAG, "Youtube Player View initialization failed");
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /*public void playOrStopClick(View view) {
        ImageView playStopButton = findViewById(R.id.playStopButton);
        if (myYouTubePlayer.isPlaying()) {
            myYouTubePlayer.pause();
            playStopButton.setImageResource(R.drawable.ic_play_track);
        } else {
            myYouTubePlayer.play();
            playStopButton.setImageResource(R.drawable.ic_stop_track);
        }
    }*/

    public void nextTrackClick(View view) {
        int nextI = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(videoID)) {
                nextI = i + 1;
                break;
            }

        }

        if (nextI >= order.length) {
            nextI = 0;
        }

        videoID = order[nextI];

        task.cancel(true);
        ArrayList<String> params = new ArrayList<>();
        params.add("New");
        params.add(uid);
        params.add(videoID);
        items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
        task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
            @Override
            public void processFinish(Socket output) {
                socket = output;
            }
        }).execute(items);

        myYouTubePlayer.loadVideo(videoID);


    }

    public void lastTrackClick(View view) {
        int lastI = 0;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(videoID)) {
                lastI = i - 1;
                break;
            }

        }

        if (lastI < 0) {
            lastI = order.length - 1;
        }

        videoID = order[lastI];

        task.cancel(true);
        ArrayList<String> params = new ArrayList<>();
        params.add("New");
        params.add(uid);
        params.add(videoID);
        items = new ItemsForThread(params, myYouTubePlayer, videoID, null);
        task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
            @Override
            public void processFinish(Socket output) {
                socket = output;
            }
        }).execute(items);

        myYouTubePlayer.loadVideo(videoID);

    }

    @Override
    protected void onPause() {
        if(isFinishing()) {
            items.stopRunning();
            Log.e("Stopping", "the thread");
            task.cancel(true);
        }
        DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("live");
        liveRef.setValue("false");
        task.cancel(true);
        ArrayList<String> params = new ArrayList<>();
        params.add("End");
        params.add(uid);
        ItemsForThread items = new ItemsForThread(params, null, null, null);
        task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
            @Override
            public void processFinish(Socket output) {
            }
        }).execute(items);

        super.onRestart();
        super.onPause();
    }

    @Override
    protected void onRestart() {
        DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("live");
        liveRef.setValue("true");
        super.onRestart();
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            isEnding = true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

