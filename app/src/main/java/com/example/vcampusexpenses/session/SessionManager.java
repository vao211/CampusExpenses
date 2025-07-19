package com.example.vcampusexpenses.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SessionPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_REGISTRATION_COMPLETED = "registrationCompleted";
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveLoginSession(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getSavedEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public void setRegistrationCompleted() {
        editor.putBoolean(KEY_REGISTRATION_COMPLETED, true);
        editor.apply();
    }

    public boolean isRegistrationCompleted() {
        return pref.getBoolean(KEY_REGISTRATION_COMPLETED, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}