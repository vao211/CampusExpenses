package com.example.vcampusexpenses.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AccountBudget {
    private String budgetId;
    private String name;
    private double totalAmount;
    private double remainingAmount;
    private String startDate;
    private String endDate;
    private List<String> listAccountIds = new ArrayList<>();

    // Constructor for getListUserBudgets
    public AccountBudget(String budgetId, String name, double totalAmount, double remainingAmount, String startDate, String endDate) {
        this.budgetId = budgetId;
        this.name = name;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.listAccountIds = new ArrayList<>();
    }

    public AccountBudget(String name, double totalAmount, double remainingAmount, String startDate, String endDate) {
        this.name = name;
        this.totalAmount = totalAmount;
        this.remainingAmount = remainingAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.listAccountIds = new ArrayList<>();
    }

    // Check if transaction belongs to AccountBudget
    public boolean appliesToTransaction(Transaction transaction) {
        if (transaction.isTransfer()) return false;

        // Check budget time period
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
            return false; // If date is invalid, skip budget
        }

        return listAccountIds.contains(transaction.getAccountId());
    }

    public void updateRemaining(Transaction transaction) {
        if (transaction.getType().equals("INCOME")) {
            remainingAmount += transaction.getAmount();
        } else if (transaction.getType().equals("OUTCOME")) {
            remainingAmount -= transaction.getAmount();
        }
    }

    // Add account to budget
    public void addAccount(String accountId) {
        if (!listAccountIds.contains(accountId)) {
            listAccountIds.add(accountId);
        }
    }

    public String getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public List<String> getListAccountIds() {
        return listAccountIds;
    }

    public void setListAccountIds(List<String> listAccountIds) {
        this.listAccountIds = listAccountIds != null ? new ArrayList<>(listAccountIds) : new ArrayList<>();
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