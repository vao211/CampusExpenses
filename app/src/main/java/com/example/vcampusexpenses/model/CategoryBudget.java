package com.example.vcampusexpenses.model;

import java.io.Serializable;

public class CategoryBudget implements Serializable {
    private String categoryId;
    private String accountId;
    private double totalAmount;
    private double remainingAmount;

    public CategoryBudget(String categoryId, String accountId, double totalAmount) {
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.totalAmount = totalAmount;
        this.remainingAmount = totalAmount;
    }

    public CategoryBudget(String categoryId, String accountId, double totalAmount, double remainingAmount) {
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void updateRemaining(Transaction transaction) {
        if (transaction.getType().equals("INCOME")) {
            remainingAmount += transaction.getAmount();
        } else if (transaction.getType().equals("OUTCOME")) {
            remainingAmount -= transaction.getAmount();
        }
    }

    public void reverseUpdate(Transaction transaction) {
        if (transaction.getType().equals("INCOME")) {
            remainingAmount -= transaction.getAmount();
        } else if (transaction.getType().equals("OUTCOME")) {
            remainingAmount += transaction.getAmount();
        }
    }
}