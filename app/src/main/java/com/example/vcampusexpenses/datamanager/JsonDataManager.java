package com.example.vcampusexpenses.datamanager;

import android.content.Context;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Data;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.User;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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

    public JsonDataManager(Context context) {
        this.context = context;
        this.file = new File(context.getFilesDir(), FILE_NAME);
        initializeFile("default_user");
    }

    public JsonDataManager(Context context, String userId) {
        this.context = context;
        this.file = new File(context.getFilesDir(), FILE_NAME);
        initializeFile(userId);
    }

    public Context getContext() {
        return context;
    }

    private void initializeFile(String userId) {
        if (!file.exists()) {
            // Create sample data
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
            Map<String, Transaction> transactions = new HashMap<>();

            Data data = new Data(accounts, categories, budgets, transactions);
            userData = new UserData(new User(userId, data));
            saveData();
        } else {
            loadData();
        }
    }

    public synchronized void loadData() {
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            userData = gson.fromJson(reader, UserData.class);
        } catch (IOException e) {
            DisplayToast.Display(context, "Load data error: " + e.getMessage());
            return;
        }
    }

    public synchronized void saveData() {
        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(userData, writer);
        } catch (IOException e) {
            DisplayToast.Display(context, "Save data error: " + e.getMessage());
            return;
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
}