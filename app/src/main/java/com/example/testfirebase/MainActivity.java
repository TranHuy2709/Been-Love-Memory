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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfirebase.utils.FileUtils;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.testfirebase.DownloadService.DOWNLOAD_DATE;
import static com.example.testfirebase.DownloadService.DOWNLOAD_EVENT;
import static com.example.testfirebase.DownloadService.DOWNLOAD_IMAGE;
import static com.example.testfirebase.DownloadService.DOWNLOAD_MEMORY;
import static com.example.testfirebase.fragments.DateCountFragment.FEMALE_BIRTHDAY;
import static com.example.testfirebase.fragments.DateCountFragment.FIRST_DATE;
import static com.example.testfirebase.fragments.DateCountFragment.MALE_BIRTHDAY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String CURRENT_USER_ID= "curentUser";
    public static String DOWNLOAD_TYPE= "downloadType";

    private TextView resetPassword, register;
    private EditText userName, password;
    private CheckBox cbRemember;
    private Button btnLogin;
    private ProgressBar progressBar;

    private SharedPreferences login, currentUserShared;

    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private BroadcastReceiver receiverMemory = new DownloadMemoryCompleteReceiver();
    private BroadcastReceiver receiverImage = new DownloadImageCompleteReceiver();
    private BroadcastReceiver receiverEvent = new DownloadEventCompleteReceiver();
    private BroadcastReceiver receiverDate = new DownloadDateCompleteReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login= getSharedPreferences("login", MODE_PRIVATE);
        currentUserShared= getSharedPreferences(CURRENT_USER_ID, MODE_PRIVATE);

        boolean loginStatus= login.getBoolean("loginStatus", false);
        if(loginStatus){
            startActivity(new Intent(MainActivity.this, MainAppActivity.class));
        }else {
            setupView();
            registerBrocastReceiver();
        }
        Toast.makeText(MainActivity.this, "On Creat", Toast.LENGTH_LONG).show();
    }

    private void registerBrocastReceiver() {
        IntentFilter filterMemory= new IntentFilter(DOWNLOAD_MEMORY);
        registerReceiver(receiverMemory, filterMemory);
        IntentFilter filterImage= new IntentFilter(DOWNLOAD_IMAGE);
        registerReceiver(receiverImage, filterImage);
        IntentFilter filterEvent= new IntentFilter(DOWNLOAD_EVENT);
        registerReceiver(receiverEvent, filterEvent);
        IntentFilter filterDate= new IntentFilter(DOWNLOAD_DATE);
        registerReceiver(receiverDate, filterDate);
    }

    private void setupView(){
        register= findViewById(R.id.loginTxtRegister);
        register.setOnClickListener(this);
        progressBar= findViewById(R.id.loginProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        btnLogin= findViewById(R.id.loginBtnLogin);
        btnLogin.setOnClickListener(this);
        userName= findViewById(R.id.loginEdtEmail);
        password= findViewById(R.id.loginEdtPasword);
        cbRemember= findViewById(R.id.loginCbRemember);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginTxtRegister:
                progressBar.setVisibility(View.VISIBLE);
                startActivity(new Intent(this, RegisterUser.class));
                break;
            case R.id.loginBtnLogin:
                progressBar.setVisibility(View.VISIBLE);
                userLogin();
                break;
        }
    }

    private void userLogin() {
        String email= userName.getText().toString().trim();
        String pass= password.getText().toString().trim();

        if(email.isEmpty()){
            userName.setError("Email is required");
            userName.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            userName.setError("Please enter valid email");
            userName.requestFocus();
            return;
        }

        if(pass.isEmpty()){
            password.setError("Password is required");
            password.requestFocus();
            return;
        }

        if(pass.length()<6){
            password.setError("Password is too short (Min length must be 6 character");
            password.requestFocus();
            return;
        }



        String savedEmail= login.getString("email","");
        String savedPassword= login.getString("password","");

        if(email.equals(savedEmail) && pass.equals(savedPassword)){
            startActivity(new Intent(MainActivity.this, MainAppActivity.class));
            return;
        }
        userSignIn(email,pass);
    }

    private void userSignIn(String email, String pass){
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    login.edit().clear().apply();
                    currentUserShared.edit().clear().apply();
                    FileUtils.deleteInternalStorage(getApplicationContext());

                    downloadMemory();
                }else {
                    Toast.makeText(MainActivity.this, "Login firebase failed, try again", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Login firebase failed", Toast.LENGTH_LONG).show();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(MainActivity.this, "Cancelled login", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void downloadMemory(){
        Intent intent= new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(DOWNLOAD_TYPE, "Memory");
        startService(intent);
    }

    private void downloadImage(){
        Intent intent= new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(DOWNLOAD_TYPE, "Image");
        startService(intent);
    }

    private void downloadEvent(){
        Intent intent= new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(DOWNLOAD_TYPE, "Event");
        startService(intent);
    }

    private void downloadDate(){
        Intent intent= new Intent(MainActivity.this, DownloadService.class);
        intent.putExtra(DOWNLOAD_TYPE, "Date");
        startService(intent);
    }

    class DownloadMemoryCompleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("MemoryComplete",false)){
                Toast.makeText(MainActivity.this,"Download memory completed", Toast.LENGTH_SHORT).show();
                downloadEvent();
            }
        }
    }

    class DownloadImageCompleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("ImageComplete",false)){
                Toast.makeText(MainActivity.this,"Download image completed", Toast.LENGTH_SHORT).show();
                login.edit().putString("email", userName.getText().toString().trim()).apply();
                login.edit().putString("password",password.getText().toString().trim()).apply();
                if(cbRemember.isChecked()){
                    login.edit().putBoolean("loginStatus",true).apply();
                }else {
                    login.edit().putBoolean("loginStatus",false).apply();
                }
                startActivity(new Intent(MainActivity.this, MainAppActivity.class));
            }
        }
    }

    class DownloadEventCompleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("EventComplete",false)){
                Toast.makeText(MainActivity.this,"Download event completed", Toast.LENGTH_SHORT).show();
                downloadDate();
            }
        }
    }

    class DownloadDateCompleteReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("DateComplete",false)){
                Toast.makeText(MainActivity.this,"Download date completed", Toast.LENGTH_SHORT).show();
                downloadImage();

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverMemory);
        unregisterReceiver(receiverImage);
        unregisterReceiver(receiverEvent);
        unregisterReceiver(receiverDate);
    }
}//end