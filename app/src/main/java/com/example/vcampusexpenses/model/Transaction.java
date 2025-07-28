package com.example.vcampusexpenses.model;
public class Transaction {
    private String transactionId;
    private String type;  // "INCOME", "OUTCOME", "TRANSFER"
    private double amount;
    private String date;
    private String description;

    // Dùng cho INCOME/OUTCOME
    private String accountId;
    private String categoryId;

    //TRANSFER
    private String fromAccountId;
    private String toAccountId;

    //Constructor cho INCOME/OUTCOME
    public Transaction(String type, String accountId, String categoryId,
                       double amount, String date, String description) {
        this.type = type;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    //Constructor cho TRANSFER
    public Transaction(String fromAccountId, String toAccountId,
                       double amount, String date, String description) {
        this.type = "TRANSFER";
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public boolean isValid() {
        if (amount <= 0) return false;
        if (type.equals("TRANSFER")) {
            return fromAccountId != null && toAccountId != null && !fromAccountId.equals(toAccountId);
        } else {
            return accountId != null && categoryId != null && (type.equals("INCOME") || type.equals("OUTCOME"));
        }
    }

    public boolean isTransfer() { return "TRANSFER".equals(type); }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }

    public String getToAccountId() { return toAccountId; }
    public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }
}
