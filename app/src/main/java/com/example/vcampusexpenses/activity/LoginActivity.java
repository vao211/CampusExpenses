package com.example.vcampusexpenses.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.FireBaseAuthen;
import com.example.vcampusexpenses.authentication.GuestAuthen;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin, btnRegister;
    ImageButton btnGoogleLogin, btnGuestLogin;
    FirebaseAuth mAuth;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGuestLogin = findViewById(R.id.btn_guestLogin);
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        CheckLoggedIn();
        GuestLogin();
        GoogleLogin();
        Login();
        GoToRegister();
    }
    private void GuestLogin(){
        btnGuestLogin.setOnClickListener(v -> {
            GuestAuthen.LogIn(this);
            finish();
        });
    }

    private void GoogleLogin() {
        btnGoogleLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, GoogleLoginActivity.class);
            startActivity(intent);
        });
    }
    private void CheckLoggedIn(){
        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void Login() {
        btnLogin.setOnClickListener(v -> {
            FireBaseAuthen.LogIn(this, edtEmail.getText().toString(), edtPassword.getText().toString());
        });
    }
    private void GoToRegister() {
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
}