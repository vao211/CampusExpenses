package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public BudgetService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        Log.d("BudgetService", "Initialized with userId: " + userId);
    }

    public String getBudgetId(String budgetName) {
        Log.d("BudgetService", "Getting budgetId for budgetName: " + budgetName);
        if (budgetName == null || budgetName.trim().isEmpty()) {
            Log.e("BudgetService", "Invalid budget name");
            DisplayToast.Display(dataFile.getContext(), "Invalid budget name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject budgets = jsonData.getAsJsonObject("budgets");
        if (budgets == null) {
            Log.e("BudgetService", "No budgets found");
            DisplayToast.Display(dataFile.getContext(), "No budgets found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : budgets.entrySet()) {
            JsonObject budgetJson = entry.getValue().getAsJsonObject();
            if (budgetJson.has("name") && budgetJson.get("name").getAsString().equals(budgetName)) {
                String budgetId = budgetJson.get("budgetId").getAsString();
                Log.d("BudgetService", "Found budgetId: " + budgetId + " for budgetName: " + budgetName);
                return budgetId;
            }
        }
        Log.w("BudgetService", "Budget not found: " + budgetName);
        DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetName);
        return null;
    }

    public void addBudget(Budget budget) {
        Log.d("BudgetService", "Adding budget: " + (budget != null ? budget.getName() : "null"));
        if (budget == null || budget.getName() == null || budget.getTotalAmount() <= 0) {
            Log.e("BudgetService", "Budget info is invalid");
            DisplayToast.Display(dataFile.getContext(), "Budget info is invalid");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();

        if (accounts == null || categories == null || budgets == null) {
            accounts = accounts == null ? new HashMap<>() : accounts;
            categories = categories == null ? new HashMap<>() : categories;
            budgets = budgets == null ? new HashMap<>() : budgets;
            userData.getUser().getData().setAccount(accounts);
            userData.getUser().getData().setCategories(categories);
            userData.getUser().getData().setBudgets(budgets);
        }

        //ktra account tồn tại
        for (String accountId : budget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }
        //ktra category tồn tại
        for (String categoryId : budget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                Log.e("BudgetService", "Category not found: " + categoryId);
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        String budgetId = IdGenerator.generateId(IdGenerator.ModelType.BUDGET);
        budget.setBudgetId(budgetId);

        //thêm budget vào budgets
        budgets.put(budgetId, budget);

        //update list budget của các Account
        for (String accountId : budget.getAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray == null) {
                budgetsArray = new ArrayList<>();
                account.setBudgetIds(budgetsArray);
            }
            if (!budgetsArray.contains(budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        Log.d("BudgetService", "Budget added: " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Budget added successfully");
    }

    public void updateBudget(String budgetId, Budget newBudget) {
        Log.d("BudgetService", "Updating budgetId: " + budgetId);
        if (newBudget == null || newBudget.getName() == null || newBudget.getTotalAmount() <= 0) {
            Log.e("BudgetService", "Budget info is invalid");
            DisplayToast.Display(dataFile.getContext(), "Budget info is invalid");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        if (accounts == null || categories == null || budgets == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //kiểm tra budget tồn tại
        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //kiểm tra account tồn tại
        for (String accountId : newBudget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        //ktra category tồn tại trong categoryLimits
        for (String categoryId : newBudget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                Log.e("BudgetService", "Category not found: " + categoryId);
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        //xóa budget khỏi account
        Budget oldBudget = budgets.get(budgetId);
        List<String> oldAccountIds = oldBudget.getAccountIds();
        for (String accountId : oldAccountIds) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        //update budget
        newBudget.setBudgetId(budgetId);
        budgets.put(budgetId, newBudget);

        //update danh sách budget của các Account mới
        for (String accountId : newBudget.getAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray == null) {
                budgetsArray = new ArrayList<>();
                account.setBudgetIds(budgetsArray);
            }
            if (!budgetsArray.contains(budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        Log.d("BudgetService", "Budget updated: " + newBudget.getName());
        DisplayToast.Display(dataFile.getContext(), "Budget updated successfully");
    }

    public void deleteBudget(String budgetId) {
        Log.d("BudgetService", "Deleting budgetId: " + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || accounts == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //Ktra budget tồn tại
        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //xóa budget khỏi account
        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        for (String accountId : accountIds) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        budgets.remove(budgetId);
        Log.d("BudgetService", "Budget deleted: " + budgetId);
        DisplayToast.Display(dataFile.getContext(), "Budget deleted successfully");
    }

    public List<Budget> getListUserBudgets() {
        Log.d("BudgetService", "Getting list of budgets");
        List<Budget> listBudgets = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return listBudgets;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        if (budgets != null) {
            listBudgets.addAll(budgets.values());
            Log.d("BudgetService", "Found " + listBudgets.size() + " budgets");
        } else {
            Log.w("BudgetService", "No budgets found");
        }
        return listBudgets;
    }

    private boolean checkContainsInJsonArray(JsonArray array, String value) {
        for (JsonElement element : array) {
            if (element.getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    protected void updateBudgetsInTransaction(Transaction transaction) {
        Log.d("BudgetService", "Updating budgets for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping budget update");
            return;
        }

        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                budget.updateRemaining(transaction);
                Log.d("BudgetService", "Updated budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
        // Không gọi saveData() ở đây
    }

    protected void reverseBudgetUpdate(Transaction transaction) {
        Log.d("BudgetService", "Reversing budget update for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping reverse budget update");
            return;
        }
        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                if (transaction.getType().equals("INCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() - transaction.getAmount());
                } else if (transaction.getType().equals("OUTCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() + transaction.getAmount());
                }
                Log.d("BudgetService", "Reversed budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
        // Không gọi saveData() ở đây
    }
}