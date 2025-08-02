// /app/src/main/java/com/example/vcampusexpenses/manager/SettingManager.java
package com.example.vcampusexpenses.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {

    private static final String PREFERENCES_FILE = "AppSettings";
    private static final String KEY_CURRENCY = "currency_symbol";
    private static final String DEFAULT_CURRENCY = "$"; // Default currency

    // Method to get the currency symbol
    public static String getCurrency(Context context) {
        if (context == null) {
            return DEFAULT_CURRENCY;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    // Method to set the currency symbol
    public static void setCurrency(Context context, String currencySymbol) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CURRENCY, currencySymbol);
        editor.apply();
    }

}
