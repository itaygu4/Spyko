package com.example.itayg.spykomusic;

import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChatTask extends AsyncTask<ItemsForChatThread, Void, Socket> {

    private ItemsForChatThread items;
    private ArrayList<String> input = new ArrayList<>();
    private PrintWriter out = null;
    private BufferedReader in = null;
    String uid, message, nickname;

    @Override
    protected Socket doInBackground(ItemsForChatThread... itemsForChatThreads) {
        items = itemsForChatThreads[0];     //get the input to the thread
        input.addAll(this.items.getParams());   //get the parameters from the input
        try {
            return doWork();
        }catch (IOException e){e.printStackTrace();}
        return null;
    }

    private Socket doWork() throws IOException{     //do the background work
        Socket socket = null;

        String host = Constants.getIP();        //get the IP of the server
        try{
            //establish a connection
            socket = new Socket(host, 5213);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }catch (UnknownHostException e){
            e.printStackTrace();
            return null;
        }
        int counter = 0;
        String toSend = "";     //what to send to the server
        for(String str : input){

            if(input.get(0).equals("ChatSend")){
                if(counter == 3)        //the actual message to be sent should be in str
                    toSend += input.get(3).length() + " " + '\n';       //send the chat message the following way: length of the message-space-new line-chat message
            }
            toSend += str + " ";
            counter++;
        }

        out.println(toSend);        //send this to the server

        if(input.get(0).equals("ChatStart")){       //start the chat (when the user joins the chat)
            while (true){
                String inputFromServer = in.readLine();
                while(inputFromServer == null || inputFromServer.equals("")) {      //wait until a new message comes from the server
                    inputFromServer = in.readLine();
                }
                String [] messageParts = inputFromServer.split(" ");    //split the message into words
                uid = messageParts[0];
                String messageLength = messageParts[1];
                int length = Integer.parseInt(messageLength);
                message = "";
                //read the message content
                while(length != 0){
                    message += (char)in.read();
                    length--;
                }
                final DatabaseReference nicknameRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("nickname");    //get the sender's nickname
                nicknameRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        nickname = dataSnapshot.getValue().toString();      //get the nickname from the firebase snapshot
                        ChatMessage chatMessage = new ChatMessage(uid, nickname, message);      //create a new chat message object
                        items.getMessages().add(chatMessage);       //add it to the other chat messages
                        if(items.getAdapter() == null){     //if no adapter has been set yet
                            items.setAdapter(new ChatMessageCustomAdapter(items.getContext(), items.getMessages()));    //set an adapter
                            items.getRecyclerView().setAdapter(items.getAdapter());     //apply the adapter to the recycler view

                        }
                        else {
                            items.getAdapter().notifyItemInserted(items.getMessages().size()-1);    //show that a new chat message added
                        }
                        items.getRecyclerView().smoothScrollToPosition(items.getMessages().size()-1);   //scroll lower in the chat

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        return null;
    }
}
