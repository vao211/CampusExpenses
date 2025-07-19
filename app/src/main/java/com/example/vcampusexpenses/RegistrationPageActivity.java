package com.example.vcampusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vcampusexpenses.database.UserDB;
import com.example.vcampusexpenses.session.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Calendar;

public class RegistrationPageActivity extends AppCompatActivity {
    private EditText edtDisplayName, edtRealName;
    private DatePicker datePicker;
    private CheckBox chkTerms;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private UserDB userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_page);

        // Khởi tạo views
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtRealName = findViewById(R.id.edtRealName);
        datePicker = findViewById(R.id.datePicker);
        chkTerms = findViewById(R.id.chkTerms);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        userDB = new UserDB();

        loadUserData();

        btnSubmit.setOnClickListener(v -> submitUserInfo());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userDB.loadUserData(userId, new UserDB.UserDataCallback() {
                @Override
                public void onSuccess(String displayName, String realName, String dateOfBirth) {
                    if (displayName != null) {
                        edtDisplayName.setText(displayName);
                    }
                    if (realName != null) {
                        edtRealName.setText(realName);
                    }
                    if (dateOfBirth != null && dateOfBirth.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                        try {
                            String[] dateParts = dateOfBirth.split("-");
                            int year = Integer.parseInt(dateParts[0]);
                            int month = Integer.parseInt(dateParts[1]) - 1; //tháng bắt đầu từ 0
                            int day = Integer.parseInt(dateParts[2]);
                            datePicker.updateDate(year, month, day);
                        } catch (Exception e) {
                            DisplayToast.Display(RegistrationPageActivity.this, "Date format error: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    DisplayToast.Display(RegistrationPageActivity.this, errorMessage);
                }
            });
        } else {
            DisplayToast.Display(this, "User not authenticated");
        }
    }

    private void submitUserInfo() {
        String displayName = edtDisplayName.getText().toString().trim();
        String realName = edtRealName.getText().toString().trim();
        boolean termsAccepted = chkTerms.isChecked();

        if (displayName.isEmpty() || realName.isEmpty()) {
            DisplayToast.Display(this, "Please fill in all fields");
            return;
        }
        if (!termsAccepted) {
            DisplayToast.Display(this, "Please accept the terms and conditions");
            return;
        }

        Calendar today = Calendar.getInstance();
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        if (selectedDate.after(today)) {
            DisplayToast.Display(this, "Please select a valid date of birth");
            return;
        }

        //lưu dữ liệu vào Firebase Firestore qua UserDB
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String dateOfBirth = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
            userDB.updateUserData(userId, displayName, realName, dateOfBirth, new UserDB.UserDataCallback() {
                @Override
                public void onSuccess(String displayName, String realName, String dateOfBirth) {
                    sessionManager.setRegistrationCompleted();
                    DisplayToast.Display(RegistrationPageActivity.this, "Registration successful");
                    Intent intent = new Intent(RegistrationPageActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onError(String errorMessage) {
                    DisplayToast.Display(RegistrationPageActivity.this, errorMessage);
                }
            });
        } else {
            DisplayToast.Display(this, "User not authenticated");
        }
    }
}