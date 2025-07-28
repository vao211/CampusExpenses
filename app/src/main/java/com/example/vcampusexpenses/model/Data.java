package com.example.vcampusexpenses.model;

import java.util.Map;

public class Data {
    private Map<String, Account> accounts;
    private Map<String, Category> categories;
    private Map<String, Budget> budgets;
    private Map<String, Transaction> transactions;

    public Data(Map<String, Account> account, Map<String, Category> categories,
                Map<String, Budget> budgets, Map<String, Transaction> transactions) {
        this.accounts = account;
        this.categories = categories;
        this.budgets = budgets;
        this.transactions = transactions;
    }

    public Map<String, Account> getAccount() {
        return accounts;
    }

    public void setAccount(Map<String, Account> account) {
        this.accounts = account;
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Category> categories) {
        this.categories = categories;
    }

    public Map<String, Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(Map<String, Budget> budgets) {
        this.budgets = budgets;
    }

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }
}
