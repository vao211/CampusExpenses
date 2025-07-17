package com.example.vcampusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    Button btnLogout;
    TextView txtUserEmail1, txtUserEmail2;
    SessionManager sessionManager;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUserEmail1 = findViewById(R.id.txtUserEmail1);
        txtUserEmail2 = findViewById(R.id.txtUserEmail2);
        btnLogout = findViewById(R.id.btnLogout);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();

        LoadUserEmailUsingFireBase();
        LoadUserEmailUsingPreferences();
        Logout();
    }

    private void LoadUserEmailUsingFireBase(){
        assert mAuth.getCurrentUser() != null;
        String userEmail = mAuth.getCurrentUser().getEmail();
        txtUserEmail1.setText(userEmail);
    }
    private void LoadUserEmailUsingPreferences(){
        String email = sessionManager.getSavedEmail();
        if (email != null) {
            txtUserEmail2.setText(email);
        }
    }
    private void Logout(){
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
