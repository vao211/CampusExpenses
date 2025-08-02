package com.example.vcampusexpenses.datamanager;
import android.content.Context;
import android.util.Log;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class SyncDataManager {
    private String TAG = "SyncDataManager";
    private String API_URL;
    private final OkHttpClient client;
    private final Context context;
    private final UserDataManager userDataManager;
    private final SessionManager sessionManager;
    String userId;

    public SyncDataManager(Context context, UserDataManager userDataManager) {
        this.sessionManager = new SessionManager(context);
        userId = sessionManager.getUserId();
        this.context = context;
        this.userDataManager = UserDataManager.getInstance(context, userId);
        this.client = new OkHttpClient();
        API_URL = context.getString(R.string.api_url);
    }



    public void fetchData(String userId, final ApiCallBack callBack) {
        String URL = API_URL + "?userId=" + userId;
        Request request = new Request.Builder().url(URL).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Get onFailure: " + e.getMessage());
                if (context != null) {
                    Log.e(TAG, "Get onFailure when fetching data: " + e.getMessage());
                }
                if (callBack != null) {
                    callBack.onFailure("Connection error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Get onResponse success: " + responseBody);
                    //nếu phản hồi là "File not found"
                    if (responseBody.contains("File not found")) {
                        Log.d("SyncDataManager", "API indicates file not found for userId: " + userId + ". No local overwrite.");
                        if (context != null) {
                            Log.e(TAG, "Data file not found on server. No local overwrite");
                        }
                        if (callBack != null) {
                            callBack.onSuccess(responseBody); // Gọi onSuccess của ApiDataCallback, không ovverride
                            //thông báo thành công dù liệu không tồn tại
                        }
                        return;
                    }

                    try {
                        Gson gson = new Gson();
                        UserData apiUserData = gson.fromJson(responseBody, UserData.class);
                        if (apiUserData != null && apiUserData.getUser() != null && apiUserData.getUser().getUserId().equals(userId)) {
                            userDataManager.setUserDataObject(apiUserData);
                        }
                        if (context != null) {
                            Log.d(TAG, "Data fetched successfully");
                        }
                        if (callBack != null) {
                            callBack.onSuccess(responseBody);
                        } else {
                            Log.e("SyncDataManager", "Invalid UserData format or mismatched userId from API.");
                            if (context != null) {
                                Log.e(TAG, "Invalid UserData format or mismatched userId from API.");
                            }
                            if (callBack != null) {
                                callBack.onFailure("Invalid UserData format or mismatched userId from API");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Get onResponse error: " + e.getMessage());
                        if (context != null) {
                            Log.e(TAG, "Get onResponse error: " + e.getMessage());
                        }
                        if (callBack != null) {
                            callBack.onFailure("Get onResponse error: " + e.getMessage());
                        }
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("SyncDataManager", "GET request failed. Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                    if (context != null) {
                        Log.e(TAG, "Error when fetching data from server: " + response.code() + " " + response.message());
                    }
                    if (callBack != null) {
                        callBack.onFailure("Error when fetching data from server: " + response.code() + " " + response.message());
                    }
                }
            }
        });
    }

    public void putData(String userId, final ApiCallBack callBack) {
        String URL = API_URL + "?userId=" + userId;
        userDataManager.loadData(); //Tải data mới nhất
        UserData putUserData = userDataManager.getUserDataObject();

        if (putUserData == null) {
            Log.w("SyncDataManager", "No local user data to put to API.");
            return;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonBody = gson.toJson(putUserData);

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(URL)
                .put(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("SyncDataManager", "PUT request failed: " + e.getMessage(), e);
                if (context != null) {
                    Log.d(TAG, "Error when putting data to server: " + e.getMessage());
                }
                if (callBack != null) {
                    callBack.onFailure("Connection error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("SyncDataManager", "PUT success. Response: " + responseBody);
                    if (context != null) {
                        Log.d(TAG, "PUT success. Response: " + responseBody);
                    }
                    if (callBack != null) {
                        callBack.onSuccess(responseBody);
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e("SyncDataManager", "PUT request failed. Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                    if (context != null) {
                        Log.d(TAG, "PUT request failed. Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                    }
                    if (callBack != null) {
                        callBack.onFailure("Server error when putting data: " + response.code() + " " + response.message());
                    }
                }
            }
        });
    }
}
