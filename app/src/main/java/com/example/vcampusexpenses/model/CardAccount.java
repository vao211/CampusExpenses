package com.example.vcampusexpenses.model;

public class CardAccount {
    private String cardId; // For Firestore document ID
    private String userId;
    private String cardName; // e.g., "Visa Gold"
    private String last4Digits; // Store only last 4 digits for display
    private double currentBalance; // How much is currently spent/owed
    private double creditLimit;
    // Available balance can be calculated: creditLimit - currentBalance

    public CardAccount() {
        // Default constructor for Firestore
    }

    public CardAccount(String userId, String cardName, String last4Digits, double currentBalance, double creditLimit) {
        this.userId = userId;
        this.cardName = cardName;
        this.last4Digits = last4Digits;
        this.currentBalance = currentBalance;
        this.creditLimit = creditLimit;
    }

    public double getAvailableBalance() {
        return creditLimit - currentBalance;
    }

    // Getters and Setters
    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public void setLast4Digits(String last4Digits) {
        this.last4Digits = last4Digits;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }
}
