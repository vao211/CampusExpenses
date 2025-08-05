package com.example.vcampusexpenses.datamanager;

import android.content.Context;
import android.util.Log;

import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Data;
import com.example.vcampusexpenses.model.User;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserDataManager {
    private static final String FILE_NAME = "expense_data.json";
    private static volatile UserDataManager userManagerInstance;
    private final Context context;
    private final File file;
    private UserData userData;
    private final String userId;

    public UserDataManager(Context context, String userId) {
        this.context = context;
        this.userId = userId;
        this.file = new File(context.getFilesDir(), FILE_NAME);
        Log.d("UserDataManager", "File path: " + file.getAbsolutePath());
        Log.d("UserDataManager", "File exists: " + file.exists() + ", writable: " + file.canWrite());
        initializeFile(userId);
    }

    public static synchronized UserDataManager getInstance(Context context, String userId) {
        if (userManagerInstance == null || !userManagerInstance.getUserId().equals(userId)) {
            synchronized (UserDataManager.class) {
                if (userManagerInstance == null || !userManagerInstance.getUserId().equals(userId)) {
                    userManagerInstance = new UserDataManager(context.getApplicationContext(), userId);
                }
            }
        }
        return userManagerInstance;
    }

    public Context getContext() {
        return context;
    }

    public String getUserId() {
        return userId;
    }

    private void createSampleData() {
        Map<String, Account> accounts = new HashMap<>();
        String accountId = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
        String accountId2 = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
        accounts.put(accountId, new Account(accountId, "Cash", 0.0));
        accounts.put(accountId2, new Account(accountId2, "Bank", 0.0));

        Map<String, Category> categories = new HashMap<>();
        String[] categoryNames = {"Ăn uống", "Sức khỏe", "Internet", "Tiền điện", "Tiền nước", "Tiền Lương"};
        for (String name : categoryNames) {
            String categoryId = IdGenerator.generateId(IdGenerator.ModelType.CATEGORY);
            Category category = new Category(categoryId, name);
//            category.setBudgetForAccount(accountId, Integer.MAX_VALUE);
            categories.put(categoryId, category);
        }

        Map<String, AccountBudget> budgets = new HashMap<>();
        Map<String, com.example.vcampusexpenses.model.Transaction> transactions = new HashMap<>();
        Data data = new Data(accounts, categories, budgets, transactions);
        userData = new UserData(new User(userId, data));
        saveData();
    }

    private void initializeFile(String userId) {
        if (!file.exists()) {
            createSampleData();
        } else if (userData == null || !userData.getUser().getUserId().equals(userId)) {
            loadData();
            if (userData == null || !userData.getUser().getUserId().equals(userId)) {
                createSampleData();
            }
        }
    }

    public synchronized void loadData() {
        Log.d("UserDataManager", "Loading data from: " + Thread.currentThread().getStackTrace()[3].toString());
        if (!file.exists()) {
            Log.w("UserDataManager", "No data to load");
            DisplayToast.Display(context, "No data to load");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(reader);
            if (!jsonElement.isJsonObject()) {
                Log.e("UserDataManager", "Invalid JSON format in file");
                DisplayToast.Display(context, "Invalid JSON format in file");
                return;
            }
            userData = gson.fromJson(jsonElement, UserData.class);
            Log.d("UserDataManager", "Loaded JSON: " + gson.toJson(userData));
            if (userData == null) {
                Log.e("UserDataManager", "Failed to load data: JSON is invalid");
                DisplayToast.Display(context, "Failed to load data: JSON is invalid");
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            Log.e("UserDataManager", "Load data error: " + e.getMessage(), e);
            DisplayToast.Display(context, "Load data error: " + e.getMessage());
            userData = null;
        }
    }

    public synchronized void saveData() {
        Log.d("UserDataManager", "Attempting to save data from: " + Thread.currentThread().getStackTrace()[3].toString());
        if (userData == null) {
            Log.w("UserDataManager", "No data to save");
            DisplayToast.Display(context, "No data to save");
            return;
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Log.e("UserDataManager", "Failed to create new file: " + file.getAbsolutePath());
                    DisplayToast.Display(context, "Failed to create data file");
                    return;
                }
            } catch (IOException e) {
                Log.e("UserDataManager", "Error creating file: " + e.getMessage(), e);
                DisplayToast.Display(context, "Error creating data file: " + e.getMessage());
                return;
            }
        }

        if (!file.canWrite()) {
            Log.e("UserDataManager", "File is not writable: " + file.getAbsolutePath());
            DisplayToast.Display(context, "Cannot write to data file");
            return;
        }

        try (FileWriter writer = new FileWriter(file, false)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonContent = gson.toJson(userData);
            Log.d("UserDataManager", "JSON to save: " + jsonContent);
            gson.toJson(userData, writer);
            writer.flush();
            Log.d("UserDataManager", "Data saved successfully to: " + file.getAbsolutePath());

            try (FileReader reader = new FileReader(file)) {
                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(reader);
                Log.d("UserDataManager", "File content after save: " + jsonElement.toString());
            } catch (IOException e) {
                Log.e("UserDataManager", "Error reading file after save: " + e.getMessage(), e);
                DisplayToast.Display(context, "Error verifying saved data: " + e.getMessage());
            }
        } catch (IOException e) {
            Log.e("UserDataManager", "Save data error: " + e.getMessage(), e);
            DisplayToast.Display(context, "Save data error: " + e.getMessage());
        }
    }

    public synchronized JsonObject getUserData(String userId) {
        if (userData != null && userData.getUser().getUserId().equals(userId)) {
            Gson gson = new Gson();
            return gson.toJsonTree(userData.getUser().getData()).getAsJsonObject();
        }
        Log.w("UserDataManager", "User data not found for userId: " + userId);
        DisplayToast.Display(context, "User data not found for userId: " + userId);
        return new JsonObject();
    }

    public UserData getUserDataObject() {
        return userData;
    }
    public void setUserDataObject(UserData userData) {
        this.userData = userData;
        saveData();
    }
}