
package com.example.vcampusexpenses.session;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SessionManager {
    private static final String DATABASE_NAME = "Session.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "session";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_REGISTRATION_COMPLETED = "registrationCompleted";

    private final DatabaseHelper dbHelper;

    public SessionManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        KEY_USER_ID + " TEXT PRIMARY KEY, " +
                        KEY_EMAIL + " TEXT, " +
                        KEY_IS_LOGGED_IN + " INTEGER, " +
                        KEY_REGISTRATION_COMPLETED + " INTEGER)";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void saveLoginSession(String email, String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("INSERT OR REPLACE INTO " + TABLE_NAME + " (" +
                            KEY_USER_ID + ", " + KEY_EMAIL + ", " + KEY_IS_LOGGED_IN + ", " + KEY_REGISTRATION_COMPLETED +
                            ") VALUES (?, ?, ?, ?)",
                    new Object[]{userId, email, 1, 0});
        } finally {
            db.close();
        }
    }

    public boolean isLoggedIn() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + KEY_IS_LOGGED_IN + " FROM " + TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_LOGGED_IN)) == 1;
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public String getSavedEmail() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + KEY_EMAIL + " FROM " + TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public String getUserId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + KEY_USER_ID + " FROM " + TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void setRegistrationCompleted() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + KEY_REGISTRATION_COMPLETED + " = 1");
        } finally {
            db.close();
        }
    }

    public boolean isRegistrationCompleted() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + KEY_REGISTRATION_COMPLETED + " FROM " + TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REGISTRATION_COMPLETED)) == 1;
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void logout() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM " + TABLE_NAME);
        } finally {
            db.close();
        }
    }
}


//package com.example.vcampusexpenses.session;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//public class SessionManager {
//    private static final String PREF_NAME = "SessionPrefs";
//    private static final String KEY_EMAIL = "email";
//    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
//    private static final String KEY_REGISTRATION_COMPLETED = "registrationCompleted";
//    private static final String KEY_USER_ID = "userId";
//
//    private final SharedPreferences pref;
//    private final SharedPreferences.Editor editor;
//
//    public SessionManager(Context context) {
//        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
//        editor = pref.edit();
//    }
//
//    public void saveLoginSession(String email, String userId) {
//        editor.putString(KEY_USER_ID, userId);
//        editor.putString(KEY_EMAIL, email);
//        editor.putBoolean(KEY_IS_LOGGED_IN, true);
//        editor.apply();
//    }
//
//    public boolean isLoggedIn() {
//        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
//    }
//
//    public String getSavedEmail() {
//        return pref.getString(KEY_EMAIL, null);
//    }
//
//    public String getUserId() {
//        return pref.getString(KEY_USER_ID, null);
//    }
//    public void setRegistrationCompleted() {
//        editor.putBoolean(KEY_REGISTRATION_COMPLETED, true);
//        editor.apply();
//    }
//
//    public boolean isRegistrationCompleted() {
//        return pref.getBoolean(KEY_REGISTRATION_COMPLETED, false);
//    }
//
//    public void logout() {
//        editor.clear();
//        editor.apply();
//    }
//}