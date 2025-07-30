package com.example.vcampusexpenses.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.GoogleAuthen;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class GoogleLoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private ImageView gifLoading;
    private GoogleAuthen googleAuthen;
    private SessionManager sessionManager;
    private FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        gifLoading = findViewById(R.id.gif_loading);
        loadGif();

        sessionManager = new SessionManager(this);
        googleAuthen = new GoogleAuthen(this);
        fbAuth = FirebaseAuth.getInstance();

        startGoogleSignIn();
    }
    private void startGoogleSignIn() {
        Log.d("GoogleLoginActivity", "Starting Google Sign-In");
        Intent signInIntent = googleAuthen.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Log.d("GoogleLoginActivity", "Handling Google Sign-In result");
            Task<GoogleSignInAccount> task = googleAuthen.handleSignInResult(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    googleAuthen.firebaseAuthWithGoogle(account.getIdToken(), account, new GoogleAuthen.AuthCallback() {
                        @Override
                        public void onSuccess(GoogleSignInAccount account) {
                            Log.d("GoogleLoginActivity", "Firebase authentication successful for email: " + account.getEmail());
                            sessionManager.saveLoginSession(account.getEmail(), fbAuth.getUid());
                            DisplayToast.Display(GoogleLoginActivity.this, "Firebase authentication successful");

                            Intent intent = new Intent(GoogleLoginActivity.this, RegistrationPageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e("GoogleLoginActivity", "Authentication failed: " + errorMessage);
                            DisplayToast.Display(GoogleLoginActivity.this, "Authentication failed: " + errorMessage);
                            Intent intent = new Intent(GoogleLoginActivity.this, RegistrationPageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    Log.e("GoogleLoginActivity", "Google Sign-In failed: Account is null");
                    DisplayToast.Display(this, "Google Sign-In failed: Account is null");
                    Intent intent = new Intent(GoogleLoginActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            } catch (ApiException e) {
                Log.e("GoogleLoginActivity", "Google Sign-In failed: " + e.getStatusCode());
                DisplayToast.Display(this, "Google Sign-In failed: " + e.getStatusCode());
                Intent intent = new Intent(GoogleLoginActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void loadGif() {
        Log.d("GoogleLoginActivity", "Loading GIF");
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(gifLoading);
    }
}