package com.example.itayg.spykomusic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class YoutubeVideoAdapter extends RecyclerView.Adapter<YoutubeViewHolder> {
    private static final String TAG = YoutubeVideoAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<SearchResult> SearchResultArrayList;
    private DatabaseReference songsRef;
    private String id;
    private int lastPosition = -1;


    public YoutubeVideoAdapter(Context context, ArrayList<SearchResult> SearchResultArrayList, String id) {
        this.context = context;
        this.SearchResultArrayList = SearchResultArrayList;
        this.id = id;
    }

    @Override
    public YoutubeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.song_row_item, parent, false);
        return new YoutubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final YoutubeViewHolder holder, final int position) {

        setAnimation(holder.layout, position);

        //holder.setIsRecyclable(false);
        final SearchResult searchResult = SearchResultArrayList.get(position);

        holder.videoTitle.setText(searchResult.getSnippet().getTitle());
        holder.artistName.setText(searchResult.getSnippet().getChannelTitle());


        /*  initialize the thumbnail image view , we need to pass Developer Key */
        holder.videoThumbnailImageView.initialize(Constants.getKey(), new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {
                //when initialization is sucess, set the video id to thumbnail to load
                youTubeThumbnailLoader.setVideo(searchResult.getId().getVideoId());

                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                        //when thumbnail loaded successfully release the thumbnail loader as we are showing thumbnail in adapter
                        youTubeThumbnailLoader.release();
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        //print or show error when thumbnail load failed
                        Log.e(TAG, "Youtube Thumbnail Error");
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                //print or show error when initialization failed
                Log.e(TAG, "Youtube Initialization Failure");

            }
        });

        DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("playlists").child(id).child("songs");

        playlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean includedInPlaylist = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (searchResult.getId().getVideoId().equals(snapshot.getKey())){
                        holder.addToPlaylistButton.setImageResource(R.drawable.ic_added_to_playlist_2);
                        includedInPlaylist = true;
                    }
                }
                if (!includedInPlaylist){
                    holder.addToPlaylistButton.setImageResource(R.drawable.ic_add_to_playlist_2);
                    holder.addToPlaylistButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            songsRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("playlists").child(id).child("songs");
                            songsRef.child(searchResult.getId().getVideoId()).setValue(true);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, YoutubePlayerActivity.class)
                        .putExtra("video_id", searchResult.getId().getVideoId())
                        .putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return SearchResultArrayList != null ? SearchResultArrayList.size() : 0;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
