package com.example.vcampusexpenses.services;

import android.content.Context;
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

    public BudgetService(Context context, String userId) {
        this.dataFile = new UserDataManager(context, userId);
        this.userData = dataFile.getUserDataObject();
        this.userId = userId;
    }

    public String getBudgetId(String budgetName) {
        if (budgetName == null || budgetName.trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "Invalid budget name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject budgets = jsonData.getAsJsonObject("budgets");
        if (budgets == null) {
            DisplayToast.Display(dataFile.getContext(), "No budgets found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : budgets.entrySet()) {
            JsonObject budgetJson = entry.getValue().getAsJsonObject();
            if (budgetJson.has("name") && budgetJson.get("name").getAsString().equals(budgetName)) {
                return budgetJson.get("budgetId").getAsString();
            }
        }
        DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetName);
        return null;
    }
    public void addBudget(Budget budget) {
        if (budget == null
                || budget.getName() == null
                || budget.getTotalAmount() <= 0) {
            DisplayToast.Display(dataFile.getContext(), "Budget Info is invalid");
            return;
        }

        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
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

        //Kiểm tra account tồn tại
        for (String accountId : budget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }
        //Kiểm tra category tồn tại
        for (String categoryId : budget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        String budgetId = IdGenerator.generateId(IdGenerator.ModelType.BUDGET);
        budget.setBudgetId(budgetId);

        //thêm budget vào budgets
        budgets.put(budgetId, budget);

        //update danh sách budget của các Account
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

        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Add budget successfully");
    }

    public void updateBudget(String budgetId, Budget newBudget) {
        if (newBudget == null
                || newBudget.getName() == null
                || newBudget.getTotalAmount() <= 0) {
            DisplayToast.Display(dataFile.getContext(), "Budget Info is invalid");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        if (accounts == null || categories == null || budgets == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //Kiểm tra budget tồn tại
        if (!budgets.containsKey(budgetId)) {
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //Kiểm tra account tồn tại
        for (String accountId : newBudget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        //Kiểm tra category tồn tại trong categoryLimits
        for (String categoryId : newBudget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        //Xóa budget khỏi account
        Budget oldBudget = budgets.get(budgetId);
        List<String> oldAccountIds = oldBudget.getAccountIds();
        for (String accountId : oldAccountIds) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                //Xóa budget khỏi danh sách budgets của account (ghi đè)
                budgetsArray.remove(budgetId);
            }
        }

        //Update budget
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
            //kiếm tra đã có budget này trong danh sách chưa
            if (!budgetsArray.contains(budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Update budget successfully");
    }

    public void deleteBudget(String budgetId) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || accounts == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //Kiểm tra budget tồn tại
        if (!budgets.containsKey(budgetId)) {
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //xóa budget khỏi account
        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        for (String accountId : accountIds) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            //xóa budget khỏi danh sách budgets
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        //xóa budget
        budgets.remove(budgetId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Delete budget successfully");
    }

    public List<Budget> getListUserBudgets() {
        List<Budget> listBudgets = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return listBudgets;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        if (budgets != null) {
            listBudgets.addAll(budgets.values());
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
        if (transaction.isTransfer()) {
            return; //transfer không ảnh hưởng đến ngân sách
        }

        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                budget.updateRemaining(transaction);
            }
        }
        dataFile.saveData();
    }

    protected void reverseBudgetUpdate(Transaction transaction) {
        if (transaction.isTransfer()) {
            return; //transfer không ảnh hưởng đến ngân sách
        }
        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                if (transaction.getType().equals("INCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() - transaction.getAmount());
                } else if (transaction.getType().equals("OUTCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() + transaction.getAmount());
                }
            }
        }
        dataFile.saveData();
    }
}