package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.FireBaseAuthen;
import com.example.vcampusexpenses.authentication.GuestAuthen;
import com.example.vcampusexpenses.session.SessionManager;

public class SettingActivity extends AppCompatActivity {
    Button btn_logout;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sessionManager = new SessionManager(this);
        btn_logout = findViewById(R.id.btn_logout);

        LogOut();
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
