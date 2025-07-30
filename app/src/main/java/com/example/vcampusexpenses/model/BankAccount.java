package com.example.vcampusexpenses.model;

public class BankAccount {
    private String bankAccountId; // For Firestore document ID
    private String userId;
    private String bankName;
    private String accountHolderName; // Optional, but good to have
    private String accountNumberLast4; // Similar to card, for display
    private double balance;

    public BankAccount() {
        // Default constructor for Firestore
    }

    public BankAccount(String userId, String bankName, String accountHolderName, String accountNumberLast4, double balance) {
        this.userId = userId;
        this.bankName = bankName;
        this.accountHolderName = accountHolderName;
        this.accountNumberLast4 = accountNumberLast4;
        this.balance = balance;
    }

    // Getters and Setters
    public String getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountNumberLast4() {
        return accountNumberLast4;
    }

    public void setAccountNumberLast4(String accountNumberLast4) {
        this.accountNumberLast4 = accountNumberLast4;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
