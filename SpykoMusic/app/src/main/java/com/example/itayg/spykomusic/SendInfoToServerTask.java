package com.example.itayg.spykomusic;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.api.client.util.DateTime;
import com.google.common.primitives.Ints;
import com.instacart.library.truetime.TrueTime;

import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SendInfoToServerTask extends AsyncTask <ItemsForThread, Void, Socket> {

    private ArrayList<String> input = new ArrayList<>();
    private YouTubePlayer player;
    private ItemsForThread items;
    private String videoID;
    private YouTubePlayer listenerPlayer;
    private int millisToSeekTo = 0;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private boolean playing = true;
    private int timeToAdd = 0;
    Long startingLoadTime;


    public interface AsyncResponse {
        void processFinish(Socket output);
    }

    public AsyncResponse delegate = null;   //a delegate to get the return value of the doInBackground function

    public SendInfoToServerTask(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected Socket doInBackground(ItemsForThread... items) {      //the stuff the thread will do in the background
        this.items = items[0];      //get the input to the thread
        input.addAll(this.items.getParams());   //add all the parameters to "input"
        player = this.items.getPlayer();
        videoID = this.items.getVideoID();
        listenerPlayer = this.items.getListenerPlayer();
        try {
            return doWork();
        }catch (IOException e){e.printStackTrace();}
        return null;
    }

    @Override
    protected void onPostExecute(Socket socket) {
        delegate.processFinish(socket);
        super.onPostExecute(socket);
    }

    private Socket doWork() throws IOException{
        Socket socket = null;

        String host = Constants.getIP();        //get the server's IP
        try{
            //establish a connection
            socket = new Socket(host, 5213);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }catch (UnknownHostException e){
            e.printStackTrace();
            return null;
        }
        String toSend = "";
        for(String str : input){
            toSend += str + " ";    //send the input with spaces
        }

        out.println(toSend);        //send it

        if(input.get(0).equals("Start")){       //if the DJ is starting watching videos
            startFunction();
        }

        if(input.get(0).equals("Connect")){     //if a user wants to connect to the DJ
            while (!isCancelled() && !(videoID = in.readLine()).startsWith("VideoID")); //wait till you get the video ID
            if(isCancelled()) {
                return null;
            }
            final String [] differentWords = videoID.split(" ");    //split the input into words
            videoID = differentWords[1];
            String videoStatus = differentWords[2];
            final boolean pause;
            if(videoStatus.startsWith("pause"))     //if the DJ's video is paused
                pause = true;       //pause the video as well
            else
                pause = false;      //don't pause the video

            listenerPlayer.loadVideo(videoID);      //load the video into the youtube player

            Log.e("INFO", "the videoID is " + videoID);

            out.println("OK");      //notify the server the videoId was received
            String fromServer = null;
            while (!isCancelled() && !(fromServer = in.readLine()).startsWith("TimeMillis"));   //read the data and store it if it starts with "TimeMillis"
            if(isCancelled()) {     //if the thread should be stopped
                return null;
            }
            final String [] differentWords2 = fromServer.split(" ");    //get all the different words in the input


            listenerPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {
                    //startingLoadTime = System.currentTimeMillis();
                }

                @Override
                public void onLoaded(String s) {        //When the video has loaded
                    Long timeStartedFromServer = Long.parseLong(differentWords2[2]);    //get the time from the DJ
                    int timeMillisFromServer = Integer.parseInt(differentWords2[1]);    //parse into Integer

                    Date time = TrueTime.isInitialized() ? TrueTime.now() : new Date();     //get the current time using the TrueTime library
                    millisToSeekTo =  timeMillisFromServer + Ints.checkedCast(time.getTime() - timeStartedFromServer);  //calculate the point in the video to seek to
                    Log.e("INFO", "time millis is:" + millisToSeekTo);      //log

                    listenerPlayer.seekToMillis(millisToSeekTo);        //seek the video to the calculated point
                    if(!pause)
                        listenerPlayer.play();      //if the DJ hasn't paused the video then play
                    else
                        listenerPlayer.pause();     //if the DJ has paused the video then pause as well

                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {

                }

                @Override
                public void onVideoEnded() {

                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                }
            });

            while (true) {
                String input = "";
                //loop until a new action is needed
                while (!isCancelled() && !((input = in.readLine()).startsWith("Pause")) && !input.startsWith("Seek") && !input.startsWith("New") && !input.startsWith("Continue"));
                if(isCancelled()) {
                    return null;
                }
                Log.e("input", input);
                if(input.startsWith("New")){        //if the DJ switches to a new video
                    listenerPlayer.loadVideo(input.substring(4));   //load the video to the player
                    listenerPlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                        @Override
                        public void onLoading() {

                        }

                        @Override
                        public void onLoaded(String s) {
                            listenerPlayer.seekToMillis(0);     //seek to the start
                            listenerPlayer.play();      //play the video
                        }

                        @Override
                        public void onAdStarted() {

                        }

                        @Override
                        public void onVideoStarted() {

                        }

                        @Override
                        public void onVideoEnded() {

                        }

                        @Override
                        public void onError(YouTubePlayer.ErrorReason errorReason) {

                        }
                    });
                    continue;
                }
                if(input.startsWith("Seek")){       //if the DJ seeked the video forward or backward
                    String time = input.substring(5);       //get the time to seek to
                    listenerPlayer.seekToMillis(Integer.parseInt(time));    //seek the video
                    continue;
                }
                if(input.startsWith("Pause"))       //if the DJ paused the video
                    listenerPlayer.pause();
                if(input.startsWith("Continue"))    //if the DJ continued the video
                    listenerPlayer.play();

            }


        }

        if(input.get(0).equals("Pause")){   //if the DJ pauses the video
            startFunction();
            this.cancel(true);
        }

        if(input.get(0).equals("Continue")){    //if the DJ continues the video
            startFunction();
        }

        if(input.get(0).equals("Seek")){    //if the DJ seeks the video forward or backward
            String send = "";
            for(String str : input){
                send += str + " ";      //send the input with spaces
            }
            startFunction();
        }

        if(input.get(0).equals("End")) {    //if the DJ stops playing videos
            //close resources
            out.close();
            in.close();
            socket.close();
        }

        if(input.get(0).equals("Disconnect")){      //if the listener quits
            //close the resources
            out.close();
            in.close();
            socket.close();
        }
        return socket;
    }

    private void startFunction() throws IOException{        //handles the requests coming to the DJ, e.g. what's the video
        while (true) {
            String videoIdRequest = null;
            while (!isCancelled() && ((videoIdRequest = in.readLine()) !=null && !videoIdRequest.startsWith("VideoID")));   //while there were no requests from the server for the video ID
            if(isCancelled()) {
                break;
            }
            Log.e("INFO", "got the videoID");   //logging
            out.println(videoID);       //send the server the playing video ID
            String timeMillisRequest = null;
            while (!isCancelled() && ((timeMillisRequest = in.readLine()) !=null && !timeMillisRequest.startsWith("TimeMillis"))) ;     //while there were no requests from the server for the video time millis
            if(isCancelled()) {
                break;
            }
            if(player == null) {    //if there is no longer a youtube player running at the DJ's device
                Log.e("INFO", "The player is null!");
            }
            if(timeMillisRequest == null || videoIdRequest == null)
                break;
            Date date = TrueTime.isInitialized() ? TrueTime.now() : new Date();     //get current time using the TrueTime library

            int timeMillis = player.getCurrentTimeMillis();     //get the video's current time
            String send = Integer.toString(timeMillis);     //parse the video's time into string

            out.println(send + " " + date.getTime());       //send the info to the server
            Log.e("INFO", "time millis in dj: " + send);
        }
    }


}
