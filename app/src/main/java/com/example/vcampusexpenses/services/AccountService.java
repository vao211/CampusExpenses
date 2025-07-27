package com.example.vcampusexpenses.services;

import android.content.Context;
import com.example.vcampusexpenses.datamanager.JsonDataManager;
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
    private final JsonDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public AccountService(Context context, String userId) {
        this.dataFile = new JsonDataManager(context, userId);
        this.userData = dataFile.getUserDataObject();
        this.userId = userId;
    }

    public String getAccountId(String accountName) {
        if (accountName == null || accountName.trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "Invalid account name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject accounts = jsonData.getAsJsonObject("accounts");
        if (accounts == null) {
            DisplayToast.Display(dataFile.getContext(), "No accounts found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : accounts.entrySet()) {
            JsonObject accountJson = entry.getValue().getAsJsonObject();
            if (accountJson.has("name") && accountJson.get("name").getAsString().equals(accountName)) {
                return accountJson.get("accountId").getAsString();
            }
        }
        DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountName);
        return null;
    }

    protected void saveAccount(Account account) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null) {
            accounts = new HashMap<>();
            userData.getUser().getData().setAccount(accounts);
        }
        accounts.put(account.getAccountId(), account);
        dataFile.saveData();
    }

    public Account getAccount(String accountId) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null || !accounts.containsKey(accountId)) {
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return null;
        }
        return accounts.get(accountId);
    }

    public void addAccount(Account account) {
        if (account == null || account.getName() == null || account.getName().trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Account Name");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null) {
            accounts = new HashMap<>();
            userData.getUser().getData().setAccount(accounts);
        }

        //check trùng tên
        for (Account existingAccount : accounts.values()) {
            if (existingAccount.getName().equals(account.getName())) {
                DisplayToast.Display(dataFile.getContext(), "Account Name Already Exists");
                return;
            }
        }
        //auto gen Id
        String accountId = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
        account.setAccountId(accountId);
        saveAccount(account);
    }

    public void updateAccount(String accountId, String newName, double balance) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts == null || !accounts.containsKey(accountId)) {
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return;
        }
        if (newName == null || newName.trim().isEmpty()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Account Name");
            return;
        }
        for (Account existingAccount : accounts.values()) {
            if (existingAccount.getName().equals(newName) && !existingAccount.getAccountId().equals(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Account Name Already Exists");
                return;
            }
        }
        Account account = accounts.get(accountId);
        account.setName(newName);
        account.setBalance(balance);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Update Account Successfully");
    }

    public void deleteAccount(String accountId) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        if (accounts == null || !accounts.containsKey(accountId)) {
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return;
        }
        if (transactions != null) {
            for (Transaction transaction : transactions.values()) {
                if (!transaction.isTransfer() && transaction.getAccountId().equals(accountId)) {
                    DisplayToast.Display(dataFile.getContext(), "Cannot Delete Account Because It Is Used In Transaction");
                    return;
                }
            }
        }
        if (budgets != null) {
            for (Budget budget : budgets.values()) {
                if (budget.getAccountIds() != null && budget.getAccountIds().contains(accountId)) {
                    DisplayToast.Display(dataFile.getContext(), "Cannot Delete Account Because It Is Used In Budget");
                    return;
                }
            }
        }
        accounts.remove(accountId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Delete Account Successfully");
    }

    public List<Account> getListAccounts() {
        List<Account> accountList = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return accountList;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        if (accounts != null) {
            accountList.addAll(accounts.values());
        }
        return accountList;
    }
}