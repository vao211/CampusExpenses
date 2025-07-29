package com.example.vcampusexpenses.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.utils.DisplayToast;

public class AboutActivity extends AppCompatActivity {
    ImageButton btn_back, btn_github;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btn_back = findViewById(R.id.btn_back);
        btn_github = findViewById(R.id.btn_github);

        back();
        openGithub();
    }
    private void openGithub(){
        btn_github.setOnClickListener(v -> {
            String url = "https://github.com/vao211/CampusExpenses";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                DisplayToast.Display(this, "No browser found");
            }
        });
    }
    private void back(){
        btn_back.setOnClickListener(v -> {
            finish();
        });
    }
}
