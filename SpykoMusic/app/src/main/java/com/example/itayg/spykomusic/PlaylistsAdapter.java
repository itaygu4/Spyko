package com.example.itayg.spykomusic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import jp.wasabeef.blurry.Blurry;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistCellViewHolder>{
    private Context context;
    private ArrayList<String> playlistIDs;
    private String uid;
    private String id;
    private String thumbnailVideoId;

    public PlaylistsAdapter(Context context, ArrayList<String> playlistIDs, String uid) {
        this.context = context;
        this.playlistIDs = playlistIDs;
        this.uid = uid;
        thumbnailVideoId = null;
    }

    @NonNull
    @Override
    public PlaylistCellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.playlist_cell_item, parent, false);
        return new PlaylistCellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaylistCellViewHolder holder, int position) {

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
            public void onDataChange(final DataSnapshot dataSnapshot) {
                thumbnailVideoId = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    thumbnailVideoId = snapshot.getKey();
                    break;

                }

                if(thumbnailVideoId != null) {
                    String img_url="http://img.youtube.com/vi/"+thumbnailVideoId+"/maxresdefault.jpg"; // this is link which will give u thumnail image of that video

                    final RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.spyko_logo);
                    requestOptions.centerCrop();
                    Glide.with(context)
                            .load(img_url)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    Blurry.with(context)
                                            .radius(9)
                                            .sampling(2)
                                            .animate(500)
                                            .from(convertToBitmap(resource))
                                            .into(holder.playlistImage);
                                    return true;
                                }
                            })
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.playlistImage) ;


                   /* Picasso.get()
                            .load(img_url)
                            .placeholder(R.drawable.spyko_logo)
                            .into(holder.playlistImage);*/
                }
                else{
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.spyko_logo);
                    Glide.with(context)
                            .load(R.drawable.spyko_logo)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable drawable, Object model, com.bumptech.glide.request.target.Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    Blurry.with(context)
                                            .radius(7)
                                            .sampling(2)
                                            .animate(500)
                                            .from(convertToBitmap(drawable))
                                            .into(holder.playlistImage);
                                    return true;
                                }
                            })
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.playlistImage);
                    /*Picasso.get()
                            .load(R.drawable.spyko_logo)
                            .placeholder(R.drawable.spyko_logo)
                            .into(holder.playlistImage);*/
                }


                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            BitmapDrawable drawable = (BitmapDrawable) holder.playlistImage.getDrawable();
                                            Bitmap bitmap = drawable.getBitmap();
                                            Bitmap blurred = fastblur(bitmap, 1, 14);
                                            holder.playlistImage.setImageBitmap(blurred);
                                        }
                                    },1900);*/


                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        Blurry.with(context)
                                .radius(7)
                                .sampling(2)
                                .animate(500)
                                .capture(holder.playlistImage)
                                .into(holder.playlistImage);



                    }
                },1750);*/
               // Blurry.with(context).capture(holder.playlistImage).into(holder.playlistImage);

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
                intent.putExtra("playlist_id", playlistIDs.get(holder.getAdapterPosition()));
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistIDs.size();
    }

    public Bitmap convertToBitmap(Drawable drawable){
        try {
            Bitmap bitmap;

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            // Handle the error
            return null;
        }
    }

    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) { return (null); } int w = bitmap.getWidth(); int h = bitmap.getHeight(); int[] pix = new int[w * h]; Log.e("pix", w + " " + h + " " + pix.length); bitmap.getPixels(pix, 0, w, 0, 0, w, h); int wm = w - 1; int hm = h - 1; int wh = w * h; int div = radius + radius + 1; int r[] = new int[wh]; int g[] = new int[wh]; int b[] = new int[wh]; int rsum, gsum, bsum, x, y, i, p, yp, yi, yw; int vmin[] = new int[Math.max(w, h)]; int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) { p = pix[yi + Math.min(wm, Math.max(i, 0))]; sir = stack[i + radius]; sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) { r[yi] = dv[rsum]; g[yi] = dv[gsum]; b[yi] = dv[bsum]; rsum -= routsum; gsum -= goutsum; bsum -= boutsum; stackstart = stackpointer - radius + div; sir = stack[stackstart % div]; routsum -= sir[0]; goutsum -= sir[1]; boutsum -= sir[2]; if (y == 0) { vmin[x] = Math.min(x + radius + 1, wm); } p = pix[yw + vmin[x]]; sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) { yi = Math.max(0, yp) + x; sir = stack[i + radius]; sir[0] = r[yi]; sir[1] = g[yi]; sir[2] = b[yi]; rbs = r1 - Math.abs(i); rsum += r[yi] * rbs; gsum += g[yi] * rbs; bsum += b[yi] * rbs; if (i > 0) {
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
            } else {
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
            }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}
