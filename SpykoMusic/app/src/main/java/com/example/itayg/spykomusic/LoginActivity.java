package com.example.itayg.spykomusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.instacart.library.truetime.TrueTime;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.tinify.Tinify;

import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {



    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_login);




        Fabric.with(this, new Crashlytics());

        Tinify.setKey("_6eGbrELTCQhsnNfnBpsumX12GhbBl19");
        mAuth = FirebaseAuth.getInstance();



    }

    @Override
    public void onStart() {
        super.onStart();





        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        // Check if user is signed in (non-null) and update UI accordingly.
        if(!HelperMethods.isNetworkAvailable(this)){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("No internet connection available");
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else {
            if (!TrueTime.isInitialized()) {
                InitTrueTimeAsyncTask trueTime = new InitTrueTimeAsyncTask(this);
                trueTime.execute();
            }
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }

    }

    public void signUp(View view) {
        Intent intent = new Intent (this, SignupActivity.class);
        startActivity(intent);
    }

    public void logIn(View view) {
        EditText emailText = findViewById(R.id.email);
        EditText passwordText = findViewById(R.id.password);
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.equals("") || password.equals("")){
            int redColor = Color.parseColor("#FF5A5F");
            new StyleableToast.Builder(LoginActivity.this)
                    .text("Please fill all the fields")
                    .backgroundColor(redColor)
                    .show();
            return;
        }


        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        int redColor = Color.parseColor("#FF5A5F");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("debug", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("debug", "signInWithEmail:failure", task.getException());
                            new StyleableToast.Builder(LoginActivity.this)
                                    .text("Username or Password is not correct")
                                    .backgroundColor(redColor)
                                    .show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        int blueColor = Color.parseColor("#3899f4");
        if (user != null) {
            new StyleableToast.Builder(LoginActivity.this)
                    .text("Signed in")
                    .backgroundColor(blueColor)
                    .show();
            Intent intent = new Intent (this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
