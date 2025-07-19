package com.example.vcampusexpenses;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.authentication.LogInLogOut;
import com.example.vcampusexpenses.session.SessionManager;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    private Button btnLogout;
    private TextView  txtUserEmail2;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUserEmail2 = findViewById(R.id.txtUserEmail2);
        btnLogout = findViewById(R.id.btnLogout);
        sessionManager = new SessionManager(this);

        LoadUserEmail();
        Logout();
    }

    @SuppressLint("SetTextI18n")
    private void LoadUserEmail(){
        String email = sessionManager.getSavedEmail();
        if (email != null) {
            txtUserEmail2.setText("UserEmail: "+ email);
        }
    }
    private void Logout(){
        btnLogout.setOnClickListener(v -> {
            LogInLogOut.LogOut(this);
            finish();
        });
    }
}
