package com.example.vcampusexpenses.model;

public class Setting {
    private String displayName;
    private String currency;
    private boolean notification;

    public Setting(String displayName, String currency, boolean notification) {
        this.displayName = displayName;
        this.currency = currency;
        this.notification = notification;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean getNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }
}