package com.example.vcampusexpenses.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vcampusexpenses.DisplayToast;
import com.example.vcampusexpenses.LoginActivity;
import com.example.vcampusexpenses.MainActivity;
import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.RegistrationPageActivity;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

@SuppressWarnings("deprecation")
public class GoogleLoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private ImageView gifLoading;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private SessionManager sessionManager;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlelogin);

        gifLoading = findViewById(R.id.gif_loading);
        loadGif();

        //Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("614210499739-jbs4sm6b3lqeqce4f92quisdc1hih6ld.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        startGoogleSignIn();
//        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN); // gọi signInIntent xong sẽ trả về kết quả trong onActivityResult
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken(), account);
                } else {
                    DisplayToast.Display(this,"Google Sign-In failed: Account is null");
                }
            } catch (ApiException e) {
                DisplayToast.Display(this,"Google Sign-In failed: " + e.getStatusCode());
                Intent intent = new Intent(GoogleLoginActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DisplayToast.Display(GoogleLoginActivity.this,
                                    "Firebase authentication successful");
//                            sessionManager.saveLoginSession(firebaseAuth.getCurrentUser().getEmail());
                            sessionManager.saveLoginSession(account.getEmail());
                            Intent intent = new Intent(GoogleLoginActivity.this, RegistrationPageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            DisplayToast.Display(GoogleLoginActivity.this,
                                    "Authentication failed: "
                                            + task.getException().getMessage());
                            Intent intent = new Intent(GoogleLoginActivity.this, RegistrationPageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
    private void loadGif() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(gifLoading);
    }
}