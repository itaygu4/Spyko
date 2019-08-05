package com.example.itayg.spykomusic;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class SignupActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private User user;
    private String email;
    private String password;
    private boolean available;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

        mAuth = FirebaseAuth.getInstance();


    }

    public void signUp(View view) {
        EditText fullNameText = findViewById(R.id.full_name);
        EditText nicknameText = findViewById(R.id.nickname);
        EditText emailText = findViewById(R.id.email);
        EditText passwordText = findViewById(R.id.password);
        String fullName = fullNameText.getText().toString();
        String nickname = nicknameText.getText().toString();
        email = emailText.getText().toString();
        password = passwordText.getText().toString();
        int redColor = Color.parseColor("#FF5A5F");



        if(fullName.equals("")){
            new StyleableToast.Builder(this)
                    .text("Please enter your full name")
                    .backgroundColor(redColor)
                    .show();
            return;
        }

        if(nickname.equals("")){
            new StyleableToast.Builder(this)
                    .text("Please enter a nickname")
                    .backgroundColor(redColor)
                    .show();
            return;
        }

        if(email.equals("")){
            new StyleableToast.Builder(this)
                    .text("Please enter your email")
                    .backgroundColor(redColor)
                    .show();
            return;
        }

        if(password.equals("")){
            new StyleableToast.Builder(this)
                    .text("Please enter a password")
                    .backgroundColor(redColor)
                    .show();
            return;
        }
        try {
            PrintStream output = new PrintStream(openFileOutput("confidential.txt", MODE_APPEND));
            output.println(email + '\t' + password);
            output.close();
        }
        catch (FileNotFoundException fnfe){
            Log.v("Exception","file not found");
        }
        String realName = fullName.substring(0,1).toUpperCase() + fullName.substring(1);
        String [] words = realName.split(" ");
        if(!(words.length == 0))
            realName = "";
        for(int i=0; i< words.length; i++){
            if(!words[i].equals("")){
                String word = words[i].substring(0,1).toUpperCase() + words[i].substring(1);
                if(i == words.length-1)
                    realName += word;
                else
                    realName += word +" ";
            }
        }
        user = new User(realName,nickname,email);
        checkNicknameAvailable(nickname);
    }

    private void checkNicknameAvailable(final String nickname){
        available = true;
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("nickname").getValue().toString().equals(nickname)){
                        available = false;
                        int redColor = Color.parseColor("#FF5A5F");
                        new StyleableToast.Builder(SignupActivity.this)
                                .text("This nickname is already in use")
                                .backgroundColor(redColor)
                                .show();

                        return;
                    }
                }
                if(available)
                    createAccount(email, password);
                usersRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                int redColor = Color.parseColor("#FF5A5F");
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("debug", "createUserWithEmail:success");
                    FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(fbUser.getUid()).setValue(user);
                    updateUI(mAuth.getCurrentUser());
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("debug", "createUserWithEmail:failure", task.getException());

                    try {
                        throw task.getException();
                    } catch(FirebaseAuthWeakPasswordException e) {
                        new StyleableToast.Builder(SignupActivity.this)
                                .text("Password must be at least 6 characters long")
                                .backgroundColor(redColor)
                                .show();
                    } catch(FirebaseAuthInvalidCredentialsException e) {
                        new StyleableToast.Builder(SignupActivity.this)
                                .text("Not a valid mail address")
                                .backgroundColor(redColor)
                                .show();
                    } catch(FirebaseAuthUserCollisionException e) {
                        new StyleableToast.Builder(SignupActivity.this)
                                .text("Email address is already in use")
                                .backgroundColor(redColor)
                                .show();
                    } catch(Exception e) {
                        Log.e("Exception", e.getMessage());
                    }
                    updateUI(null);
                }

                // ...
            }
        });
    }

    private void updateUI(FirebaseUser user){
        int blueColor = Color.parseColor("#3899f4");
        if (user != null) {
            new StyleableToast.Builder(SignupActivity.this)
                    .text("Signed in")
                    .backgroundColor(blueColor)
                    .show();
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            return;
        }
    }

    public void backClick(View view) {
        finish();
    }
}
