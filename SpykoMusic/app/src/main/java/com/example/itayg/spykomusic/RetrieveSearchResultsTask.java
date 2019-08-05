package com.example.itayg.spykomusic;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.net.Socket;
import java.util.ArrayList;

public class RetrieveSearchResultsTask extends AsyncTask<YouTube.Search.List, Void, ArrayList<SearchResult>> {

    public interface AsyncResponse {
        void processFinish(ArrayList<SearchResult> searchResults);
    }

    public RetrieveSearchResultsTask.AsyncResponse delegate = null;

    public RetrieveSearchResultsTask(RetrieveSearchResultsTask.AsyncResponse delegate){
        this.delegate = delegate;
    }

    ProgressBar bar;

    public void setProgressBar(ProgressBar bar) {
        this.bar = bar;
    }

    @Override
    protected ArrayList<SearchResult> doInBackground(YouTube.Search.List... searchListByKeywordRequest) {
        try {
            Log.e("r", "Retrieving search results!");
            SearchListResponse response = searchListByKeywordRequest[0].execute();
            ArrayList<SearchResult> searchResults = new ArrayList<>();
            for(SearchResult sr : response.getItems()){
                searchResults.add(sr);
            }
            publishProgress();
            return searchResults;
        }catch (Exception e){
            Log.e("error",e.toString());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        this.bar.setVisibility(View.GONE);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(ArrayList<SearchResult> searchResults) {
        delegate.processFinish(searchResults);
        super.onPostExecute(searchResults);
    }
}
