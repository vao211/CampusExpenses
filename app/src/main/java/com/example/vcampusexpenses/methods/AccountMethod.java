package com.example.vcampusexpenses.methods;

import android.content.Context;

import com.example.vcampusexpenses.datamanager.JsonDataFile;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class AccountMethod {
    private final JsonDataFile dataFile;
    private final String userId;

    public AccountMethod(Context context, String userId) {
        this.dataFile = new JsonDataFile(context);
        this.userId = userId;
    }

    private void saveAccount(Account account) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accountJson = new JsonObject();
        accountJson.addProperty("accountId", account.getAccountId());
        accountJson.addProperty("name", account.getName());
        accountJson.addProperty("balance", account.getBalance());
        JsonArray budgetsArray = new JsonArray();
        for (String budgetId : account.getBudgetIds()) {
            budgetsArray.add(budgetId);
        }
        accountJson.add("budgets", budgetsArray);
        userData.getAsJsonObject("accounts").add(account.getAccountId(), accountJson);
        dataFile.saveData();
    }
    public Account getAccount(String accountId) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accountJson = userData.getAsJsonObject("accounts").getAsJsonObject(accountId);
        if(accountJson == null){
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return null;
        }
        JsonArray budgetsArray = accountJson.getAsJsonArray("budgets");
        List<String> budgets = new ArrayList<>();
        if (budgetsArray != null) {
            for (JsonElement budgetId : budgetsArray) {
                budgets.add(budgetId.getAsString());
            }
        }
        return new Account(
                accountId,
                accountJson.get("name").getAsString(),
                accountJson.get("balance").getAsDouble(),
                budgets
        );
    }
    public void addAccount(Account account){
        if(account == null || account.getName() == null
                || account.getName().trim().isEmpty()){
            DisplayToast.Display(dataFile.getContext(), "InValid Account Name");
            return;
        }
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accounts = userData.getAsJsonObject("accounts");

        //check trùng tên
        for(String key : accounts.keySet()){
            JsonObject existingAccount = accounts.getAsJsonObject(key);
            if(existingAccount.get("name").getAsString().equals(account.getName())){
                DisplayToast.Display(dataFile.getContext(), "Account Name Already Exists");
                return;
            }
        }
        //auto gen Id
        String accountId = IdGenerator.generateId(IdGenerator.ModelType.ACCOUNT);
        account.setAccountId(accountId);
        saveAccount(account);
    }
    public void updateAccount(String accountId, String newName, double balance){
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accounts = userData.getAsJsonObject("accounts");
        if(!accounts.has(accountId)){
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return;
        }
        if(newName == null || newName.trim().isEmpty()){
            DisplayToast.Display(dataFile.getContext(), "InValid Account Name");
            return;
        }
        for(String key : accounts.keySet()){
            JsonObject existingAccount = accounts.getAsJsonObject(key);
            if(existingAccount.get("name").getAsString().equals(newName)
                    && !existingAccount.get("accountId").getAsString().equals(accountId)){
                DisplayToast.Display(dataFile.getContext(), "Account Name Already Exists");
                return;
            }
        }
        JsonObject accountJson = accounts.getAsJsonObject(accountId);
        accountJson.addProperty("name", newName);
        accountJson.addProperty("balance", balance);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Update Account Successfully");
    }
    public void deleteAccount(String accountId) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accounts = userData.getAsJsonObject("accounts");
        JsonObject transactions = userData.getAsJsonObject("transactions");
        JsonObject budgets = userData.getAsJsonObject("budgets");
        if (!accounts.has(accountId)) {
            DisplayToast.Display(dataFile.getContext(), "Account Not Found: " + accountId);
            return;
        }
        for (String transactionId : transactions.keySet()) {
            JsonObject transactionJson = transactions.getAsJsonObject(transactionId);
            if (!transactionJson.get("type").getAsString().equals("TRANSFER") &&
                    transactionJson.get("accountId").getAsString().equals(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Cannot Delete Account Because It Is Used In Transaction");
                return;
            }
        }
        for (String budgetId : budgets.keySet()) {
            JsonObject budgetJson = budgets.getAsJsonObject(budgetId);
            JsonObject categoryLimits = budgetJson.getAsJsonObject("categoryLimits");
            if (categoryLimits != null && categoryLimits.has(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Cannot Delete Account Because It Is Used In Budget");
                return;
            }
        }
        accounts.remove(accountId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Delete Account Successfully");
    }
    public List<Account> getListAccounts(){
        List<Account> accountList = new ArrayList<>();
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accountsJson = userData.getAsJsonObject("accounts");

        for (String accountId : accountsJson.keySet()) {
            JsonObject accountJson = accountsJson.getAsJsonObject(accountId);
            JsonArray budgetsArrayJson = accountJson.getAsJsonArray("budgets");
            List<String> budgets = new ArrayList<>();
            if (budgetsArrayJson != null) {
                for (JsonElement budgetId : budgetsArrayJson) {
                    budgets.add(budgetId.getAsString());
                }
            }
            Account account = new Account(
                    accountId,
                    accountJson.get("name").getAsString(),
                    accountJson.get("balance").getAsDouble(),
                    budgets
            );
            accountList.add(account);
        }
        return accountList;
    }
}
