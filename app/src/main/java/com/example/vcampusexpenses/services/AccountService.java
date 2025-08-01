package com.example.vcampusexpenses.services;

import android.content.Context;
import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
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

public class AccountService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public AccountService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        Log.d("AccountService", "Initialized with userId: " + userId);
    }

    public void updateBalance(String accountId, double amount) {
        Log.d("AccountService", "Attempting to update balance for accountId: " + accountId + " with amount: " + amount);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized in updateBalance.");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("AccountService", "Account not found in updateBalance for accountId: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found for balance update");
            return;
        }

        Account account = accounts.get(accountId);
        double oldBalance = account.getBalance();
        Log.d("AccountService", "Old balance for " + account.getName() + ": " + oldBalance);
        account.setBalance(oldBalance + amount);
        Log.d("AccountService", "New balance for " + account.getName() + ": " + account.getBalance());
    }

    public String getAccountId(String accountName) {
        Log.d("AccountService", "Getting accountId for accountName: " + accountName);
        if (accountName == null || accountName.trim().isEmpty()) {
            Log.e("AccountService", "Invalid account name");
            DisplayToast.Display(dataFile.getContext(), "Invalid account name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject accounts = jsonData.getAsJsonObject("accounts");
        if (accounts == null) {
            Log.e("AccountService", "No accounts found");
            DisplayToast.Display(dataFile.getContext(), "No accounts found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : accounts.entrySet()) {
            JsonObject accountJson = entry.getValue().getAsJsonObject();
            if (accountJson.has("name") && accountJson.get("name").getAsString().equals(accountName)) {
                String accountId = accountJson.get("accountId").getAsString();
                Log.d("AccountService", "Found accountId: " + accountId + " for accountName: " + accountName);
                return accountId;
            }
        }
        Log.w("AccountService", "Account not found: " + accountName);
        DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountName);
        return null;
    }

    protected void saveAccount(Account account) {
        Log.d("AccountService", "Saving account: " + account.getName());
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null) {
            accounts = new HashMap<>();
            userData.getUser().getData().setAccount(accounts);
        }
        accounts.put(account.getAccountId(), account);
        Log.d("AccountService", "Account saved: " + account.getName());
    }

    public Account getAccount(String accountId) {
        Log.d("AccountService", "Getting account for accountId: " + accountId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.w("AccountService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return null;
        }
        return accounts.get(accountId);
    }

    public boolean addAccount(Account account) {
        Log.d("AccountService", "Adding account: " + account.getName());
        if (account == null || account.getName() == null || account.getName().trim().isEmpty()) {
            Log.e("AccountService", "Invalid account name");
            DisplayToast.Display(dataFile.getContext(), "Invalid account name");
            return false;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return false;
        }

        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null) {
            accounts = new HashMap<>();
            userData.getUser().getData().setAccount(accounts);
        }

        // Check for duplicate account names (case-insensitive)
        for (Account existingAccount : accounts.values()) {
            if (existingAccount.getName().equalsIgnoreCase(account.getName())) {
                Log.w("AccountService", "Account name already exists: " + account.getName());
                DisplayToast.Display(dataFile.getContext(), "Account name already exists");
                return false;
            }
        }

        String accountId = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
        account.setAccountId(accountId);
        saveAccount(account);
        Log.d("AccountService", "Account added: " + account.getName());
        return true;
    }

    public boolean isAccountNameExists(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) {
            return false;
        }
        List<Account> accounts = getListAccounts();
        if (accounts != null) {
            String trimmedNewName = accountName.trim();
            for (Account account : accounts) {
                if (account.getName().equalsIgnoreCase(trimmedNewName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateAccount(String accountId, String newName, double balance) {
        Log.d("AccountService", "Updating accountId: " + accountId + " with newName: " + newName + ", balance: " + balance);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("AccountService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }
        if (newName == null || newName.trim().isEmpty()) {
            Log.e("AccountService", "Invalid account name");
            DisplayToast.Display(dataFile.getContext(), "Invalid account name");
            return;
        }
        for (Account existingAccount : accounts.values()) {
            if (existingAccount.getName().equals(newName) && !existingAccount.getAccountId().equals(accountId)) {
                Log.w("AccountService", "Account name already exists: " + newName);
                DisplayToast.Display(dataFile.getContext(), "Account name already exists");
                return;
            }
        }
        Account account = accounts.get(accountId);
        account.setName(newName);
        account.setBalance(balance);
        Log.d("AccountService", "Account updated: " + account.getName() + ", balance: " + account.getBalance());

        DisplayToast.Display(dataFile.getContext(), "Account updated successfully");
    }

    public void deleteAccount(String accountId) {
        Log.d("AccountService", "Deleting accountId: " + accountId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("AccountService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }
        if (transactions != null) {
            for (Transaction transaction : transactions.values()) {
                if (!transaction.isTransfer() && transaction.getAccountId().equals(accountId)) {
                    Log.w("AccountService", "Cannot delete account because it is used in transaction");
                    DisplayToast.Display(dataFile.getContext(), "Cannot delete account because it is used in transaction");
                    return;
                }
            }
        }
        if (budgets != null) {
            for (Budget budget : budgets.values()) {
                if (budget.getAccountIds() != null && budget.getAccountIds().contains(accountId)) {
                    Log.w("AccountService", "Cannot delete account because it is used in budget");
                    DisplayToast.Display(dataFile.getContext(), "Cannot delete account because it is used in budget");
                    return;
                }
            }
        }
        accounts.remove(accountId);
        Log.d("AccountService", "Account deleted: " + accountId);
        DisplayToast.Display(dataFile.getContext(), "Account deleted successfully");
    }

    public List<Account> getListAccounts() {
        Log.d("AccountService", "Getting list of accounts");
        List<Account> accountList = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("AccountService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return accountList;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts != null) {
            accountList.addAll(accounts.values());
            Log.d("AccountService", "Found " + accountList.size() + " accounts");
        } else {
            Log.w("AccountService", "No accounts found");
        }
        return accountList;
    }
}