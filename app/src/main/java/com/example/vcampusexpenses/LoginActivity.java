package com.example.vcampusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin, btnRegister;
    ImageButton btnGoogleLogin;
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
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        CheckLoggedIn();
        //AutoLogin();
        Login();
        GoToRegister();
    }

    private void CheckLoggedIn(){
        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void ValidateUser() {
        String password = edtPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // Regex patterns
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String passwordPattern = "^(?=\\S+$).{8,}";

        if (password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!email.matches(emailPattern)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.matches(passwordPattern)) {
            Toast.makeText(this, "Password invalid", Toast.LENGTH_LONG).show();
            return;
        } else {
            LoginWithFirebase(email, password);
        }
    }

    private void LoginWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                sessionManager.saveLoginSession(email, password);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                HandleLoginError(task.getException());
            }
        });
    }

    private void HandleLoginError(Exception exception) {
        String errorMessage;
        if (exception == null) {
            errorMessage = "Unknown error occurred";
        } else {
            switch (Objects.requireNonNull(exception.getMessage())) {
                // Firebase Auth Error Codes
                case "ERROR_INVALID_EMAIL":
                    errorMessage = "Invalid email address";
                    break;
                case "ERROR_USER_NOT_FOUND":
                    errorMessage = "Email not found";
                    break;
                case "ERROR_WRONG_PASSWORD":
                    errorMessage = "Wrong password";
                    break;
                case "ERROR_NETWORK_REQUEST_FAILED":
                    errorMessage = "Network request failed";
                    break;
                default:
                    errorMessage = "Fail to login: " + exception.getMessage();
                    break;
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void Login() {
        btnLogin.setOnClickListener(v -> {
            ValidateUser();

        });
    }

    private void GoToRegister() {
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void AutoLogin(){
        String email = sessionManager.getSavedEmail();
        String password = sessionManager.getSavedPassword();
        if (email != null) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    sessionManager.logout();
                }
            });
        }
    }
}