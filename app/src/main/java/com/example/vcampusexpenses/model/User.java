package com.example.vcampusexpenses.model;

public class User {
    private String userId;
    private Data data;

    public User(String userId, Data data) {
        this.userId = userId;
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
