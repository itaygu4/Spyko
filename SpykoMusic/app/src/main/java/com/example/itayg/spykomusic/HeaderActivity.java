package com.example.itayg.spykomusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;



public class HeaderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_playlist);

        TextView headerView = findViewById(R.id.headerText);

        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            headerView.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
    }
}

