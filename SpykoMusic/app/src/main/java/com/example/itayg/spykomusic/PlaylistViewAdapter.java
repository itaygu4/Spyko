package com.example.itayg.spykomusic;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewHolder>{
    private Context context;
    private ArrayList<String> playlistIDs;
    private int songCount;

    private String id;

    private String uid;
    private boolean onBind;




    public PlaylistViewAdapter(Context context, ArrayList<String> playlistIDs, String uid) {
        this.context = context;
        this.playlistIDs = playlistIDs;
        this.uid = uid;
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.playlist_row_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlaylistViewHolder holder, final int position) {
        onBind = true;
        if(playlistIDs == null || playlistIDs.size() == 0)
            return;

        id = this.playlistIDs.get(position);

        DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("playlists").child(id);

        final DatabaseReference nameRef = playlistRef.child("name");

        final DatabaseReference songsRef = playlistRef.child("songs");



        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.playlistName.setText(dataSnapshot.getValue().toString());
                nameRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        songsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                songCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    songCount++;
                }
                String song;
                if(songCount == 1)
                    song = "song";
                else
                    song = "songs";
                holder.playlistSongCount.setText(songCount + " "  + song);
                songsRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaylistActivity.class);
                intent.putExtra("playlist_id", playlistIDs.get(position));
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        onBind = false;


    }

    @Override
    public int getItemCount() {
        return playlistIDs != null ? playlistIDs.size() : 0;
    }
}