package com.example.vcampusexpenses.datamanager;

import android.content.Context;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Data;
import com.example.vcampusexpenses.model.User;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.session.SessionManager;
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

public class JsonDataManager {
    private static final String FILE_NAME = "expense_data.json";
    private final Context context;
    private final File file;
    private UserData userData;

    public JsonDataManager(Context context, String userId) {
        this.context = context;
        this.file = new File(context.getFilesDir(), FILE_NAME);
        initializeFile(userId);
    }

    public JsonDataManager(Context context) {
        this.context = context;
        SessionManager sessionManager = new SessionManager(context);
        this.file = new File(context.getFilesDir(), FILE_NAME);
        initializeFile(sessionManager.getUserId());
    }

    public Context getContext() {
        return context;
    }

    private void initializeFile(String userId) {
        if (!file.exists()) {
            // Create sample data for the given userId
            Map<String, Account> accounts = new HashMap<>();
            String accountId = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
            accounts.put(accountId, new Account(accountId, "Cash", 0.0));

            Map<String, Category> categories = new HashMap<>();
            String[] categoryNames = {"Quần Áo", "Ăn uống", "Giáo dục", "Sức khỏe", "Internet", "Điện thoại", "Tiền điện", "Tiền nước"};
            for (String name : categoryNames) {
                String categoryId = IdGenerator.generateId(IdGenerator.ModelType.CATEGORY);
                categories.put(categoryId, new Category(categoryId, name));
            }

            Map<String, Budget> budgets = new HashMap<>();
            Map<String, com.example.vcampusexpenses.model.Transaction> transactions = new HashMap<>();

            Data data = new Data(accounts, categories, budgets, transactions);
            userData = new UserData(new User(userId, data));
            saveData();
        } else {
            loadData();
            // Ensure the loaded data matches the userId
            if (userData == null || !userData.getUser().getUserId().equals(userId)) {
                // Reset data for the new userId
                userData = new UserData(new User(userId, new Data(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>())));
                saveData();
            }
        }
    }

    public synchronized void loadData() {
        if (!file.exists()) {
            DisplayToast.Display(context, "No data to load");
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonElement jsonElement = parser.parse(reader);
            if (!jsonElement.isJsonObject()) {
                DisplayToast.Display(context, "Invalid JSON format in file");
                return;
            }
            userData = gson.fromJson(jsonElement, UserData.class);
            if (userData == null) {
                DisplayToast.Display(context, "Failed to load data: JSON is invalid");
            }
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            DisplayToast.Display(context, "Load data error: " + e.getMessage());
            userData = null;
        }
    }

    public synchronized void saveData() {
        if (userData == null) {
            DisplayToast.Display(context, "No data to save");
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(userData, writer);
            writer.flush(); // Ensure data is written
        } catch (IOException e) {
            DisplayToast.Display(context, "Save data error: " + e.getMessage());
        }
    }

    public synchronized JsonObject getUserData(String userId) {
        if (userData != null && userData.getUser().getUserId().equals(userId)) {
            Gson gson = new Gson();
            return gson.toJsonTree(userData.getUser().getData()).getAsJsonObject();
        }
        DisplayToast.Display(context, "User data not found for userId: " + userId);
        return new JsonObject();
    }

    public UserData getUserDataObject() {
        return userData;
    }
}