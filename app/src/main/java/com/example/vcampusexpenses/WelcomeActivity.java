package com.example.vcampusexpenses;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class WelcomeActivity extends AppCompatActivity {
    private ImageView gif_welcome;
    private Button btn_next;
    private SharedPreferences sharedPreferences;


    //Tên file lưu trữ
    private static final String PREFS_NAME = "FirstTimeCheckPref";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Kiểm tra lần đầu chạy app
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(KEY_FIRST_RUN, true)) {
            startLoginActivity();
            return;
        }

        setContentView(R.layout.activity_welcome);

        gif_welcome = findViewById(R.id.gif_welcome);
        btn_next = findViewById(R.id.btn_next);

        LoadGif();
        goNext();
    }

    private void goNext() {
        btn_next.setOnClickListener(v -> {
            //Đã hoàn thành chạy app lần đầu
            sharedPreferences.edit().putBoolean(KEY_FIRST_RUN, false).apply();
            startLoginActivity();
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void LoadGif() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.welcome)
                .into(gif_welcome);
    }
}