package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.BudgetAccountActivity;
import com.example.vcampusexpenses.adapters.AccountBudgetAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class AccountBudgetFragment extends Fragment implements AccountBudgetAdapter.OnBudgetClickListener {
    private RecyclerView recyclerViewBudgets;
    private TextView txtEmptyBudgets;
    private AccountBudgetService accountBudgetService;
    private AccountService accountService;
    private CategoryService categoryService;
    private ImageButton btnAddBudget;
    private UserDataManager userDataManager;
    private AccountBudgetAdapter budgetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        recyclerViewBudgets = view.findViewById(R.id.rv_budget);
        txtEmptyBudgets = view.findViewById(R.id.txt_empty_budget);
        btnAddBudget = view.findViewById(R.id.btn_add_budget);

        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();

        userDataManager = UserDataManager.getInstance(requireContext(), userId);
        accountBudgetService = new AccountBudgetService(userDataManager);
        accountService = new AccountService(userDataManager);
        categoryService = new CategoryService(userDataManager);

        recyclerViewBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewBudgets.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        budgetAdapter = new AccountBudgetAdapter(null, accountBudgetService, accountService, this);
        recyclerViewBudgets.setAdapter(budgetAdapter);

        addBudget();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadBudget();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("AccountBudgetFragment", "onResume called, refreshing budget list");
        loadBudget();
    }

    private void addBudget() {
        btnAddBudget.setOnClickListener(v -> {
            Log.d("AccountBudgetFragment", "Starting BudgetAccountActivity to add budget");
            Intent intent = new Intent(requireContext(), BudgetAccountActivity.class);
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
        Log.d("AccountBudgetFragment", "Loading budgets");
        if (txtEmptyBudgets == null || recyclerViewBudgets == null) {
            Log.e("AccountBudgetFragment", "UI components not initialized");
            return;
        }

        List<AccountBudget> accountBudgetList = accountBudgetService.getListAccountBudgets();
        if (accountBudgetList == null || accountBudgetList.isEmpty()) {
            Log.d("AccountBudgetFragment", "No budgets found");
            txtEmptyBudgets.setVisibility(View.VISIBLE);
            recyclerViewBudgets.setVisibility(View.GONE);
            budgetAdapter.updateData(null);
        } else {
            Log.d("AccountBudgetFragment", "Found " + accountBudgetList.size() + " budgets");
            txtEmptyBudgets.setVisibility(View.GONE);
            recyclerViewBudgets.setVisibility(View.VISIBLE);
            budgetAdapter.updateData(accountBudgetList);
        }
    }

    @Override
    public void onEditBudgetClick(String budgetId) {
        Log.d("AccountBudgetFragment", "Starting BudgetAccountActivity to edit budget: " + budgetId);
        Intent intent = new Intent(requireContext(), BudgetAccountActivity.class);
        intent.putExtra("budgetId", budgetId);
        startActivity(intent);
    }

    @Override
    public void onDeleteBudgetClick(String budgetId) {
        Log.d("AccountBudgetFragment", "Deleting budget with ID: " + budgetId);
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    accountBudgetService.deleteBudget(budgetId);
                    userDataManager.saveData();
                    Log.d("AccountBudgetFragment", "Budget deleted, refreshing list");
                    loadBudget();
                    DisplayToast.Display(requireContext(), "AccountBudget deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}