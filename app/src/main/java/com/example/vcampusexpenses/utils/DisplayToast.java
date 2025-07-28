package com.example.vcampusexpenses.utils;

import android.content.Context;
import android.widget.Toast;

public class DisplayToast {
    public static void Display(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
