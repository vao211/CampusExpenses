package com.example.vcampusexpenses;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.authentication.LogInLogOut;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    private Button btnLogout;
    private TextView  txtUserEmail2;
    private SessionManager sessionManager;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUserEmail2 = findViewById(R.id.txtUserEmail2);
        btnLogout = findViewById(R.id.btnLogout);
        sessionManager = new SessionManager(this);
        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("614210499739-jbs4sm6b3lqeqce4f92quisdc1hih6ld.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        LoadUserEmail();
        Logout();
    }

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
