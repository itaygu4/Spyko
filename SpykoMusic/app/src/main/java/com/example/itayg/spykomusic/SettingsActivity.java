package com.example.itayg.spykomusic;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    String realFullName = "", email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.white));

        final SettingsFragment fragment = new SettingsFragment();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_content, fragment)
                .commit();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);


        final DatabaseReference accountInfoReference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        accountInfoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fullName = "";
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.getKey().equals("fullName")){
                        fullName = snapshot.getValue().toString();
                    }
                    if(snapshot.getKey().equals("email")){
                        email = snapshot.getValue().toString();
                    }

                }

                realFullName = fullName.substring(0,1).toUpperCase() + fullName.substring(1);
                String [] words = realFullName.split(" ");
                if(!(words.length == 0))
                    realFullName = "";
                for(int i=0; i< words.length; i++){
                    if(!words[i].equals("")){
                        String word = words[i].substring(0,1).toUpperCase() + words[i].substring(1);
                        if(i == words.length-1)
                            realFullName += word;
                        else
                            realFullName += word +" ";
                    }
                }

                fragment.getNamePref().setSummary(realFullName);
                fragment.getEmailPref().setSummary(email);

                prefs.unregisterOnSharedPreferenceChangeListener(SettingsActivity.this);

                prefs.edit().putString("account_name", realFullName).apply();
                prefs.edit().putString("account_email", email).apply();

                prefs.registerOnSharedPreferenceChangeListener(SettingsActivity.this);

                accountInfoReference.child("fullName").setValue(realFullName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DatabaseReference accountInfoRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        SharedPreferences namePref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        if(key.equals("account_name")){
            String name = namePref.getString("account_name", realFullName);
            accountInfoRef.child("fullName").setValue(name);
        }
        if(key.equals("account_email")){
            String emailValue = namePref.getString("account_email", email);
            accountInfoRef.child("email").setValue(emailValue);
        }
        if(key.equals("account_private")){
            accountInfoRef.child("private").setValue(namePref.getBoolean("account_private", false));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void backClick(View view) {
        finish();
    }
}
