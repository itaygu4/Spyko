package com.example.itayg.spykomusic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import com.google.api.services.youtube.model.Activity;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.Socket;
import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<YoutubeViewHolder> {
    private static final String TAG = YoutubeVideoAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<Video> playlistItemsArrayList;
    private String id;
    private String uid;
    private boolean myPlaylist = true;
    private Socket socket;
    private int lastPosition = -1;


    public PlaylistAdapter(Context context, ArrayList<Video> playlistItemsArrayList, String playlistID, String uid) {
        this.context = context;
        this.playlistItemsArrayList = playlistItemsArrayList;
        this.id = playlistID;
        this.uid = uid;
        if (!uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            myPlaylist = false;
    }

    @Override
    public YoutubeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.song_row_item, parent, false);
        return new YoutubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final YoutubeViewHolder holder, final int position) {

        if(playlistItemsArrayList == null || playlistItemsArrayList.size() == 0)
            return;

        setAnimation(holder.layout, position);
        final Video playlistItem = playlistItemsArrayList.get(position);

        holder.videoTitle.setText(playlistItem.getSnippet().getTitle());
        holder.artistName.setText(playlistItem.getSnippet().getChannelTitle());
        if(!myPlaylist)
            holder.addToPlaylistButton.setVisibility(View.GONE);
        else
            holder.addToPlaylistButton.setImageResource(R.drawable.ic_added_to_playlist_2);


        /*  initialize the thumbnail image view , we need to pass Developer Key */
        holder.videoThumbnailImageView.initialize(Constants.getKey(), new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {
                //when initialization is sucess, set the video id to thumbnail to load
                youTubeThumbnailLoader.setVideo(playlistItem.getId());

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

        holder.addToPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to remove " + playlistItem.getSnippet().getTitle() + " from your playlist?")
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                String videoID = playlistItem.getId();
                                DatabaseReference videoRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id).child("songs").child(videoID);
                                videoRef.removeValue();
                                playlistItemsArrayList.remove(playlistItemsArrayList.get(position));
                                notifyDataSetChanged();
                                /*Intent intent = new Intent(context, PlaylistActivity.class);
                                context.startActivity(intent);
                                ((android.app.Activity) context).finish();*/
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });



        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String [] order = new String[playlistItemsArrayList.size()];
                int i=0;
                for (Video video : playlistItemsArrayList){
                    order[i] = video.getId();
                    i++;
                }

                context.startActivity(new Intent(context, YoutubePlayerActivity.class)
                        .putExtra("video_id", playlistItem.getId())
                        .putExtra("playlist_order", order)
                        .putExtra("uid", uid));
            }
        });

    }

    @Override
    public int getItemCount() {
        return playlistItemsArrayList != null ? playlistItemsArrayList.size() : 0;
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
