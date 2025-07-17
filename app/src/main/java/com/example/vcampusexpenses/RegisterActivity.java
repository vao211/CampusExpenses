package com.example.vcampusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class RegisterActivity extends AppCompatActivity {
    EditText edtPassword, edtEmail, edtConfirmPassword;
    Button btnRegister, btnLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPassword = findViewById(R.id.edtPassword);
        edtEmail = findViewById(R.id.edtEmail);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();

        Register();
        GotoLogin();
    }
    private void GotoLogin(){
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }
    private void Register(){
        btnRegister.setOnClickListener(v -> {
            ValidateUser();
        });
    }
    private void ValidateUser(){
        String password = edtPassword.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Regex patterns
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String passwordPattern = "^(?=\\S+$).{8,}$";
        if(confirmPassword.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!email.matches(emailPattern)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!password.matches(passwordPattern)) {
            Toast.makeText(this, "Password not strong enough", Toast.LENGTH_LONG).show();
            return;
        }
        else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            RegisterEmailWithFirebase(email, password);
        }
    }
    private void RegisterEmailWithFirebase(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Exception exception = task.getException();
                HandleRegisterError(exception);
            }
        });
    }
    private void HandleRegisterError(Exception exception) {
        String errorMessage;
        if (exception == null) {
            errorMessage = "Unknown error occurred";
        } else {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    errorMessage = "Invalid email address";
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    errorMessage = "Email is already in use";
                    break;
                case "ERROR_WEAK_PASSWORD":
                    errorMessage = "Weak password";
                    break;
                case "ERROR_NETWORK_REQUEST_FAILED":
                    errorMessage = "Network request failed";
                    break;
                case "ERROR_OPERATION_NOT_ALLOWED":
                    errorMessage = "Operation not allowed";
                    break;
                default:
                    errorMessage = "Fail to register: " + exception.getMessage();
                    break;
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}
