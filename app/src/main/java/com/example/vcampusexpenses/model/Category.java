package com.example.vcampusexpenses.model;

import java.util.HashMap;
import java.util.Map;

public class Category{
    private String categoryId;
    private String name;
    private Map<String, CategoryBudget> accountInCategoryBudgets; //accountID, CategoryBudget

    public Category(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
        this.accountInCategoryBudgets = new HashMap<>();
    }

    public Category(String name) {
        this.name = name;
        this.accountInCategoryBudgets = new HashMap<>();
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

    public Map<String, CategoryBudget> getAccountInCategoryBudgets() {
        if (accountInCategoryBudgets == null) {
            accountInCategoryBudgets = new HashMap<>();
        }
        return accountInCategoryBudgets;
    }

    public void setAccountInCategoryBudgets(Map<String, CategoryBudget> accountInCategoryBudgets) {
        this.accountInCategoryBudgets = accountInCategoryBudgets != null ? new HashMap<>(accountInCategoryBudgets) : new HashMap<>();
    }

    public void setBudgetForAccount(String accountId, double amount) {
        if (amount <= 0) {
            accountInCategoryBudgets.remove(accountId);
        } else {
            accountInCategoryBudgets.put(accountId, new CategoryBudget(categoryId, accountId, amount));
        }
    }

    public CategoryBudget getBudgetForAccount(String accountId) {
        return accountInCategoryBudgets.get(accountId);
    }

    public void removeBudgetForAccount(String accountId) {
        accountInCategoryBudgets.remove(accountId);
    }
}