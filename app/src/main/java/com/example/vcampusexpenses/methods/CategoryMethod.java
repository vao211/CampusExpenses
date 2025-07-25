package com.example.vcampusexpenses.methods;

import android.content.Context;
import com.example.vcampusexpenses.datamanager.JsonDataManager;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryMethod {
    private final JsonDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public CategoryMethod(Context context, String userId) {
        this.dataFile = new JsonDataManager(context, userId);
        this.userData = dataFile.getUserDataObject();
        this.userId = userId;
    }

    public void addCategory(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Category Name");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
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
                DisplayToast.Display(dataFile.getContext(), "Category Name Already Exists");
                return;
            }
        }
        String categoryId = IdGenerator.generateId(IdGenerator.ModelType.CATEGORY);
        category.setCategoryId(categoryId);

        categories.put(categoryId, category);
        dataFile.saveData();
    }

    public void updateCategory(String categoryId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Category Name");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(categoryId)) {
            DisplayToast.Display(dataFile.getContext(), "Category Not Found");
            return;
        }
        for (Category existingCategory : categories.values()) {
            if (existingCategory.getName().equals(newName) && !existingCategory.getCategoryId().equals(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category Name Already Exists");
                return;
            }
        }
        Category category = categories.get(categoryId);
        category.setName(newName);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Update Category Successfully");
    }

    public void deleteCategory(String categoryId) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();

        if (categories == null || !categories.containsKey(categoryId)) {
            DisplayToast.Display(dataFile.getContext(), "Category Not Found");
            return;
        }

        if (transactions != null) {
            for (Transaction transaction : transactions.values()) {
                if (!transaction.isTransfer() && transaction.getCategoryId().equals(categoryId)) {
                    DisplayToast.Display(dataFile.getContext(), "Category Is Used In Transaction");
                    return;
                }
            }
        }

        if (budgets != null) {
            for (Budget budget : budgets.values()) {
                if (budget.getCategoryLimits() != null && budget.getCategoryLimits().containsKey(categoryId)) {
                    DisplayToast.Display(dataFile.getContext(), "Category Is Used In Budget");
                    return;
                }
            }
        }

        categories.remove(categoryId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Delete Category Successfully");
    }

    public List<Category> getListCategories() {
        List<Category> categories = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return categories;
        }
        Map<String, Category> categoryMap = userData.getUser().getData().getCategories();
        if (categoryMap != null) {
            categories.addAll(categoryMap.values());
        }
        return categories;
    }
    public String getCategoryId(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "Invalid category name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject categories = jsonData.getAsJsonObject("categories");
        if (categories == null) {
            DisplayToast.Display(dataFile.getContext(), "No categories found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : categories.entrySet()) {
            JsonObject categoryJson = entry.getValue().getAsJsonObject();
            if (categoryJson.has("name") && categoryJson.get("name").getAsString().equals(categoryName)) {
                return categoryJson.get("categoryId").getAsString();
            }
        }
        DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryName);
        return null;
    }
}