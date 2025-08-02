package com.example.vcampusexpenses.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Category{
    private String categoryId;
    private String name;
    private Map<String, CategoryBudget> accountBudgets;

    public Category(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
        this.accountBudgets = new HashMap<>();
    }

    public Category(String name) {
        this.name = name;
        this.accountBudgets = new HashMap<>();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, CategoryBudget> getAccountBudgets() {
        if (accountBudgets == null) {
            accountBudgets = new HashMap<>();
        }
        return accountBudgets;
    }

    public void setAccountBudgets(Map<String, CategoryBudget> accountBudgets) {
        this.accountBudgets = accountBudgets != null ? new HashMap<>(accountBudgets) : new HashMap<>();
    }

    public void setBudgetForAccount(String accountId, double amount) {
        if (amount <= 0) {
            accountBudgets.remove(accountId);
        } else {
            accountBudgets.put(accountId, new CategoryBudget(categoryId, accountId, amount));
        }
    }

    public CategoryBudget getBudgetForAccount(String accountId) {
        return accountBudgets.get(accountId);
    }

    public void removeBudgetForAccount(String accountId) {
        accountBudgets.remove(accountId);
    }
}