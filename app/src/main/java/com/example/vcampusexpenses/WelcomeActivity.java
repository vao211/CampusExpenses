package com.example.vcampusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class WelcomeActivity extends AppCompatActivity {
    private ImageView gif_welcome;
    private Button btn_next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        gif_welcome = findViewById(R.id.gif_welcome);
        LoadGif();

        btn_next = findViewById(R.id.btn_next);
        goNext();
    }
    private void goNext(){
        btn_next.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
                });
    }
    private void LoadGif(){
        Glide.with(this).asGif().
                load(R.drawable.welcome).
                into(gif_welcome);
    }
}
