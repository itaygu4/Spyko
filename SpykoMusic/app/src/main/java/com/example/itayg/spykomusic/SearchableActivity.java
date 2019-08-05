package com.example.itayg.spykomusic;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchableActivity extends ListActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the intent, verify the action and get the query
        final Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            finish();
        }
        else if(Intent.ACTION_VIEW.equals(intent.getAction())){

            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");



            final ArrayList<String> users_uid = new ArrayList<>();
            final String name = intent.getDataString();


            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        User user = snapshot.getValue(User.class);


                        if (user != null) {
                            String fullName = user.fullName;
                            String nickname = user.nickname;
                            if (fullName.toLowerCase().equals(name.toLowerCase()) || nickname.toLowerCase().equals(name.toLowerCase()))
                                users_uid.add(snapshot.getKey());
                        }

                    }

                    if (users_uid.size() == 1) {

                        if(users_uid.get(0).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            Intent myIntent = new Intent(SearchableActivity.this, AccountActivity.class);
                            ref.removeEventListener(this);
                            startActivity(myIntent);
                            finish();
                            return;
                        }

                        Intent intent = new Intent(SearchableActivity.this, OtherPeoplesAccountActivity.class);
                        intent.putExtra("user_uid",users_uid.get(0));
                        ref.removeEventListener(this);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    Intent myIntent = new Intent(SearchableActivity.this, SearchResultsActivity.class);
                    myIntent.putStringArrayListExtra("users_uid",users_uid);
                    myIntent.putExtra("results_name", name);
                    ref.removeEventListener(this);
                    startActivity(myIntent);
                    finish();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }



    }

}
