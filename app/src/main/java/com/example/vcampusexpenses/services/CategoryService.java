package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public CategoryService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        Log.d("CategoryService", "Initialized with userId: " + userId);
    }

    public Category getCategory(String categoryId) {
        Log.d("CategoryService", "Getting category for categoryId: " + categoryId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.w("CategoryService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return null;
        }
        return categories.get(categoryId);
    }

    public void addCategory(Category category) {
        Log.d("CategoryService", "Adding category: " + (category != null ? category.getName() : "null"));
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            Log.e("CategoryService", "Invalid category name");
            DisplayToast.Display(dataFile.getContext(), "Invalid category name");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null) {
            categories = new HashMap<>();
            userData.getUser().getData().setCategories(categories);
        }
        for (Category existingCategory : categories.values()) {
            if (existingCategory.getName().equals(category.getName())) {
                Log.w("CategoryService", "Category name already exists: " + category.getName());
                DisplayToast.Display(dataFile.getContext(), "Category name already exists");
                return;
            }
        }
        String categoryId = IdGenerator.generateId(IdGenerator.ModelType.CATEGORY);
        category.setCategoryId(categoryId);

        categories.put(categoryId, category);

        Log.d("CategoryService", "Category added: " + category.getName());
        DisplayToast.Display(dataFile.getContext(), "Category added successfully");
    }

    public void updateCategory(String categoryId, String newName) {
        Log.d("CategoryService", "Updating categoryId: " + categoryId + " with newName: " + newName);
        if (newName == null || newName.trim().isEmpty()) {
            Log.e("CategoryService", "Invalid category name");
            DisplayToast.Display(dataFile.getContext(), "Invalid category name");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("CategoryService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found");
            return;
        }
        for (Category existingCategory : categories.values()) {
            if (existingCategory.getName().equals(newName) && !existingCategory.getCategoryId().equals(categoryId)) {
                Log.w("CategoryService", "Category name already exists: " + newName);
                DisplayToast.Display(dataFile.getContext(), "Category name already exists");
                return;
            }
        }
        Category category = categories.get(categoryId);
        category.setName(newName);

        Log.d("CategoryService", "Category updated: " + newName);
        DisplayToast.Display(dataFile.getContext(), "Category updated successfully");
    }

    public void deleteCategory(String categoryId) {
        Log.d("CategoryService", "Deleting categoryId: " + categoryId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("CategoryService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found");
            return;
        }

        if (transactions != null) {
            for (Transaction transaction : transactions.values()) {
                if (!transaction.isTransfer() && transaction.getCategoryId().equals(categoryId)) {
                    Log.w("CategoryService", "Category is used in transaction: " + categoryId);
                    DisplayToast.Display(dataFile.getContext(), "Category is used in transaction");
                    return;
                }
            }
        }

        categories.remove(categoryId);

        Log.d("CategoryService", "Category deleted: " + categoryId);
        DisplayToast.Display(dataFile.getContext(), "Category deleted successfully");
    }

    public List<Category> getListCategories() {
        Log.d("CategoryService", "Getting list of categories");
        List<Category> categories = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return categories;
        }
        Map<String, Category> categoryMap = userData.getUser().getData().getCategories();
        if (categoryMap != null) {
            categories.addAll(categoryMap.values());
            Log.d("CategoryService", "Found " + categories.size() + " categories");
        } else {
            Log.w("CategoryService", "No categories found");
        }
        return categories;
    }

    public String getCategoryId(String categoryName) {
        Log.d("CategoryService", "Getting categoryId for categoryName: " + categoryName);
        if (categoryName == null || categoryName.trim().isEmpty()) {
            Log.e("CategoryService", "Invalid category name");
            DisplayToast.Display(dataFile.getContext(), "Invalid category name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            Log.e("CategoryService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject categories = jsonData.getAsJsonObject("categories");
        if (categories == null) {
            Log.e("CategoryService", "No categories found");
            DisplayToast.Display(dataFile.getContext(), "No categories found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : categories.entrySet()) {
            JsonObject categoryJson = entry.getValue().getAsJsonObject();
            if (categoryJson.has("name") && categoryJson.get("name").getAsString().equals(categoryName)) {
                String categoryId = categoryJson.get("categoryId").getAsString();
                Log.d("CategoryService", "Found categoryId: " + categoryId + " for categoryName: " + categoryName);
                return categoryId;
            }
        }
        Log.w("CategoryService", "Category not found: " + categoryName);
        DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryName);
        return null;
    }
}