package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
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

public class AccountBudgetService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;
    private final CategoryBudgetService categoryBudgetService;

    public AccountBudgetService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        this.categoryBudgetService = new CategoryBudgetService(dataManager);
        Log.d("BudgetService", "Initialized with userId: " + userId);
    }

    public AccountBudget getBudget(String budgetId) {
        Log.d("BudgetService", "Getting budget: " + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            return null;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            return null;
        }
        AccountBudget budget = budgets.get(budgetId);
        Log.d("BudgetService", "Budget found: " + budget.getName());
        return budget;
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

    public void addBudget(AccountBudget budget) {
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
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();

        if (accounts == null) {
            accounts = new HashMap<>();
        }
        if (budgets == null) {
            budgets = new HashMap<>();
        }

        userData.getUser().getData().setAccount(accounts);
        userData.getUser().getData().setBudgets(budgets);

        for (String accountId : budget.getListAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        String budgetId = IdGenerator.generateId(IdGenerator.ModelType.BUDGET);
        budget.setBudgetId(budgetId);

        budgets.put(budgetId, budget);

        for (String accountId : budget.getListAccountIds()) {
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

    public void updateBudget(String budgetId, AccountBudget newBudget, boolean override) {
        Log.d("BudgetService", "Updating budgetId: " + budgetId + ", override: " + override);

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

        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || accounts == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        for (String accountId : newBudget.getListAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        AccountBudget oldBudget = budgets.get(budgetId);

        for (String accountId : oldBudget.getListAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        AccountBudget updatedBudget = new AccountBudget(
                budgetId,
                newBudget.getName(),
                newBudget.getTotalAmount(),
                newBudget.getRemainingAmount(),
                newBudget.getStartDate(),
                newBudget.getEndDate()
        );

        if (override || newBudget.getListAccountIds().isEmpty()) {
            updatedBudget.setListAccountIds(new ArrayList<>(newBudget.getListAccountIds()));
        } else {
            updatedBudget.setListAccountIds(new ArrayList<>(oldBudget.getListAccountIds()));
        }

        budgets.put(budgetId, updatedBudget);

        for (String accountId : updatedBudget.getListAccountIds()) {
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


        Log.d("BudgetService", "Budget updated: " + updatedBudget.getName());
        DisplayToast.Display(dataFile.getContext(), "Budget updated successfully");
    }

    public void deleteBudget(String budgetId) {
        Log.d("BudgetService", "Deleting budgetId: " + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || accounts == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        AccountBudget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getListAccountIds();
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

    public List<AccountBudget> getListUserBudgets() {
        Log.d("BudgetService", "Getting list of budgets");
        List<AccountBudget> listBudgets = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return listBudgets;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        if (budgets != null) {
            listBudgets.addAll(budgets.values());
            Log.d("BudgetService", "Found " + listBudgets.size() + " budgets");
        } else {
            Log.w("BudgetService", "No budgets found");
        }
        return listBudgets;
    }

    public void updateBudgetsInTransaction(Transaction transaction) {
        Log.d("BudgetService", "Updating budgets for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping budget update");
            return;
        }

        List<AccountBudget> budgets = getListUserBudgets();
        for (AccountBudget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                budget.updateRemaining(transaction);
                Log.d("BudgetService", "Updated budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
        categoryBudgetService.updateCategoryBudgetsInTransaction(transaction);

    }

    public void reverseBudgetUpdate(Transaction transaction) {
        Log.d("BudgetService", "Reversing budget update for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping reverse budget update");
            return;
        }
        List<AccountBudget> budgets = getListUserBudgets();
        for (AccountBudget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                if (transaction.getType().equals("INCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() - transaction.getAmount());
                } else if (transaction.getType().equals("OUTCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() + transaction.getAmount());
                }
                Log.d("BudgetService", "Reversed budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
        categoryBudgetService.reverseCategoryBudgetUpdate(transaction);

    }

    public void addAccountToBudget(String budgetId, String accountId) {
        Log.d("BudgetService", "Adding account: accountId=" + accountId + " to budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("BudgetService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }

        AccountBudget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getListAccountIds();
        if (accountIds.contains(accountId)) {
            Log.w("BudgetService", "Account already applied to budget: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account already applied to budget");
            return;
        }

        budget.addAccount(accountId);
        Account account = accounts.get(accountId);
        List<String> budgetIds = account.getBudgetIds();
        if (budgetIds == null) {
            budgetIds = new ArrayList<>();
            account.setBudgetIds(budgetIds);
        }
        if (!budgetIds.contains(budgetId)) {
            budgetIds.add(budgetId);
        }


        Log.d("BudgetService", "Added account: accountId=" + accountId + " to budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account added to budget successfully");
    }

    public void updateAccountInBudget(String budgetId, String oldAccountId, String newAccountId) {
        Log.d("BudgetService", "Updating account: oldAccountId=" + oldAccountId + " to newAccountId=" + newAccountId + " in budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(oldAccountId) || !accounts.containsKey(newAccountId)) {
            Log.e("BudgetService", "Account not found: oldAccountId=" + oldAccountId + ", newAccountId=" + newAccountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }

        AccountBudget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getListAccountIds();
        if (!accountIds.contains(oldAccountId)) {
            Log.e("BudgetService", "Old account not found in budget: " + oldAccountId);
            DisplayToast.Display(dataFile.getContext(), "Old account not found in budget");
            return;
        }
        if (accountIds.contains(newAccountId)) {
            Log.w("BudgetService", "New account already applied to budget: " + newAccountId);
            DisplayToast.Display(dataFile.getContext(), "New account already applied to budget");
            return;
        }

        accountIds.remove(oldAccountId);
        budget.addAccount(newAccountId);

        Account oldAccount = accounts.get(oldAccountId);
        List<String> oldBudgetIds = oldAccount.getBudgetIds();
        if (oldBudgetIds != null) {
            oldBudgetIds.remove(budgetId);
        }

        Account newAccount = accounts.get(newAccountId);
        List<String> newBudgetIds = newAccount.getBudgetIds();
        if (newBudgetIds == null) {
            newBudgetIds = new ArrayList<>();
            newAccount.setBudgetIds(newBudgetIds);
        }
        if (!newBudgetIds.contains(budgetId)) {
            newBudgetIds.add(budgetId);
        }


        Log.d("BudgetService", "Updated account: oldAccountId=" + oldAccountId + " to newAccountId=" + newAccountId + " in budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account updated in budget successfully");
    }

    public void deleteAccountFromBudget(String budgetId, String accountId) {
        Log.d("BudgetService", "Deleting account: accountId=" + accountId + " from budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, AccountBudget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("BudgetService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }

        AccountBudget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getListAccountIds();
        if (!accountIds.contains(accountId)) {
            Log.e("BudgetService", "Account not found in budget: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found in budget");
            return;
        }

        accountIds.remove(accountId);
        Account account = accounts.get(accountId);
        List<String> budgetIds = account.getBudgetIds();
        if (budgetIds != null) {
            budgetIds.remove(budgetId);
        }


        Log.d("BudgetService", "Deleted account: accountId=" + accountId + " from budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account removed from budget successfully");
    }
}