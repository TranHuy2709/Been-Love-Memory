package com.example.testfirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.testfirebase.adapters.AppViewpagerAdapter;
import com.example.testfirebase.fragments.DateCountFragment;
import com.example.testfirebase.fragments.LoveEventsFragment;
import com.example.testfirebase.fragments.MemoryListFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainAppActivity extends AppCompatActivity {

    private List<Fragment> fragments;
    private ViewPager viewPager;

    private long backpress=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        setupFragment();
        viewPager= findViewById(R.id.mainAppViewpager);
        AppViewpagerAdapter adapter = new AppViewpagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    private void setupFragment(){
        fragments= new ArrayList<>();
        fragments.add(new MemoryListFragment());
        fragments.add(new DateCountFragment());
        fragments.add(new LoveEventsFragment());
    }

    @Override
    public void onBackPressed() {
        if(backpress> System.currentTimeMillis()){
            FirebaseAuth.getInstance().signOut();
            SharedPreferences login= getSharedPreferences("login", MODE_PRIVATE);
            login.edit().putBoolean("loginStatus", false).apply();
            startActivity(new Intent(MainAppActivity.this, MainActivity.class));
        }
        backpress= System.currentTimeMillis()+2000;
        Toast.makeText(MainAppActivity.this, "Click again to logout", Toast.LENGTH_SHORT).show();

    }
}