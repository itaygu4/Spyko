package com.example.itayg.spykomusic;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    ArrayList<String> searchResults;
    RecyclerView recyclerView;
    ArrayList<String> nicknames = new ArrayList<>();
    ArrayList <String> actuallyFollowing = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        Intent myIntent = getIntent();
        searchResults = myIntent.getStringArrayListExtra("users_uid");
        final String name = myIntent.getStringExtra("results_name");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String uid : searchResults){
                    DataSnapshot snapshot = dataSnapshot.child(uid);
                    nicknames.add(snapshot.child("nickname").getValue().toString());
                    Object object = snapshot.child("Followers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue();
                    if(object == null)
                        actuallyFollowing.add(null);
                    else{
                        actuallyFollowing.add(object.toString());
                    }
                }

                setUpRecyclerView();

                presentSearchResults(searchResults, name, nicknames);

                ListView searchResultsListView = findViewById(R.id.list_search_results);

                searchResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedUid = searchResults.get(position);

                        if(selectedUid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            Intent myIntent = new Intent(SearchResultsActivity.this, AccountActivity.class);
                            startActivity(myIntent);
                            finish();
                            return;
                        }
                        Intent myIntent = new Intent(SearchResultsActivity.this, OtherPeoplesAccountActivity.class);
                        myIntent.putExtra("user_uid",selectedUid);
                        startActivity(myIntent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void presentSearchResults(final ArrayList<String> uids, String name, ArrayList<String> nicknames) {


        final ArrayList<RowItem> rowItems = new ArrayList<>();


        for (int i = 0; i < uids.size(); i++) {
            RowItem item = new RowItem(uids.get(i), name, nicknames.get(i), actuallyFollowing.get(i));
            rowItems.add(item);


        }


        FollowingCustomAdapter adapter = new FollowingCustomAdapter(this, rowItems);
        recyclerView.setAdapter(adapter);

    }



    public void backClick(View view) {
        finish();
    }

    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.search_results_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

}
