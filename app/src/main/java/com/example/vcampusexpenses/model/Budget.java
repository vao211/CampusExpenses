package com.example.vcampusexpenses.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Budget {
    private String budgetId;
    private String name;
    private double totalAmount;
    private double remainingAmount;
    private String startDate;
    private String endDate;
    private List<String> accountIds = new ArrayList<>();
    private Map<String, Double> categoryLimits = new HashMap<>(); // categoryId -> limit

    // Constructor for getUserBudgets
    public Budget(String budgetId, String name, double totalAmount, double remainingAmount, String startDate, String endDate) {
        this.budgetId = budgetId;
        this.name = name;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Default constructor for creating new Budget instances
    public Budget() {
    }

    // Check if a transaction applies to this Budget
    public boolean appliesToTransaction(Transaction transaction) {
        if (transaction.isTransfer()) return false;

        // Kiểm tra thời gian ngân sách
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date transactionDate = sdf.parse(transaction.getDate());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            assert transactionDate != null;
            if (transactionDate.before(start) || transactionDate.after(end)) {
                return false;
            }
        } catch (ParseException e) {
            return false; //Nếu ngày không hợp lệ, bỏ qua ngân sách
        }

        return accountIds.contains(transaction.getAccountId()) &&
                categoryLimits.containsKey(transaction.getCategoryId());
    }

    // Update remainingAmount based on transaction
    public void updateRemaining(Transaction transaction) {
        if (transaction.getType().equals("INCOME")) {
            remainingAmount += transaction.getAmount(); // Income increases remaining
        } else if (transaction.getType().equals("OUTCOME")) {
            remainingAmount -= transaction.getAmount(); // Outcome decreases remaining
        }
    }

    // Add a single account ID to accountIds
    public void addAccount(String accountId) {
        if (!accountIds.contains(accountId)) {
            accountIds.add(accountId);
        }
    }

    // Add a category limit
    public void addCategoryLimit(String categoryId, double limit) {
        categoryLimits.put(categoryId, limit);
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public Map<String, Double> getCategoryLimits() {
        return categoryLimits;
    }

    public void setCategoryLimits(Map<String, Double> categoryLimits) {
        this.categoryLimits = categoryLimits != null ? new HashMap<>(categoryLimits) : new HashMap<>();
    }

    public List<String> getAccountIds() {
        return accountIds;
    }

    public void setAccountIds(List<String> accountIds) {
        this.accountIds = accountIds != null ? new ArrayList<>(accountIds) : new ArrayList<>();
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}