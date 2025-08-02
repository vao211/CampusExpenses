package com.example.vcampusexpenses.datamanager;

public interface ApiCallBack {
    void onSuccess(String responseData);
    void onFailure(String errorMessage);
}
