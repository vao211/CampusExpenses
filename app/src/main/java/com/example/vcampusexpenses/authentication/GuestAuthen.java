package com.example.vcampusexpenses.authentication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.vcampusexpenses.activity.LoginActivity;
import com.example.vcampusexpenses.activity.MainActivity;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.SettingService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.ArrayList;

public class GuestAuthen {
    public static void LogIn(Context context) {
        Log.d("GuestAuthen", "Logging in as Guest");
        SessionManager sessionManager = new SessionManager(context);
        UserDataManager dataManager = UserDataManager.getInstance(context, "Guest");
        SettingService settingService = new SettingService(context);

        sessionManager.saveLoginSession("Guest", "Guest");
        settingService.setDisplayName("Guest");

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        DisplayToast.Display(context, "Login as Guest Complete");
        Log.d("GuestAuthen", "Guest login completed");
    }

    public static void LogOut(Context context) {
        Log.d("GuestAuthen", "Logging out Guest user");
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.logout();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        DisplayToast.Display(context, "Logout Complete");
        Log.d("GuestAuthen", "Guest logout completed");
    }
}