package com.example.vcampusexpenses.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.CategoryBudgetAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.CategoryBudget;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryBudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class BudgetCategoryActivity extends AppCompatActivity {
    private static final String INVALID_AMOUNT_ERROR = "Amount must be greater than 0";
    private static final String NEGATIVE_REMAINING_ERROR = "Remaining amount cannot be negative";
    private static final String INVALID_NUMBER_ERROR = "Invalid number format";

    private String categoryId;
    private CategoryService categoryService;
    private CategoryBudgetService categoryBudgetService;
    private AccountService accountService;
    private CategoryBudgetAdapter adapter;
    private RecyclerView rvCategoryBudgets;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_category);
        initializeServices();
        setupUI();
    }

    private void initializeServices() {
        categoryId = getIntent().getStringExtra("categoryId");
        sessionManager = new SessionManager(this);
        UserDataManager dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        categoryService = new CategoryService(dataManager);
        categoryBudgetService = new CategoryBudgetService(dataManager);
        accountService = new AccountService(dataManager);
    }

    private void setupUI() {
        setupTitle();
        setupRecyclerView();
        setupAddButton();
        setupCloseButton();
    }

    private void setupTitle() {
        TextView tvTitle = findViewById(R.id.txt_Title);
        Category category = categoryService.getCategory(categoryId);
        if (category != null) {
            tvTitle.setText(String.format("Category Budget: %s", category.getName()));
        }
    }

    private void setupRecyclerView() {
        rvCategoryBudgets = findViewById(R.id.rvCategoryBudgets);
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryBudgetAdapter(this, categoryId, accountService, categoryBudgetService,
                new CategoryBudgetAdapter.OnCategoryBudgetActionListener() {
                    @Override
                    public void onEdit(CategoryBudget categoryBudget) {
                        showEditDialog(categoryBudget);
                    }

                    @Override
                    public void onDelete(CategoryBudget categoryBudget) {
                        showDeleteConfirmationDialog(categoryBudget);
                    }
                });

        rvCategoryBudgets.setAdapter(adapter);
        updateCategoryBudgetsList();
    }

    private void setupAddButton() {
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> showAddDialog());
    }
    private void setupCloseButton() {
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }
    private void updateCategoryBudgetsList() {
        Category category = categoryService.getCategory(categoryId);
        if (category != null) {
            adapter.setCategoryBudgets(category.getAccountInCategoryBudgets());
        }
    }

    private void showAddDialog() {
        AlertDialog dialog = createBudgetDialog(null);
        dialog.show();
    }

    private void showEditDialog(CategoryBudget categoryBudget) {
        AlertDialog dialog = createBudgetDialog(categoryBudget);
        dialog.show();
    }

    private AlertDialog createBudgetDialog(CategoryBudget existingBudget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category_budget, null);
        builder.setView(dialogView);

        Spinner spinnerAccount = dialogView.findViewById(R.id.spinnerAccount);
        EditText etTotalAmount = dialogView.findViewById(R.id.edt_total_amount);
        EditText etRemainingAmount = dialogView.findViewById(R.id.edt_remaining_amount);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        boolean isEditMode = existingBudget != null;
        etRemainingAmount.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        setupAccountSpinner(spinnerAccount, isEditMode ? existingBudget.getAccountId() : null);

        if (isEditMode) {
            etTotalAmount.setText(String.valueOf(existingBudget.getTotalAmount()));
            etRemainingAmount.setText(String.valueOf(existingBudget.getRemainingAmount()));
        }

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> handleSaveAction(dialog, spinnerAccount, etTotalAmount, etRemainingAmount, isEditMode));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }

    private void setupAccountSpinner(Spinner spinner, String selectedAccountId) {
        List<Account> accounts = accountService.getListAccounts();
        List<String> accountNames = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        int selectedPosition = 0;

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            accountNames.add(account.getName());
            accountIds.add(account.getAccountId());
            if (selectedAccountId != null && account.getAccountId().equals(selectedAccountId)) {
                selectedPosition = i;
            }
        }

        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                accountNames
        );
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(accountAdapter);
        spinner.setSelection(selectedPosition);
    }

    private void handleSaveAction(AlertDialog dialog, Spinner spinnerAccount,
                                  EditText etTotalAmount, EditText etRemainingAmount,
                                  boolean isEditMode) {
        List<Account> accounts = accountService.getListAccounts();
        String accountId = accounts.get(spinnerAccount.getSelectedItemPosition()).getAccountId();

        String totalAmountStr = etTotalAmount.getText().toString().trim();
        String remainingAmountStr = isEditMode ? etRemainingAmount.getText().toString().trim() : "0";

        if (!validateAmountInput(etTotalAmount, totalAmountStr, etRemainingAmount, remainingAmountStr, isEditMode)) {
            return;
        }

        try {
            double totalAmount = Double.parseDouble(totalAmountStr);
            double remainingAmount = isEditMode ? Double.parseDouble(remainingAmountStr) : 0;

            categoryBudgetService.setBudgetForAccount(categoryId, accountId, totalAmount, isEditMode ? remainingAmount : null);
            updateCategoryBudgetsList();
            dialog.dismiss();
        } catch (NumberFormatException e) {
            etTotalAmount.setError(INVALID_NUMBER_ERROR);
        }
    }

    private boolean validateAmountInput(EditText etTotalAmount, String totalAmountStr,
                                        EditText etRemainingAmount, String remainingAmountStr,
                                        boolean checkRemaining) {
        if (totalAmountStr.isEmpty()) {
            etTotalAmount.setError("Please enter the budget amount");
            return false;
        }

        if (checkRemaining && remainingAmountStr.isEmpty()) {
            etRemainingAmount.setError("Please enter the remaining amount");
            return false;
        }

        try {
            double totalAmount = Double.parseDouble(totalAmountStr);
            if (totalAmount <= 0) {
                etTotalAmount.setError(INVALID_AMOUNT_ERROR);
                return false;
            }

            if (checkRemaining) {
                double remainingAmount = Double.parseDouble(remainingAmountStr);
                if (remainingAmount < 0) {
                    etRemainingAmount.setError(NEGATIVE_REMAINING_ERROR);
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            etTotalAmount.setError(INVALID_NUMBER_ERROR);
            return false;
        }

        return true;
    }

    private void showDeleteConfirmationDialog(CategoryBudget categoryBudget) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryBudgetService.removeCategoryBudget(categoryId, categoryBudget.getAccountId());
                    updateCategoryBudgetsList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}