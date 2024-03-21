package com.example.statsforspotify;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
    private String TAG = "LOGIN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginWithSpotifyButton = findViewById(R.id.login_with_spotify);

        loginWithSpotifyButton.setOnClickListener((v) -> {
            Log.d(TAG, "onCreate: loginWithSpotifyButton clicked");
        });
    }
}
