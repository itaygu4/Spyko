package com.example.itayg.spykomusic;

import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserNames {

    private static ArrayList<String> usersInfo = new ArrayList<>();


    public static ArrayList<String> getUsersInfo() {

        return updateUsers();
    }

    private static ArrayList<String> updateUsers(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String fullName = snapshot.child("fullName").getValue().toString();
                    String nickname = snapshot.child("nickname").getValue().toString();

                    if(!usersInfo.contains(fullName))
                        usersInfo.add(fullName);

                    if(!usersInfo.contains(nickname))
                        usersInfo.add(nickname);





                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
        return usersInfo;
    }
}
