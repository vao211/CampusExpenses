package com.example.vcampusexpenses;

import android.content.Context;
import android.view.Display;
import android.widget.Toast;

public class DisplayToast {
    public static void Display(Context context, String message) {
        Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
