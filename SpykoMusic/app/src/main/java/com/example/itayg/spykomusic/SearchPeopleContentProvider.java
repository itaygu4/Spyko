package com.example.itayg.spykomusic;


import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SearchPeopleContentProvider extends ContentProvider {

    private static final String USERS = "users/"+ SearchManager.SUGGEST_URI_PATH_QUERY+"/*";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI("com.spyko.people.search", USERS, 1);
    }

    private static String[] matrixCursorColumns = {"_id",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA };

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        String queryType = "";
        switch(uriMatcher.match(uri)){
            case 1 :
                String query = uri.getLastPathSegment().toLowerCase();
                return getSearchResultsCursor(query);
            default:
                return null;
        }
    }

    private MatrixCursor getSearchResultsCursor(String searchString){
        MatrixCursor searchResults =  new MatrixCursor(matrixCursorColumns);
        Object[] mRow = new Object[3];
        int counterId = 0;
        if(searchString != null){
            searchString = searchString.toLowerCase();


            for(String rec :  UserNames.getUsersInfo()){
                String [] info = rec.split(" ");
                boolean inSearchResults = false;
                for(String word : info){
                    if(word.toLowerCase().startsWith(searchString)){
                        inSearchResults = true;
                        break;
                    }
                }
                if((!inSearchResults) && rec.startsWith(searchString))
                    inSearchResults = true;
                if(inSearchResults) {
                    mRow[0] = "" + counterId++;
                    mRow[1] = rec;

                    mRow[2] = rec;

                    searchResults.addRow(mRow);

                }
            }
        }
        return searchResults;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}

