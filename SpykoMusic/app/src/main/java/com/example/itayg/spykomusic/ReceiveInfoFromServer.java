package com.example.itayg.spykomusic;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ReceiveInfoFromServer extends AsyncTask<ItemsForListeningThread, Void, Void>{

    ItemsForListeningThread items;

    @Override
    protected Void doInBackground(ItemsForListeningThread... items) {
        this.items = items[0];
        BufferedReader in = items[0].getIn();
        PrintWriter out = items[0].getOut();
        YouTubePlayer player = items[0].getPlayer();
        String videoID = items[0].getVideoID();
        String query;
        try {
            while (items[0].isRun()) {
                while (!((query = in.readLine()).equals("VideoID")) && !((query = in.readLine()).equals("TimeMillis"))) ;
                if(query.equals("VideoId"))
                    out.println(videoID);
                else
                    out.println(player.getCurrentTimeMillis());
            }
        }catch (IOException e){e.printStackTrace();}

        return null;
    }

    private void doWork() throws IOException{
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String host = Constants.getIP();
        String videoID;
        try{
            socket = new Socket(host, 5867);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        while (true) {
            while (!(videoID = in.readLine()).startsWith("VideoID")) ;
            videoID = videoID.substring(8);
            out.println("OK");
            String timeMillis;
            while (!(timeMillis = in.readLine()).startsWith("TimeMillis")) ;
            timeMillis.substring(11);
            Log.e("video info", videoID + "    " + timeMillis);
        }
    }
}
