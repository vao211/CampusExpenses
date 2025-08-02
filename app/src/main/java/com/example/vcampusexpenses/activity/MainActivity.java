package com.example.vcampusexpenses.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.AccountRadioAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.fragments.BudgetFragment;
import com.example.vcampusexpenses.fragments.CategoriesFragment;
import com.example.vcampusexpenses.fragments.HomeFragment;
import com.example.vcampusexpenses.fragments.TransactionFragment;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    //test hiển thị user
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        sessionManager = new SessionManager(this);

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
            } else if (itemId == R.id.nav_budget) {
                selectedFragment = new BudgetFragment();
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

    // Hàm Test add, KHÔNG ĐƯỢC CHẠY. Chỉ xem để biết cách dùng.
    private void test() {
        String userId = sessionManager.getUserId();
    }
}
