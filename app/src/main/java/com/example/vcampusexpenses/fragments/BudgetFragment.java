package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class BudgetFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {
    private RecyclerView recyclerViewBudgets;
    private TextView txtEmptyBudgets;
    private BudgetService budgetService;
    private AccountService accountService;
    private CategoryService categoryService;
    private ImageButton btnAddBudget;

//    String accoutId = addAccountForBudget("Example account", accountService);
//    String categoryId1 = addCategoryForBudget("Example category", categoryService);

    UserDataManager userDataManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        recyclerViewBudgets = view.findViewById(R.id.rv_budget);
        txtEmptyBudgets = view.findViewById(R.id.txt_empty_budget);
        btnAddBudget = view.findViewById(R.id.btn_add_budget);

        // Lấy userId từ SessionManager
        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        userDataManager = new UserDataManager(requireContext(), userId);
        budgetService = new BudgetService(userDataManager);
        accountService = new AccountService(userDataManager);
        categoryService = new CategoryService(userDataManager);

        // Thiết lập RecyclerView
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        addBudget();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBudget();
    }

    private void addBudget() {
        btnAddBudget.setOnClickListener(v -> {
            Log.d("BudgetFragment", "Adding new budget");

            LinearLayout layout = new LinearLayout(requireContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 16, 16);


            final EditText inputBudgetName = new EditText(requireContext());
            inputBudgetName.setHint("Enter Budget");
            layout.addView(inputBudgetName);

            final EditText inputTotalAmount = new EditText(requireContext());
            inputTotalAmount.setHint("Enter Total Amount");
            inputTotalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputTotalAmount);

            final EditText inputRemainingAmount = new EditText(requireContext());
            inputRemainingAmount.setHint("Enter Remain");
            inputRemainingAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputRemainingAmount);

            final EditText inputStartDate = new EditText(requireContext());
            inputStartDate.setHint("Enter Start Date");
            layout.addView(inputStartDate);

            final EditText inputEndDate = new EditText(requireContext());
            inputEndDate.setHint("Enter End Date");
            layout.addView(inputEndDate);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add Budget");
            builder.setView(layout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String budgetName = inputBudgetName.getText().toString().trim();
                String totalAmountStr = inputTotalAmount.getText().toString().trim();
                String remainingAmountStr = inputRemainingAmount.getText().toString().trim();
                String startDate = inputStartDate.getText().toString().trim();
                String endDate = inputEndDate.getText().toString().trim();

                // Kiểm tra dữ liệu đầu vào
                if ( budgetName.isEmpty() ||
                        totalAmountStr.isEmpty() || remainingAmountStr.isEmpty() ||
                        startDate.isEmpty() || endDate.isEmpty()) {
                    DisplayToast.Display(requireContext(), "Please enter full information");
                    return;
                }

                double totalAmount, remainingAmount;
                try {
                    totalAmount = Double.parseDouble(totalAmountStr);
                    remainingAmount = Double.parseDouble(remainingAmountStr);
                } catch (NumberFormatException e) {
                    DisplayToast.Display(requireContext(), "Invalid Amount format");
                    return;
                }

                if (!isValidDate(startDate) || !isValidDate(endDate)) {
                    DisplayToast.Display(requireContext(), "Invalid date format (yyyy-MM-dd)");
                    return;
                }

                // Tạo ngân sách mới
                Budget budget = new Budget(budgetName, totalAmount, remainingAmount, startDate, endDate);
                budgetService.addBudget(budget);
                userDataManager.saveData();
                loadBudget();
            });
            builder.setNegativeButton("Cancel", (dialog, which) ->{
                dialog.cancel();
            });
            builder.show();
        });
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
    private String addAccountForBudget(String accountName, AccountService accountService) {
        Account account = new Account(accountName, 1000.0);
        accountService.addAccount(account);
        String accountId = accountService.getAccountId(accountName);
        if (accountId != null) {
            userDataManager.saveData();
        } else {
            Log.w("BudgetFragment", "Không thể lấy accountId cho tài khoản: " + accountName);
        }
        return accountId;
    }

    private String addCategoryForBudget(String categoryName, CategoryService categoryService) {
        Category category = new Category(categoryName);
        categoryService.addCategory(category);
        String categoryId = categoryService.getCategoryId(categoryName);
        if (categoryId != null) {
            userDataManager.saveData();
        } else {
            Log.w("BudgetFragment", "Không thể lấy categoryId cho danh mục: " + categoryName);
        }
        return categoryId;
    }

    @Override
    public void onEditBudgetClick(String budgetId) {

        Budget budget = budgetService.getListUserBudgets().stream()
                .filter(b -> b.getBudgetId().equals(budgetId))
                .findFirst()
                .orElse(null);
        if (budget == null){
            DisplayToast.Display(requireContext(), "Budget is not exist");
            return;
        }
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);


        final EditText inputBudgetName = new EditText(requireContext());
        inputBudgetName.setText(budget.getName());
        inputBudgetName.setHint("Enter New Budget");
        layout.addView(inputBudgetName);

        final EditText inputTotalAmount = new EditText(requireContext());
        inputTotalAmount.setText(String.valueOf(budget.getTotalAmount()));
        inputTotalAmount.setHint("Enter New Total Amount");
        inputTotalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputTotalAmount);

        final EditText inputRemainingAmount = new EditText(requireContext());
        inputRemainingAmount.setText(String.valueOf(budget.getRemainingAmount()));
        inputRemainingAmount.setHint("Enter New Amount Remain");
        inputRemainingAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(inputRemainingAmount);

        final EditText inputStartDate = new EditText(requireContext());
        inputStartDate.setText(budget.getStartDate());
        inputStartDate.setHint("Enter New Start Date (Year-Month-Date)");
        layout.addView(inputStartDate);

        final EditText inputEndDate = new EditText(requireContext());
        inputEndDate.setText(budget.getEndDate());
        inputEndDate.setHint("Enter New End Date (Year-Month-Date)");
        layout.addView(inputEndDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modify Budget");
        builder.setView(layout);
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String newBudgetName = inputBudgetName.getText().toString().trim();
            String newTotalAmountStr = inputTotalAmount.getText().toString().trim();
            String newRemainingAmountStr = inputRemainingAmount.getText().toString().trim();
            String newStartDate = inputStartDate.getText().toString().trim();
            String newEndDate = inputEndDate.getText().toString().trim();
            if (newBudgetName.isEmpty() || newTotalAmountStr.isEmpty() || newRemainingAmountStr.isEmpty() ||
                    newStartDate.isEmpty() || newEndDate.isEmpty()) {
                DisplayToast.Display(requireContext(), "Please enter full information");
                return;
            }

            double newTotalAmount, newRemainingAmount;
            try {
                newTotalAmount = Double.parseDouble(newTotalAmountStr);
                newRemainingAmount = Double.parseDouble(newRemainingAmountStr);
            } catch (NumberFormatException e) {
                DisplayToast.Display(requireContext(), "Amout not valid");
                return;
            }

            if (!isValidDate(newStartDate) || !isValidDate(newEndDate)) {
                DisplayToast.Display(requireContext(), "Invalid date format (yyyy-MM-dd)");
                return;
            }

            // Cập nhật ngân sách
            Budget newBudget = new Budget(budgetId, newBudgetName, newTotalAmount, newRemainingAmount, newStartDate, newEndDate);
            budgetService.updateBudget(budgetId, newBudget, true);
            userDataManager.saveData();
            loadBudget();
        });
        builder.setNegativeButton("Cancel", (dialog,which) ->{
            dialog.cancel();
        });
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
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}