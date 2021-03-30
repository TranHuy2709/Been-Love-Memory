package com.example.testfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfirebase.models.User;
import com.example.testfirebase.utils.FileUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.testfirebase.MainActivity.CURRENT_USER_ID;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private String SETUP_EVENT= "event";
    private String SETUP_MEMORY_COUNT= "memoryCount";

    private TextView txtRegister;
    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegisterNewAcc;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private SharedPreferences login;
    private SharedPreferences currentUserShared;
    ArrayList<String> events;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        events= new ArrayList<>();
        setupEvents();
        login= getSharedPreferences("login", MODE_PRIVATE);
        currentUserShared= getSharedPreferences(CURRENT_USER_ID, MODE_PRIVATE);
        setupView();
    }

    private void setupView(){
        txtRegister= findViewById(R.id.registerTxtRegister);
        txtRegister.setOnClickListener(this);
        edtEmail = findViewById(R.id.registerEdtEmail);
        edtPassword= findViewById(R.id.registerEdtPassword);
        edtConfirmPassword=findViewById(R.id.registerEdtConfirmPassword);
        btnRegisterNewAcc= findViewById(R.id.registerBtnRegister);
        btnRegisterNewAcc.setOnClickListener(this);
        progressBar= findViewById(R.id.registerProgressBar);

        mAuth= FirebaseAuth.getInstance();

    }

    private void setupEvents(){
        events.add("14/02,Valentine day|Celeb day");
        events.add("08/03,Woman day");
        events.add("20/10,Vietnam woman day");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerTxtRegister:
                progressBar.setVisibility(View.VISIBLE);
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registerBtnRegister:
                registerNewUser();
                break;
        }
    }

    private void registerNewUser() {
        String email= edtEmail.getText().toString().trim();
        String password= edtPassword.getText().toString().trim();
        String confirmPassword= edtConfirmPassword.getText().toString().trim();

        if(email.isEmpty()){
            edtEmail.setError("Email is required");
            edtEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError("Please enter valid email");
            edtEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            edtPassword.setError("Password is required");
            edtPassword.requestFocus();
            return;
        }

        if(password.length()<6){
            edtPassword.setError("Min password length must be 6 character");
            edtPassword.requestFocus();
            return;
        }

        if(!confirmPassword.equals(password)){
            edtPassword.setError("Password and ConfirmPassword should be the same");
            edtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user= new User(email, password);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        currentUserShared.edit().clear().apply();
                                        FileUtils.deleteInternalStorage(getApplicationContext());
                                        login.edit().putString("email", email).apply();
                                        login.edit().putString("password",password).apply();
                                        login.edit().putBoolean("loginStatus",true).apply();
                                        currentUserShared.edit().putInt("memoryCount",1).apply();
                                        setupEventForNewUser();
                                        pushLoveEventsToFirebase();
                                        pushMemoryCounterToFirebase();
                                        startActivity(new Intent(RegisterUser.this, MainAppActivity.class));
                                    }else {
                                        Toast.makeText(RegisterUser.this,
                                                "Registration is not successfully",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterUser.this,
                        "Registration is failed",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupEventForNewUser(){
        String eventList= "";
        for(String s: events){
            String[] eventDetail= s.split(",");
            String eventDate= eventDetail[0];
            String eventName= eventDetail[1];
            eventList= eventList + eventDate + "," + eventName + "-";
        }
        currentUserShared.edit().putString("event", eventList).apply();
    }

    private void pushLoveEventsToFirebase(){
        for(String s: events){
            String[] eventDetail= s.split(",");
            String key= FileUtils.grenateEventKey(eventDetail[0]);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events")
                    .child(key).setValue(eventDetail[1]);
        }
    }

    private void pushMemoryCounterToFirebase(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("memoryCount")
                .setValue(1);
    }

}