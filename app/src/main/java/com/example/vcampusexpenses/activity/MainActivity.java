package com.example.vcampusexpenses.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.authentication.FireBaseAuthen;
import com.example.vcampusexpenses.datamanager.JsonDataManager;
import com.example.vcampusexpenses.fragments.AnalysisFragment;
import com.example.vcampusexpenses.fragments.CategoriesFragment;
import com.example.vcampusexpenses.fragments.HomeFragment;
import com.example.vcampusexpenses.fragments.TransactionFragment;
import com.example.vcampusexpenses.methods.AccountMethod;
import com.example.vcampusexpenses.methods.BudgetMethod;
import com.example.vcampusexpenses.methods.CategoryMethod;
import com.example.vcampusexpenses.methods.TransactionMethod;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    private Button btnLogout, btnTest;
    private TextView txtUserEmail2;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        txtUserEmail2 = findViewById(R.id.txtUserEmail2);
        btnLogout = findViewById(R.id.btnLogout);
        sessionManager = new SessionManager(this);

        btnTest = findViewById(R.id.btnTest);
        test();
        LoadUserEmail();
        Logout();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Fragment mặc định
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
            } else if (itemId == R.id.nav_analysis) {
                selectedFragment = new AnalysisFragment();
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
            return true;
        });
    }

    @SuppressLint("SetTextI18n")
    private void LoadUserEmail(){
        String email = sessionManager.getSavedEmail();
        if (email != null) {
            txtUserEmail2.setText("UserEmail: "+ email);
        }
    }
    private void Logout(){
        btnLogout.setOnClickListener(v -> {
            FireBaseAuthen.LogOut(this);
            finish();
        });
    }

    // Hàm Test add, KHÔNG ĐƯỢC CHẠY. Chỉ xem để biết cách dùng.
    private void test() {
        String userId = sessionManager.getUserId();
        btnTest.setOnClickListener(v -> {
            if (userId == null || userId.trim().isEmpty()) {
                DisplayToast.Display(this, "User ID is invalid. Please log in again.");
                return;
            }

            AccountMethod accountMethod = new AccountMethod(this, userId);
            CategoryMethod categoryMethod = new CategoryMethod(this, userId);
            BudgetMethod budgetMethod = new BudgetMethod(this, userId);
            TransactionMethod transactionMethod = new TransactionMethod(this, userId);

            Account account1 = new Account("Test1", 1000);
            accountMethod.addAccount(account1);
            Account account2 = new Account("Test2", 1000);
            accountMethod.addAccount(account2);

            Category category1 = new Category("Test1");
            categoryMethod.addCategory(category1);

            Budget budget = new Budget("Test budget", 1000, 1000, "2023-01-01", "2023-12-31");
            budget.addCategoryLimit(categoryMethod.getCategoryId("Test1"), 500);
            budget.addAccount(accountMethod.getAccountId("Test2"));
            budgetMethod.addBudget(budget);
            if(budgetMethod.getBudgetId("Test budget") == null){
                DisplayToast.Display(this, "Fail to save Budget");
            }
//
            Transaction transaction1 = new Transaction("OUTCOME", accountMethod.getAccountId("Test1"),
                    categoryMethod.getCategoryId("Test1"), 500, "2023-01-01", "Test Income");
            transactionMethod.addTransaction(transaction1);

            Transaction transaction2 = new Transaction("INCOME", accountMethod.getAccountId("Test2"),
                    categoryMethod.getCategoryId("Test1"), 500, "2023-01-01", "Test Outcome");
            transactionMethod.addTransaction(transaction2);

            Transaction transaction3 = new Transaction(accountMethod.getAccountId("Test1"),
                    accountMethod.getAccountId("Test2"), 500, "2023-01-01", "Test Transfer");
            transactionMethod.addTransaction(transaction3);

            // Hiển thị thông báo hoàn tất
            DisplayToast.Display(this, "Test completed. Check Logcat and JSON file.");
        });
    }
}
