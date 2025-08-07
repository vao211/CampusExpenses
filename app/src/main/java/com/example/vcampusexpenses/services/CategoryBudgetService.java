package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.CategoryBudget;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryBudgetService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public CategoryBudgetService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        Log.d("CategoryBudgetService", "Initialized with userId: " + userId);
    }

    public Map<String, CategoryBudget> getAllCategoryBudgets(){
        Log.d("CategoryBudgetService", "Getting list of category budgets");
        Map<String, CategoryBudget> mapAccountInCategoryBudgets = new HashMap<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            return mapAccountInCategoryBudgets;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null) {
            Log.e("CategoryBudgetService", "No categories found");
            return mapAccountInCategoryBudgets;
        }

        for (Map.Entry<String, Category> entry : categories.entrySet()) {
            String categoryId = entry.getKey();
            Category category = entry.getValue();
            Log.d("CategoryBudgetService", "Category: " + category.getName() + ", CategoryId: " + categoryId);
            Map<String, CategoryBudget> accountInCategoryBudgets = category.getAccountInCategoryBudgets();
            if (accountInCategoryBudgets != null) {
                mapAccountInCategoryBudgets.putAll(accountInCategoryBudgets);
            }
        }
        Log.d("CategoryBudgetService", "List of category budgets retrieved");
        return mapAccountInCategoryBudgets;
    }
    public void setBudgetForAccount(String categoryId, String accountId, double totalAmount, Double remainingAmount) {
        Log.d("CategoryBudgetService", "Setting budget for categoryId: " + categoryId + " and accountId: " + accountId + ", totalAmount: " + totalAmount + ", remainingAmount: " + (remainingAmount != null ? remainingAmount : "null"));
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("CategoryBudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return;
        }
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("CategoryBudgetService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }
        Category category = categories.get(categoryId);
        double finalRemainingAmount = (remainingAmount != null) ? remainingAmount : totalAmount;
        if (totalAmount <= 0) {
            category.removeBudgetForAccount(accountId);
        } else {
            if (category.getAccountInCategoryBudgets() == null) {
                category.setAccountInCategoryBudgets(new HashMap<>());
            }
            category.getAccountInCategoryBudgets().put(accountId, new CategoryBudget(categoryId, accountId, totalAmount, finalRemainingAmount));
        }
        dataFile.saveData();
        Log.d("CategoryBudgetService", "Budget set for categoryId: " + categoryId + " and accountId: " + accountId);
        DisplayToast.Display(dataFile.getContext(), "Category budget set successfully");
    }

    public CategoryBudget getCategoryBudget(String categoryId, String accountId) {
        Log.d("CategoryBudgetService", "Getting budget for categoryId: " + categoryId + " and accountId: " + accountId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("CategoryBudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return null;
        }
        Category category = categories.get(categoryId);
        CategoryBudget budget = category.getBudgetForAccount(accountId);
        if (budget == null) {
            Log.w("CategoryBudgetService", "No budget set for categoryId: " + categoryId + " and accountId: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "No budget set for this category and account");
        }
        return budget;
    }

    public void removeCategoryBudget(String categoryId, String accountId) {
        Log.d("CategoryBudgetService", "Removing budget for categoryId: " + categoryId + " and accountId: " + accountId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("CategoryBudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return;
        }
        Category category = categories.get(categoryId);
        if (category.getBudgetForAccount(accountId) == null) {
            Log.w("CategoryBudgetService", "No budget set for categoryId: " + categoryId + " and accountId: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "No budget set for this category and account");
            return;
        }
        category.removeBudgetForAccount(accountId);
        dataFile.saveData();
        Log.d("CategoryBudgetService", "Budget removed for categoryId: " + categoryId + " and accountId: " + accountId);
        DisplayToast.Display(dataFile.getContext(), "Category budget removed successfully");
    }

    public void updateCategoryBudgetsInTransaction(Transaction transaction) {
        Log.d("CategoryBudgetService", "Updating category budgets for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("CategoryBudgetService", "Transaction is a transfer, skipping category budget update");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(transaction.getCategoryId())) {
            Log.w("CategoryBudgetService", "Category not found for transaction: " + transaction.getCategoryId());
            return;
        }
        Category category = categories.get(transaction.getCategoryId());
        CategoryBudget budget = category.getBudgetForAccount(transaction.getAccountId());
        if (budget != null) {
            budget.updateRemaining(transaction);
            dataFile.saveData();
            Log.d("CategoryBudgetService", "Updated category budget for categoryId: " + transaction.getCategoryId() + ", accountId: " + transaction.getAccountId() + ", remaining: " + budget.getRemainingAmount());
        }
    }

    public void reverseCategoryBudgetUpdate(Transaction transaction) {
        Log.d("CategoryBudgetService", "Reversing category budget update for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("CategoryBudgetService", "Transaction is a transfer, skipping reverse category budget update");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("CategoryBudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (categories == null || !categories.containsKey(transaction.getCategoryId())) {
            Log.w("CategoryBudgetService", "Category not found for transaction: " + transaction.getCategoryId());
            return;
        }
        Category category = categories.get(transaction.getCategoryId());
        CategoryBudget budget = category.getBudgetForAccount(transaction.getAccountId());
        if (budget != null) {
            budget.reverseUpdate(transaction);
            dataFile.saveData();
            Log.d("CategoryBudgetService", "Reversed category budget for categoryId: " + transaction.getCategoryId() + ", accountId: " + transaction.getAccountId() + ", remaining: " + budget.getRemainingAmount());
        }
    }
}