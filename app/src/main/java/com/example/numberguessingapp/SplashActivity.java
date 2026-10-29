package com.example.numberguessingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide action bar if visible
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 3 seconds delay before moving to LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Splash se direct LoginActivity par jane ke liye Intent
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Close splash activity so user cannot return to it
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}