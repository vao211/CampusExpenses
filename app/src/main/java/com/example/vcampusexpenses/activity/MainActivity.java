package com.example.vcampusexpenses.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.fragments.AnalysisFragment;
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

//    private void Logout(){
//        btnLogout.setOnClickListener(v -> {
//            FireBaseAuthen.LogOut(this);
//            finish();
//        });
//    }

    // Hàm Test add, KHÔNG ĐƯỢC CHẠY. Chỉ xem để biết cách dùng.
    private void test() {
        String userId = sessionManager.getUserId();

            AccountService accountService = new AccountService(this, userId);
            CategoryService categoryService = new CategoryService(this, userId);
            BudgetService budgetService = new BudgetService(this, userId);
            TransactionService transactionService = new TransactionService(this, userId);

            Account account1 = new Account("Test1", 1000);
            accountService.addAccount(account1);
            Account account2 = new Account("Test2", 1000);
            accountService.addAccount(account2);

            Category category1 = new Category("Test1");
            categoryService.addCategory(category1);
            String categoryUpdate = "testID";
            String categoryUpdateName = "testName";
            categoryService.updateCategory(categoryUpdate, categoryUpdateName);
            String deleteCategoryID = "testDelID";
            categoryService.deleteCategory(deleteCategoryID);


            Budget budget = new Budget("Test budget", 1000, 1000, "2023-01-01", "2023-12-31");
            budget.addCategoryLimit(categoryService.getCategoryId("Test1"), 500);
            budget.addAccount(accountService.getAccountId("Test2"));
            budgetService.addBudget(budget);
            if(budgetService.getBudgetId("Test budget") == null){
                DisplayToast.Display(this, "Fail to save Budget");
            }
//
            Transaction transaction1 = new Transaction("OUTCOME", accountService.getAccountId("Test1"),
                    categoryService.getCategoryId("Test1"), 500, "2023-01-01", "Test Income");
            transactionService.addTransaction(transaction1);

            Transaction transaction2 = new Transaction("INCOME", accountService.getAccountId("Test2"),
                    categoryService.getCategoryId("Test1"), 500, "2023-01-01", "Test Outcome");
            transactionService.addTransaction(transaction2);

            Transaction transaction3 = new Transaction(accountService.getAccountId("Test1"),
                    accountService.getAccountId("Test2"), 500, "2023-01-01", "Test Transfer");
            transactionService.addTransaction(transaction3);

            // Hiển thị thông báo hoàn tất
            DisplayToast.Display(this, "Test completed. Check Logcat and JSON file.");




    }
}
