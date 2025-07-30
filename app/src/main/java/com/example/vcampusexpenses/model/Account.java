package com.example.vcampusexpenses.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountId;
    private String userId;
    private String name;
    private String accountName;
    private double balance;
    private String type;
    private List<String> budgets;

    public Account() {
        // Default constructor for Firestore
    }

    public Account(String userId, String accountName, double balance, String type) {
        this.userId = userId;
        this.accountName = accountName;
        this.balance = balance;
        this.type = type;
    }

    public Account(String accountId, String name, double balance) {
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
        this.budgets = new ArrayList<>();
    }
    public Account(String name, double balance) {
        this.name = name;
        this.balance = balance;
        this.budgets = new ArrayList<>();
    }
    public Account(String accountId, String name, double balance, List<String> budgets) {
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
        this.budgets = budgets != null ? new ArrayList<>(budgets) : new ArrayList<>();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getBudgetIds() {
        return budgets;
    }

    public void setBudgetIds(List<String> budgets) {
        this.budgets = budgets != null ? new ArrayList<>(budgets) : new ArrayList<>();
    }

    public void updateBalance(double amount) {
        this.balance += amount;
    }
}