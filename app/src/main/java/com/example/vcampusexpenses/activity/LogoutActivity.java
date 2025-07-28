package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vcampusexpenses.R;

public class LogoutActivity extends AppCompatActivity {
    private ImageView gifLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        gifLoading = findViewById(R.id.gif_loading);
        loadGif();
    }

    private void loadGif() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(gifLoading);
    }
}
