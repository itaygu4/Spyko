package com.example.itayg.spykomusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ListenToOthersActivity extends YouTubeBaseActivity {

    private static final String TAG = YoutubePlayerActivity.class.getSimpleName();
    private String videoID;
    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayer myYouTubePlayer;
    private String[] order = null;
    private Socket socket;
    private String uid;
    private AsyncTask<ItemsForThread, Void, Socket> task;
    RecyclerView recyclerView;
    private ChatMessageCustomAdapter adapter;
    private EditText userMessageInputView;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private String myUid, message, nickname;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_others);

        userMessageInputView = findViewById(R.id.message_input);
        userMessageInputView.setFocusable(false);

        userMessageInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                userMessageInputView.setFocusableInTouchMode(true);

                return false;
            }


        });

        final SoftKeyboardStateWatcher softKeyboardStateWatcher
                = new SoftKeyboardStateWatcher(findViewById(R.id.main_layout));

        softKeyboardStateWatcher.addSoftKeyboardStateListener(new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {

            }

            @Override
            public void onSoftKeyboardClosed() {
                userMessageInputView.setFocusable(false);
            }
        });


        setUpRecyclerView();

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+

        getWindow().getDecorView().setSystemUiVisibility(flags);

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

        youTubePlayerView = findViewById(R.id.youtube_player_view);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");




        if(myYouTubePlayer != null)
            myYouTubePlayer.release();
        initializeYoutubePlayer();


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

                    ArrayList<String> params = new ArrayList<>();
                    params.add("Connect");
                    params.add(uid);
                    params.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ItemsForThread items = new ItemsForThread(params, null, null, youTubePlayer);
                    task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                        @Override
                        public void processFinish(Socket output) {

                        }
                    });

                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);

                    ArrayList<String> paramsForChat = new ArrayList<>();
                    paramsForChat.add("ChatStart");
                    paramsForChat.add(uid);
                    paramsForChat.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ItemsForChatThread itemsForChat = new ItemsForChatThread(paramsForChat, adapter ,chatMessages, recyclerView, ListenToOthersActivity.this);
                    AsyncTask<ItemsForChatThread, Void, Socket> chatTask = new ChatTask();

                    chatTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemsForChat);
                    //set the player style here: like CHROMELESS, MINIMAL, DEFAULT
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

                    //load the video
                    //OR

                    //cue the video
                    //youTubePlayer.cueVideo(videoID);

                    //if you want when activity start it should be in full screen uncomment below comment
                    //  youTubePlayer.setFullscreen(true);

                    //If you want the video should play automatically then uncomment below comment

                    youTubePlayer.setShowFullscreenButton(false);

                    myYouTubePlayer = youTubePlayer;


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

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
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

    @Override
    protected void onPause() {
        if(isFinishing()) {
            Log.e("Stopping", "the thread");
            if(task!=null)
                task.cancel(true);
            ArrayList<String> params = new ArrayList<>();
            params.add("Disconnect");
            params.add(uid);
            params.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ItemsForThread items = new ItemsForThread(params, null, null, null);
            task = new SendInfoToServerTask(new SendInfoToServerTask.AsyncResponse() {
                @Override
                public void processFinish(Socket output) {

                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);
        }
        super.onPause();
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    public void messageSendClick(View view) {
        message = userMessageInputView.getText().toString();
        if(message.length() == 0 || checkIfEmpty(message))
            return;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userMessageInputView.setText("");
        final DatabaseReference nicknameRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("nickname");
        nicknameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nickname = dataSnapshot.getValue().toString();
                ChatMessage chatMessage = new ChatMessage(myUid, nickname, message);
                chatMessages.add(chatMessage);
                nicknameRef.removeEventListener(this);
                if(adapter == null){
                    adapter = new ChatMessageCustomAdapter(ListenToOthersActivity.this, chatMessages);
                    recyclerView.setAdapter(adapter);

                }
                else {
                    adapter.notifyItemInserted(chatMessages.size()-1);
                }
                recyclerView.smoothScrollToPosition(chatMessages.size()-1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                nicknameRef.removeEventListener(this);
            }
        });

        ArrayList<String> params = new ArrayList<>();
        params.add("ChatSend");
        params.add(uid);
        params.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        params.add(message);
        ItemsForChatThread items = new ItemsForChatThread(params, adapter ,chatMessages, recyclerView, this);
        AsyncTask<ItemsForChatThread, Void, Socket> chatTask = new ChatTask();

        chatTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, items);



    }

    private boolean checkIfEmpty(String message){
        for(int i=0; i<message.length(); i++){
            char c = message.charAt(i);
            if(c != ' ' && c != '\t' && c != '\n')
                return false;
        }
        return true;
    }
}
