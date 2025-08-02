package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.example.vcampusexpenses.activity.BudgetActivity;
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

        // Lấy userId từ SessionManager
        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        userDataManager = new UserDataManager(requireContext(), userId);
        budgetService = new BudgetService(userDataManager);
        accountService = new AccountService(userDataManager);
        categoryService = new CategoryService(userDataManager);

        // Tạo tài khoản và danh mục mặc định, tương tự ví dụ

        // Thiết lập RecyclerView
        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewBudgets.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

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
            Intent intent = new Intent(requireContext(), BudgetActivity.class);
            startActivity(intent);
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
            BudgetAdapter budgetAdapter = new BudgetAdapter(budgetList, budgetService, this);
            recyclerViewBudgets.setAdapter(budgetAdapter);
        }
    }

    @Override
    public void onEditBudgetClick(String budgetId) {
        Intent intent = new Intent(requireContext(), BudgetActivity.class);
        intent.putExtra("budgetId", budgetId);
        startActivity(intent);
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