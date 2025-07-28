package com.example.vcampusexpenses.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleAuthen {
    private final GoogleSignInClient googleSignInClient;
    private final FirebaseAuth firebaseAuth;
    private final Context context;

    public interface AuthCallback {
        void onSuccess(GoogleSignInAccount account);
        void onFailure(String errorMessage);
    }

    public GoogleAuthen(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("614210499739-jbs4sm6b3lqeqce4f92quisdc1hih6ld.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public Task<GoogleSignInAccount> handleSignInResult(Intent data) {
        return GoogleSignIn.getSignedInAccountFromIntent(data);
    }

    public void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(account);
                        } else {
                            callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        }
                    }
                });
    }
}