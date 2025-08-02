package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;

import java.util.List;

public class BudgetActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private Budget budget;
    private ImageButton btnClose, btnSubmit;
    private LinearLayout llDatePicker, llSelectCategory, llSelectAccount, llSelectToAccount;
    private TextView txtStartDate,txtEndDate, txtSelectedCategory, txtSelectedAccount, txtSelectedToAccount, txtSelectToAccount, txtTitle;
    private EditText edtAmount;
    private boolean categorySelected, accountSelected, toAccountSelected;
    String budgetId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        btnClose = findViewById(R.id.btn_close);
        btnSubmit = findViewById(R.id.btn_submit);
        llDatePicker = findViewById(R.id.ll_datePicker);
        llSelectCategory = findViewById(R.id.ll_select_category);
        llSelectAccount = findViewById(R.id.ll_select_account);
        llSelectToAccount = findViewById(R.id.ll_select_to_account);
        txtTitle = findViewById(R.id.txt_title);
        txtStartDate = findViewById(R.id.txt_start_date);
        txtEndDate = findViewById(R.id.txt_end_date);
        txtSelectedCategory = findViewById(R.id.txt_selected_category);
        txtSelectedAccount = findViewById(R.id.txt_selected_account);
        //Text bên trong khung chọn acccount tới cho transfer
        txtSelectedToAccount = findViewById(R.id.txt_selected_to_account);
        //Text bên ngoài khung chọn acccount tới cho transfer
        txtSelectToAccount = findViewById(R.id.txt_select_to_account);
        edtAmount = findViewById(R.id.edt_amount);

        categorySelected = false;
        accountSelected = false;
        toAccountSelected = false;

        //service + UserDataManager
        sessionManager = new SessionManager(this);
        dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountService = new AccountService(dataManager);
        BudgetService budgetService = new BudgetService(dataManager);
        categoryService = new CategoryService(dataManager);

    }

    private void loadBudget(String budgetId) {
        Budget budget = budgetService.getBudget(budgetId);
        String budgetName = budget.getName();
        String startDate = budget.getStartDate();
        String endDate = budget.getEndDate();
        double amount = budget.getTotalAmount();
        String budgetStartDate = budget.getStartDate();
        String budgetEndDate = budget.getEndDate();

        List<String> budgetListAccounts = budget.getListAccountIds();

    }
}
