package com.example.vcampusexpenses.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.FireBaseAuthen;
import com.example.vcampusexpenses.authentication.GuestAuthen;
import com.example.vcampusexpenses.services.SettingService;
import com.example.vcampusexpenses.session.SessionManager;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    Button btn_login;
    ImageButton btn_logout, btn_back;
    TextView txt_displayName, txt_type_currency, txt_description_notif, txt_description_sync, txt_email;
    SessionManager sessionManager;
    SettingService settingService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sessionManager = new SessionManager(this);
        btn_logout = findViewById(R.id.btn_logout);
        btn_login = findViewById(R.id.btn_login);
        btn_back = findViewById(R.id.btn_back);
        txt_displayName = findViewById(R.id.txt_displayName);
        txt_type_currency = findViewById(R.id.txt_type_currency);
        txt_description_notif = findViewById(R.id.txt_description_notif);
        txt_description_sync = findViewById(R.id.txt_description_sync);
        txt_email = findViewById(R.id.txt_email);
        settingService = new SettingService(this);

        loadLoginState();
        loadSetting();
        goToLogin();
        backToMain();
        LogOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLoginState();
        loadSetting();
    }
    protected void goToLogin(){
        btn_login.setOnClickListener(v -> {
            GuestAuthen.LogOut(this);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            //finish();
        });
    }
    private void backToMain(){
        btn_back.setOnClickListener(v -> {
            finish();
        });
    }
    private void loadLoginState() {
        String userId = sessionManager.getUserId();
        if (!userId.equals("Guest")) {
            btn_login.setVisibility(View.GONE);
            txt_email.setVisibility(View.VISIBLE);
            txt_email.setText(sessionManager.getSavedEmail());
        } else {
            btn_login.setVisibility(View.VISIBLE);
            txt_email.setVisibility(View.GONE);
        }
    }
    private void loadSetting(){
        String displayName = settingService.getDisplayName();
        String currency = settingService.getCurrency();
        boolean notification = settingService.getNotification();
        txt_displayName.setText(displayName);
        txt_type_currency.setText(currency);
        if(notification){
            txt_description_notif.setText("On");
        }else{
            txt_description_notif .setText("Off");
        }
    }

    private void LogOut(){
        btn_logout.setOnClickListener(v ->{
            String userId = sessionManager.getUserId();
            if(userId != "Guest"){
                GuestAuthen.LogOut(this);
                finish();
            }else{
                FireBaseAuthen.LogOut(this);
                finish();
            }
        });
    }
}
