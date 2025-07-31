package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.BudgetAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class BudgetFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {
    private RecyclerView recyclerViewBudgets;
    private TextView txtEmptyBudgets;
    private BudgetService budgetService;
    private AccountService accountService;
    private CategoryService categoryService;
    private ImageButton btnAddBudget, btnManageAccounts, btnManageCategories;
    private String accountId1;
    private String accountId2; // Added for example
    private String categoryId1;
    private String categoryId2; // Added for example
    private UserDataManager userDataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        recyclerViewBudgets = view.findViewById(R.id.rv_budget);
        txtEmptyBudgets = view.findViewById(R.id.txt_empty_budget);
        btnAddBudget = view.findViewById(R.id.btn_add_budget);
        btnManageAccounts = view.findViewById(R.id.btn_manage_accounts);
        btnManageCategories = view.findViewById(R.id.btn_manage_category);

        // Lấy userId từ SessionManager
        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        userDataManager = new UserDataManager(requireContext(), userId);
        budgetService = new BudgetService(userDataManager);
        accountService = new AccountService(userDataManager);
        categoryService = new CategoryService(userDataManager);

        // Tạo tài khoản và danh mục mặc định, tương tự ví dụ
        accountId1 = addAccountForBudget("Main Savings", 5000.0, accountService);
        accountId2 = addAccountForBudget("Secondary Account", 3000.0, accountService); // Added
        categoryId1 = addCategoryForBudget("Food", categoryService);
        categoryId2 = addCategoryForBudget("Travel", categoryService); // Added

        // Thiết lập RecyclerView
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewBudgets.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        // Thiết lập sự kiện cho các nút mới
        btnManageAccounts.setOnClickListener(v -> showAccountManagementDialog());
        btnManageCategories.setOnClickListener(v -> showCategoryManagementDialog());

        addBudget();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBudget();
    }

    private String addAccountForBudget(String accountName, double balance, AccountService accountService) {
        Account account = new Account(accountName, balance);
        accountService.addAccount(account);
        String accountId = accountService.getAccountId(accountName);
        if (accountId != null) {
            userDataManager.saveData();
            Log.d("BudgetFragment", "Added account: " + accountName + " with ID: " + accountId);
        } else {
            Log.w("BudgetFragment", "Failed to get accountId for account: " + accountName);
        }
        return accountId;
    }

    private String addCategoryForBudget(String categoryName, CategoryService categoryService) {
        Category category = new Category(categoryName);
        categoryService.addCategory(category);
        String categoryId = categoryService.getCategoryId(categoryName);
        if (categoryId != null) {
            userDataManager.saveData();
            Log.d("BudgetFragment", "Added category: " + categoryName + " with ID: " + categoryId);
        } else {
            Log.w("BudgetFragment", "Failed to get categoryId for category: " + categoryName);
        }
        return categoryId;
    }

    private void addBudget() {
        btnAddBudget.setOnClickListener(v -> {
            Log.d("BudgetFragment", "Adding new budget");

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);

            final EditText inputAccount = new EditText(requireContext());
            inputAccount.setHint("Enter Account Name");
            layout.addView(inputAccount);

            final EditText inputBalance = new EditText(requireContext());
            inputBalance.setHint("Enter Account Balance");
            inputBalance.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputBalance);

            final EditText inputCategory = new EditText(requireContext());
            inputCategory.setHint("Enter Category Name");
            layout.addView(inputCategory);

            final EditText inputBudgetName = new EditText(requireContext());
            inputBudgetName.setHint("Enter Budget Name");
            layout.addView(inputBudgetName);

            final EditText inputTotalAmount = new EditText(requireContext());
            inputTotalAmount.setHint("Enter Total Amount");
            inputTotalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputTotalAmount);

            final EditText inputRemainingAmount = new EditText(requireContext());
            inputRemainingAmount.setHint("Enter Remaining Amount");
            inputRemainingAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputRemainingAmount);

            final EditText inputStartDate = new EditText(requireContext());
            inputStartDate.setHint("Enter Start Date (yyyy-MM-dd)");
            layout.addView(inputStartDate);

            final EditText inputEndDate = new EditText(requireContext());
            inputEndDate.setHint("Enter End Date (yyyy-MM-dd)");
            layout.addView(inputEndDate);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add Budget");
            builder.setView(layout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String accountName = inputAccount.getText().toString().trim();
                String balanceStr = inputBalance.getText().toString().trim();
                String categoryName = inputCategory.getText().toString().trim();
                String budgetName = inputBudgetName.getText().toString().trim();
                String totalAmountStr = inputTotalAmount.getText().toString().trim();
                String remainingAmountStr = inputRemainingAmount.getText().toString().trim();
                String startDate = inputStartDate.getText().toString().trim();
                String endDate = inputEndDate.getText().toString().trim();

                if (accountName.isEmpty() || balanceStr.isEmpty() || categoryName.isEmpty() ||
                        budgetName.isEmpty() || totalAmountStr.isEmpty() || remainingAmountStr.isEmpty() ||
                        startDate.isEmpty() || endDate.isEmpty()) {
                    DisplayToast.Display(requireContext(), "Please enter full information");
                    return;
                }

                double balance, totalAmount, remainingAmount;
                try {
                    balance = Double.parseDouble(balanceStr);
                    totalAmount = Double.parseDouble(totalAmountStr);
                    remainingAmount = Double.parseDouble(remainingAmountStr);
                } catch (NumberFormatException e) {
                    DisplayToast.Display(requireContext(), "Invalid amount format");
                    return;
                }

                if (!isValidDate(startDate) || !isValidDate(endDate)) {
                    DisplayToast.Display(requireContext(), "Invalid date format (yyyy-MM-dd)");
                    return;
                }

                if (balance < totalAmount) {
                    DisplayToast.Display(requireContext(), "Insufficient account balance: " + balance);
                    return;
                }

                String newAccountId = addAccountForBudget(accountName, balance, accountService);
                String newCategoryId = addCategoryForBudget(categoryName, categoryService);

                if (newAccountId == null || newCategoryId == null) {
                    DisplayToast.Display(requireContext(), "Failed to create account or category");
                    return;
                }

                Budget budget = new Budget(budgetName, totalAmount, remainingAmount, startDate, endDate);
                budget.addAccount(newAccountId);
                budget.addCategoryLimit(newCategoryId, balance);
                budgetService.addBudget(budget);
                userDataManager.saveData();
                loadBudget();

                Log.d("BudgetFragment", "Added budget: " + budgetName + " with accountId: " + newAccountId + " and categoryId: " + newCategoryId);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void showAccountManagementDialog() {
        List<Budget> budgetList = budgetService.getListUserBudgets();
        if (budgetList == null || budgetList.isEmpty()) {
            DisplayToast.Display(requireContext(), "No budgets available");
            return;
        }

        // Giả sử chọn budget đầu tiên để đơn giản
        String budgetId = budgetList.get(0).getBudgetId();

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        final EditText inputAccountId = new EditText(requireContext());
        inputAccountId.setHint("Enter Account ID to Add/Delete (e.g., " + accountId1 + ")");
        layout.addView(inputAccountId);

        final EditText inputNewAccountId = new EditText(requireContext());
        inputNewAccountId.setHint("Enter New Account ID for Update (e.g., " + accountId2 + ")");
        layout.addView(inputNewAccountId);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Manage Budget Accounts");
        builder.setView(layout);

        builder.setPositiveButton("Add Account", (dialog, which) -> {
            String accountId = inputAccountId.getText().toString().trim();
            if (accountId.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter account ID");
                return;
            }
            budgetService.addAccountToBudget(budgetId, accountId);
            userDataManager.saveData();
            loadBudget();
            DisplayToast.Display(requireContext(), "Account added to budget");
        });

        builder.setNeutralButton("Update Account", (dialog, which) -> {
            String oldAccountId = inputAccountId.getText().toString().trim();
            String newAccountId = inputNewAccountId.getText().toString().trim();
            if (oldAccountId.isEmpty() || newAccountId.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter both account IDs");
                return;
            }
            budgetService.updateAccountInBudget(budgetId, oldAccountId, newAccountId);
            userDataManager.saveData();
            loadBudget();
            DisplayToast.Display(requireContext(), "Account updated in budget");
        });

        builder.setNegativeButton("Delete Account", (dialog, which) -> {
            String accountId = inputAccountId.getText().toString().trim();
            if (accountId.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter account ID");
                return;
            }
            budgetService.deleteAccountFromBudget(budgetId, accountId);
            userDataManager.saveData();
            loadBudget();
            DisplayToast.Display(requireContext(), "Account removed from budget");
        });

        builder.show();
    }

    private void showCategoryManagementDialog() {
        List<Budget> budgetList = budgetService.getListUserBudgets();
        if (budgetList == null || budgetList.isEmpty()) {
            DisplayToast.Display(requireContext(), "No budgets available");
            return;
        }

        // Giả sử chọn budget đầu tiên để đơn giản
        String budgetId = budgetList.get(0).getBudgetId();

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        final EditText inputCategoryId = new EditText(requireContext());
        inputCategoryId.setHint("Enter Category ID (e.g., " + categoryId2 + ")");
        layout.addView(inputCategoryId);

        final EditText inputLimit = new EditText(requireContext());
        inputLimit.setHint("Enter Category Limit");
        inputLimit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputLimit);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Manage Budget Categories");
        builder.setView(layout);

        builder.setPositiveButton("Add Limit", (dialog, which) -> {
            String categoryId = inputCategoryId.getText().toString().trim();
            String limitStr = inputLimit.getText().toString().trim();
            if (categoryId.isEmpty() || limitStr.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter category ID and limit");
                return;
            }
            try {
                double limit = Double.parseDouble(limitStr);
                budgetService.addCategoryLimit(budgetId, categoryId, limit);
                userDataManager.saveData();
                loadBudget();
                DisplayToast.Display(requireContext(), "Category limit added");
            } catch (NumberFormatException e) {
                DisplayToast.Display(requireContext(), "Invalid limit format");
            }
        });

        builder.setNeutralButton("Update Limit", (dialog, which) -> {
            String categoryId = inputCategoryId.getText().toString().trim();
            String limitStr = inputLimit.getText().toString().trim();
            if (categoryId.isEmpty() || limitStr.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter category ID and limit");
                return;
            }
            try {
                double limit = Double.parseDouble(limitStr);
                budgetService.updateCategoryLimit(budgetId, categoryId, limit);
                userDataManager.saveData();
                loadBudget();
                DisplayToast.Display(requireContext(), "Category limit updated");
            } catch (NumberFormatException e) {
                DisplayToast.Display(requireContext(), "Invalid limit format");
            }
        });

        builder.setNegativeButton("Delete Limit", (dialog, which) -> {
            String categoryId = inputCategoryId.getText().toString().trim();
            if (categoryId.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter category ID");
                return;
            }
            budgetService.deleteCategoryLimit(budgetId, categoryId);
            userDataManager.saveData();
            loadBudget();
            DisplayToast.Display(requireContext(), "Category limit removed");
        });

        builder.show();
    }

    private boolean isValidDate(String date) {
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void loadBudget() {
        Log.d("BudgetFragment", "Loading budgets");
        if (txtEmptyBudgets == null || recyclerViewBudgets == null) {
            Log.e("BudgetFragment", "UI components not initialized");
            return;
        }

        List<Budget> budgetList = budgetService.getListUserBudgets();
        if (budgetList == null || budgetList.isEmpty()) {
            Log.d("BudgetFragment", "No budgets found");
            txtEmptyBudgets.setVisibility(View.VISIBLE);
            recyclerViewBudgets.setVisibility(View.GONE);
        } else {
            Log.d("BudgetFragment", "Found " + budgetList.size() + " budgets");
            txtEmptyBudgets.setVisibility(View.GONE);
            recyclerViewBudgets.setVisibility(View.VISIBLE);
            BudgetAdapter budgetAdapter = new BudgetAdapter(budgetList, this);
            recyclerViewBudgets.setAdapter(budgetAdapter);
        }
    }

    @Override
    public void onEditBudgetClick(String budgetId) {
        Budget budget = budgetService.getListUserBudgets().stream()
                .filter(b -> b.getBudgetId().equals(budgetId))
                .findFirst()
                .orElse(null);
        if (budget == null) {
            DisplayToast.Display(requireContext(), "Budget does not exist");
            return;
        }

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        final EditText inputBudgetName = new EditText(requireContext());
        inputBudgetName.setText(budget.getName());
        inputBudgetName.setHint("Enter New Budget Name");
        layout.addView(inputBudgetName);

        final EditText inputTotalAmount = new EditText(requireContext());
        inputTotalAmount.setText(String.valueOf(budget.getTotalAmount()));
        inputTotalAmount.setHint("Enter New Total Amount");
        inputTotalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputTotalAmount);

        final EditText inputRemainingAmount = new EditText(requireContext());
        inputRemainingAmount.setText(String.valueOf(budget.getRemainingAmount()));
        inputRemainingAmount.setHint("Enter New Remaining Amount");
        inputRemainingAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputRemainingAmount);

        final EditText inputStartDate = new EditText(requireContext());
        inputStartDate.setText(budget.getStartDate());
        inputStartDate.setHint("Enter New Start Date (yyyy-MM-dd)");
        layout.addView(inputStartDate);

        final EditText inputEndDate = new EditText(requireContext());
        inputEndDate.setText(budget.getEndDate());
        inputEndDate.setHint("Enter New End Date (yyyy-MM-dd)");
        layout.addView(inputEndDate);

        final EditText inputAccountId = new EditText(requireContext());
        inputAccountId.setHint("Enter New Account ID (e.g., " + accountId2 + ")");
        layout.addView(inputAccountId);

        final EditText inputCategoryId = new EditText(requireContext());
        inputCategoryId.setHint("Enter New Category ID (e.g., " + categoryId2 + ")");
        layout.addView(inputCategoryId);

        final EditText inputCategoryLimit = new EditText(requireContext());
        inputCategoryLimit.setHint("Enter New Category Limit");
        inputCategoryLimit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputCategoryLimit);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Budget (Override)");
        builder.setView(layout);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newBudgetName = inputBudgetName.getText().toString().trim();
            String newTotalAmountStr = inputTotalAmount.getText().toString().trim();
            String newRemainingAmountStr = inputRemainingAmount.getText().toString().trim();
            String newStartDate = inputStartDate.getText().toString().trim();
            String newEndDate = inputEndDate.getText().toString().trim();
            String newAccountId = inputAccountId.getText().toString().trim();
            String newCategoryId = inputCategoryId.getText().toString().trim();
            String newCategoryLimitStr = inputCategoryLimit.getText().toString().trim();

            if (newBudgetName.isEmpty() || newTotalAmountStr.isEmpty() || newRemainingAmountStr.isEmpty() ||
                    newStartDate.isEmpty() || newEndDate.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter full information");
                return;
            }

            double newTotalAmount, newRemainingAmount, newCategoryLimit;
            try {
                newTotalAmount = Double.parseDouble(newTotalAmountStr);
                newRemainingAmount = Double.parseDouble(newRemainingAmountStr);
                newCategoryLimit = newCategoryLimitStr.isEmpty() ? 0 : Double.parseDouble(newCategoryLimitStr);
            } catch (NumberFormatException e) {
                DisplayToast.Display(requireContext(), "Invalid amount format");
                return;
            }

            if (!isValidDate(newStartDate) || !isValidDate(newEndDate)) {
                DisplayToast.Display(requireContext(), "Invalid date format (yyyy-MM-dd)");
                return;
            }

            Budget newBudget = new Budget(budgetId, newBudgetName, newTotalAmount, newRemainingAmount, newStartDate, newEndDate);
            if (!newAccountId.isEmpty()) {
                newBudget.addAccount(newAccountId);
            }
            if (!newCategoryId.isEmpty() && newCategoryLimit > 0) {
                newBudget.addCategoryLimit(newCategoryId, newCategoryLimit);
            }
            budgetService.updateBudget(budgetId, newBudget, true); // Override
            userDataManager.saveData();
            loadBudget();
            DisplayToast.Display(requireContext(), "Budget updated");
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onDeleteBudgetClick(String budgetId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    budgetService.deleteBudget(budgetId);
                    userDataManager.saveData();
                    loadBudget();
                    DisplayToast.Display(requireContext(), "Budget deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}