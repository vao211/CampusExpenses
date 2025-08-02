package com.example.vcampusexpenses.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountId;
    private String name;
    private double balance;
    private List<String> budgets;

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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
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