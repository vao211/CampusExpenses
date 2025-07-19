package com.example.vcampusexpenses.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserDB {
    private final FirebaseAuth fbAuth;
    private final FirebaseFirestore fireStoreDB;

    //callback truy vấn
    public interface UserDataCallback {
        void onSuccess(String displayName, String realName, String dateOfBirth);
        void onError(String errorMessage);
    }
    public UserDB(){
        fbAuth  = FirebaseAuth.getInstance();
        fireStoreDB = FirebaseFirestore.getInstance();
    }
    public void loadUserData(String userId, UserDataCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("UserID cannot be null or empty");
            return;
            }
        fireStoreDB.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String displayName = documentSnapshot.getString("displayName");
                        String realName = documentSnapshot.getString("realName");
                        String dateOfBirth = documentSnapshot.getString("dateOfBirth");
                        callback.onSuccess(displayName, realName, dateOfBirth);
                    }
                    else{
                        callback.onSuccess(null, null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError("Error when loading user data: " + e.getMessage());
                });
    }
    public void updateUserData(String userId, String displayName, String realName, String dateOfBirth, UserDataCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("UserID cannot be null or empty");
            return;
        }
        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", displayName);
        userData.put("realName", realName);
        userData.put("dateOfBirth", dateOfBirth);
        fireStoreDB.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(displayName, realName, dateOfBirth);
                })
                .addOnFailureListener( e -> {
                            callback.onError("Error when updating user data: " + e.getMessage());
                        }
                );
    }
}
