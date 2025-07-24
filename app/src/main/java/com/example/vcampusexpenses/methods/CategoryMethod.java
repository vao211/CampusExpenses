package com.example.vcampusexpenses.methods;

import android.content.Context;

import com.example.vcampusexpenses.datamanager.JsonDataFile;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class CategoryMethod {
    private final JsonDataFile dataFile;
    private final String userId;
    public CategoryMethod(Context context, String userId) {
        this.dataFile = new JsonDataFile(context);
        this.userId = userId;
    }
    public void addCategory(Category category) {
        if(category == null || category.getName() == null || category.getName().trim().isEmpty()){
            DisplayToast.Display(dataFile.getContext(), "InValid Category Name");
            return;
        }
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject categories = userData.getAsJsonObject("categories");
        for (String key : categories.keySet()){
            JsonObject existingCategory = categories.getAsJsonObject(key);
            if (existingCategory.get("name").getAsString().equals(category.getName())){
                DisplayToast.Display(dataFile.getContext(), "Category Name Already Exists");
                return;
            }
        }
        String categoryId = IdGenerator.generateId(IdGenerator.ModelType.CATEGORY);
        category.setCategoryId(categoryId);

        JsonObject categoryJson = new JsonObject();
        categoryJson.addProperty("categoryId", categoryId);
        categoryJson.addProperty("name", category.getName());

        categories.add(categoryId, categoryJson);
        dataFile.saveData();
    }
    public void updateCategory(String categoryId, String newName) {
        if(newName == null || newName.trim().isEmpty()){
            DisplayToast.Display(dataFile.getContext(), "InValid Category Name");
            return;
        }
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject categories = userData.getAsJsonObject("categories");
        if(!categories.has(categoryId)){
            DisplayToast.Display(dataFile.getContext(), "Category Not Found");
        }
        for (String key : categories.keySet()){
            JsonObject existingCategory = categories.getAsJsonObject(key);
            if (existingCategory.get("name").getAsString().equals(newName)
                && !existingCategory.get("categoryId").getAsString().equals(categoryId)){
                DisplayToast.Display(dataFile.getContext(), "Category Name Already Exists");
                return;
            }
        }
        JsonObject categoryJson = categories.getAsJsonObject(categoryId);
        categoryJson.addProperty("name", newName);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Update Category Successfully");
    }
    public void deleteCategory(String categoryId) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject categories = userData.getAsJsonObject("categories");
        JsonObject transactions = userData.getAsJsonObject("transactions");
        JsonObject budgets = userData.getAsJsonObject("budgets");

        if(!categories.has(categoryId)){
            DisplayToast.Display(dataFile.getContext(), "Category Not Found");
            return;
        }

        for (String transactionId : transactions.keySet()){
            JsonObject transactionJson = transactions.getAsJsonObject(transactionId);
            if(!transactionJson.get("type").getAsString().equals("TRANSFER")
                && transactionJson.get("categoryId").getAsString().equals(categoryId)){
                DisplayToast.Display(dataFile.getContext(), "Category Is Used In Transaction");
                return;
            }
        }

        for (String budgetId : budgets.keySet()) {
            JsonObject budgetJson = budgets.getAsJsonObject(budgetId);
            JsonObject categoryLimits = budgetJson.getAsJsonObject("categoryLimits");
            if (categoryLimits != null && categoryLimits.has(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category Is Used In Budget");
                return;
            }
        }
    }
    public List<Category> getListCategories() {
        List<Category> categories = new ArrayList<>();
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject categoriesJson = userData.getAsJsonObject("categories");

        for (String categoryId : categoriesJson.keySet()) {
            JsonObject categoryJson = categoriesJson.getAsJsonObject(categoryId);
            Category category = new Category(
                    categoryJson.get("categoryId").getAsString(),
                    categoryJson.get("name").getAsString()
            );
            categories.add(category);
        }
        return categories;
    }
}
