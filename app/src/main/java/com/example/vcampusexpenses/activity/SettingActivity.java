package com.example.vcampusexpenses.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.FireBaseAuthen;
import com.example.vcampusexpenses.authentication.GuestAuthen;
import com.example.vcampusexpenses.services.SettingService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

public class SettingActivity extends AppCompatActivity {
    Button btn_login;
    ImageButton btn_logout, btn_back, btn_notification, btn_sync, btn_currency, btn_export, btn_feedback, btn_about;
    TextView txt_displayName, txt_type_currency, txt_description_notif, txt_description_sync, txt_email;
    SessionManager sessionManager;
    SettingService settingService;
    // Khởi tạo ActivityResultLauncher để yêu cầu quyền trên Android >= 13
    ActivityResultLauncher<String> requestPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sessionManager = new SessionManager(this);
        btn_logout = findViewById(R.id.btn_logout);
        btn_login = findViewById(R.id.btn_login);
        btn_back = findViewById(R.id.btn_back);
        btn_notification = findViewById(R.id.btn_notif);
        btn_sync = findViewById(R.id.btn_sync);
        btn_currency = findViewById(R.id.btn_currency);
        btn_export = findViewById(R.id.btn_export);
        btn_feedback = findViewById(R.id.btn_feedback);
        btn_about = findViewById(R.id.btn_about);
        txt_displayName = findViewById(R.id.txt_displayName);
        txt_type_currency = findViewById(R.id.txt_type_currency);
        txt_description_notif = findViewById(R.id.txt_description_notif);
        txt_description_sync = findViewById(R.id.txt_description_sync);
        txt_email = findViewById(R.id.txt_email);
        settingService = new SettingService(this);

       requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
               isGranted -> {
            if (isGranted) {
                Log.d("Setting activity", "POST_NOTIFICATIONS permission granted");
                settingService.setNotification(true);
                txt_description_notif.setText("On");
            } else {
                Log.d("Setting activity", "POST_NOTIFICATIONS permission denied");
                settingService.setNotification(false);
                txt_description_notif.setText("Off");
            }
        });
        
        loadLoginState();
        loadSetting();
        setNotification();
        setCurrency();
        feedback();
        about();
        feedback();
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
    private void about(){
        btn_about.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
    }
    private void feedback(){
        btn_feedback.setOnClickListener(v -> {
            String url = "https://forms.gle/VWamV1aJP49A3gWP6";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                DisplayToast.Display(this, "No browser found");
            }
        });
    }
    private void setCurrency(){
        btn_currency.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_currency, null);
            builder.setView(dialogView);

            CheckBox chkVND = dialogView.findViewById(R.id.chk_VND);
            CheckBox chkUSD = dialogView.findViewById(R.id.chk_USD);
            CheckBox chkEUR = dialogView.findViewById(R.id.chk_EUR);

            String currentCurrency = settingService.getCurrency();
            switch (currentCurrency) {
                case "VND":
                    chkVND.setChecked(true);
                    break;
                case "USD":
                    chkUSD.setChecked(true);
                    break;
                case "EUR":
                    chkEUR.setChecked(true);
                    break;
            }

            chkVND.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chkUSD.setChecked(false);
                    chkEUR.setChecked(false);
                    settingService.setCurrency("VND");
                    txt_type_currency.setText("VND");
                }
            });
            chkUSD.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chkVND.setChecked(false);
                    chkEUR.setChecked(false);
                    settingService.setCurrency("USD");
                    txt_type_currency.setText("USD");
                }
            });

            chkEUR.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chkVND.setChecked(false);
                    chkUSD.setChecked(false);
                    settingService.setCurrency("EUR");
                    txt_type_currency.setText("EUR");
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                loadSetting();
            });
            builder.create().show();
        });
    }
    private void setNotification(){
        btn_notification.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification, null);
            builder.setView(dialogView);

            CheckBox chkEnableNotif = dialogView.findViewById(R.id.chk_enable_notif);
            CheckBox chkDisableNotif = dialogView.findViewById(R.id.chk_disable_notif);

            boolean currentNotif = settingService.getNotification();
            chkEnableNotif.setChecked(currentNotif);
            chkDisableNotif.setChecked(!currentNotif);

            //only 1 checkbox can be checked
            chkEnableNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chkDisableNotif.setChecked(false);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        // Android 13 trở lên: Yêu cầu quyền POST_NOTIFICATIONS
                        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS");
                            settingService.setNotification(true);
                            txt_description_notif.setText("On");
                        }
                        else{
                            settingService.setNotification(true);
                            txt_description_notif.setText("On");
                        }
                    } else{
                        openNotificationSettings();
                        settingService.setNotification(true);
                        txt_description_notif.setText("On");
                    }
                }
            });

            chkDisableNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chkEnableNotif.setChecked(false);
                    settingService.setNotification(false);
                    txt_description_notif.setText("Off");
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        openNotificationSettings();
                    }
                    else{
                        openNotificationSettings();
                    }
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                loadSetting();
            });
            builder.create().show();
        });
    }
    private void openNotificationSettings(){
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android >= 8.0
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.getPackageName());
        } else {
            //Android 7.0 -> 7.1.1:
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", this.getPackageName(), null));
        }
        startActivity(intent);
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
            if(userId.equals("Guest")){
                GuestAuthen.LogOut(this);
                finish();
            }else{
                FireBaseAuthen.LogOut(this);
                finish();
            }
        });
    }
}
