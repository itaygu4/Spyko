package com.example.itayg.spykomusic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class ItemsForChatThread {
    private ArrayList<String> params;
    private ChatMessageCustomAdapter adapter;
    private ArrayList<ChatMessage> messages;
    private RecyclerView recyclerView;
    private Context context;

    public ItemsForChatThread(ArrayList<String> params, ChatMessageCustomAdapter adapter, ArrayList<ChatMessage> messages, RecyclerView recyclerView, Context context) {
        this.params = params;
        this.adapter = adapter;
        this.messages = messages;
        this.recyclerView = recyclerView;
        this.context = context;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    public void setParams(ArrayList<String> params) {
        this.params = params;
    }

    public ChatMessageCustomAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(ChatMessageCustomAdapter adapter) {
        this.adapter = adapter;
    }

    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public Context getContext() {
        return context;
    }
}
