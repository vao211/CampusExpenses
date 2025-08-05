package com.example.vcampusexpenses.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.fragments.AccountBudgetFragment;
import com.example.vcampusexpenses.fragments.CategoriesFragment;
import com.example.vcampusexpenses.fragments.HomeFragment;
import com.example.vcampusexpenses.fragments.TransactionFragment;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private AccountBudgetService accountBudgetService;
    private AccountService accountService;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final String CHANNEL_ID = "BudgetNotificationChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Yêu cầu quyền thông báo cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Tạo kênh thông báo
        createNotificationChannel();

        sessionManager = new SessionManager(this);
        UserDataManager userDataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountBudgetService = new AccountBudgetService(userDataManager);
        accountService = new AccountService(userDataManager);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Fragment mặc định
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_budget) {
                selectedFragment = new AccountBudgetFragment();
            } else if (itemId == R.id.nav_transaction) {
                selectedFragment = new TransactionFragment();
            } else if (itemId == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            // Kiểm tra và hiển thị thông báo khi chuyển fragment
            checkBudgetsForZeroRemaining();
            return true;
        });

        // Kiểm tra ngân sách khi khởi động ứng dụng
        checkBudgetsForZeroRemaining();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Alerts";
            String description = "Notifications for budget alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String budgetName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif) // Thay bằng icon của bạn
                .setContentTitle("Budget Exhausted")
                .setContentText("Budget '" + budgetName + "' has no remaining amount!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(budgetName.hashCode(), builder.build());
        }
    }
    private void checkBudgetsForZeroRemaining() {
        List<AccountBudget> accountBudgetList = accountBudgetService.getListUserBudgets();
        SharedPreferences prefs = getSharedPreferences("BudgetNotifications", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (accountBudgetList != null) {
            for (AccountBudget budget : accountBudgetList) {
                if (budget.getRemainingAmount() == 0) {
                    String budgetId = budget.getBudgetId();
                    // Kiểm tra xem thông báo đã được gửi chưa
                    if (!prefs.getBoolean("notified_" + budgetId, false)) {
                        Toast.makeText(this, "Budget '" + budget.getName() + "' has no remaining amount!", Toast.LENGTH_LONG).show();
                        Account account = accountService.getAccount(budget.getListAccountIds().get(0));
                        sendNotification(account.getName());
                        editor.putBoolean("notified_" + budgetId, true);
                        editor.apply();
                    }
                } else {
                    // Xóa trạng thái thông báo nếu remainingAmount không còn là 0
                    editor.remove("notified_" + budget.getBudgetId());
                    editor.apply();
                }
            }
        }
    }
    // Hàm Test add, KHÔNG ĐƯỢC CHẠY. Chỉ xem để biết cách dùng.
    private void test() {
        String userId = sessionManager.getUserId();
    }
}