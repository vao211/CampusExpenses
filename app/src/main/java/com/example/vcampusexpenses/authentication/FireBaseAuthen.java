package com.example.vcampusexpenses.authentication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.activity.LoginActivity;
import com.example.vcampusexpenses.activity.RegistrationPageActivity;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

public class FireBaseAuthen {

    public static void LogIn(Context context, String email, String password) {
        Log.d("FireBaseAuthen", "Attempting login for email: " + email);
        SessionManager sessionManager = new SessionManager(context);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Regex patterns
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String passwordPattern = "^(?=\\S+$).{8,}";

        if (password.isEmpty() || email.isEmpty()) {
            Log.w("FireBaseAuthen", "Empty email or password");
            DisplayToast.Display(context, "Please fill all fields");
            return;
        } else if (!email.matches(emailPattern)) {
            Log.w("FireBaseAuthen", "Invalid email format: " + email);
            DisplayToast.Display(context, "Please enter a valid email address");
            return;
        } else if (password.length() < 8) {
            Log.w("FireBaseAuthen", "Password too short: " + password.length() + " characters");
            DisplayToast.Display(context, "Password must be at least 8 characters");
            return;
        } else if (!password.matches(passwordPattern)) {
            Log.w("FireBaseAuthen", "Invalid password format");
            DisplayToast.Display(context, "Password invalid");
            return;
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                sessionManager.saveLoginSession(user.getEmail(), user.getUid());
                                Log.d("FireBaseAuthen", "Login successful for userId: " + user.getUid());


                                Intent intent = new Intent(context, RegistrationPageActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);

                                if (context instanceof android.app.Activity) {
                                    ((android.app.Activity) context).finish();
                                }
                                DisplayToast.Display(context, "Login successful");
                            } else {
                                Log.e("FireBaseAuthen", "Login failed: User data not found");
                                DisplayToast.Display(context, "Login failed: User data not found");
                            }
                        } else {
                            Log.e("FireBaseAuthen", "Login failed: " + task.getException());
                            HandleLoginError(context, task.getException());
                        }
                    });
        }
    }

    @SuppressWarnings("deprecation")
    public static void LogOut(Context context) {
        Log.d("FireBaseAuthen", "Logging out");
        SessionManager sessionManager = new SessionManager(context);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("614210499739-jbs4sm6b3lqeqce4f92quisdc1hih6ld.apps.googleusercontent.com")
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);

        // Firebase
        mAuth.signOut();

        // Google
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            sessionManager.logout();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).finish();
            }
            String message = task.isSuccessful() ? "Logged out successfully" : "Logout failed";
            Log.d("FireBaseAuthen", message);
            DisplayToast.Display(context, message);
        });
    }

    private static void HandleLoginError(Context context, Exception exception) {
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
        Log.e("FireBaseAuthen", "Login error: " + errorMessage);
        DisplayToast.Display(context, errorMessage);
    }
}