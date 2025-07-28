package com.example.vcampusexpenses.authentication;

import android.content.Context;
import android.content.Intent;

import com.example.vcampusexpenses.activity.LoginActivity;
import com.example.vcampusexpenses.activity.MainActivity;
import com.example.vcampusexpenses.activity.RegistrationPageActivity;
import com.example.vcampusexpenses.datamanager.JsonDataManager;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

public class GuestAuthen {
    private static void createSampleData(Context context) {
        JsonDataManager dataManager = new JsonDataManager(context);
    }
    public static void LogIn(Context context){
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.saveLoginSession("Guest", "Guest");
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        DisplayToast.Display(context, "Login as Guest Complete");
    }
    public static void LogOut(Context context){
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.logout();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        DisplayToast.Display(context, "Logout Complete");
    }
}
