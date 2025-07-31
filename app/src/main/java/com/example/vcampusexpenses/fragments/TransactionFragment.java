package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.AddTransactionActivity;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DateFilterUtil;
import com.example.vcampusexpenses.utils.DateFilterView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionFragment extends Fragment {
    String TAG = "TransactionFragment";
    private DateFilterView dateFilterView;
    private DateFilterUtil dateFilterUtil;
    private LinearLayout llIncome, llOutcome, llTransfer, llAll;
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private TransactionService transactionService;
    private String transactionType;
    ImageButton btnAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();
        dataManager = UserDataManager.getInstance(requireContext(), userId);

        accountService = new AccountService(dataManager);
        categoryService = new CategoryService(dataManager);
        budgetService = new BudgetService(dataManager);
        transactionService = new TransactionService(dataManager, accountService, budgetService);

        btnAdd = view.findViewById(R.id.btn_add);

        llIncome = view.findViewById(R.id.ll_income);
        llOutcome = view.findViewById(R.id.ll_outcome);
        llTransfer = view.findViewById(R.id.ll_transfer);
        llAll = view.findViewById(R.id.ll_all);


        dateFilterView = view.findViewById(R.id.date_filter_view);
        initDateFilter(savedInstanceState);
        setTransactionTypeSelection();
        setTransactionTypeSelection("all");
        addTransaction();
        return view;
    }
    private void addTransaction(){
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
            startActivity(intent);
        });
    }
    private void setTransactionTypeSelection() {
        setTransactionTypeSelection(null);
        llIncome.setOnClickListener(v -> setTransactionTypeSelection("income"));
        llOutcome.setOnClickListener(v -> setTransactionTypeSelection("outcome"));
        llTransfer.setOnClickListener(v -> setTransactionTypeSelection("transfer"));
        llAll.setOnClickListener(v -> setTransactionTypeSelection("all"));
    }
    private void setTransactionTypeSelection(String selected) {
        llIncome.setBackgroundResource(R.drawable.border);
        llOutcome.setBackgroundResource(R.drawable.border);
        llTransfer.setBackgroundResource(R.drawable.border);
        llAll.setBackgroundResource(R.drawable.border);

        if ("income".equals(selected)) {
            llIncome.setBackgroundResource(R.drawable.bg_selected_income);
            transactionType = "income";
        } else if ("outcome".equals(selected)) {
            llOutcome.setBackgroundResource(R.drawable.bg_selected_outcome);
            transactionType = "outcome";
        } else if ("transfer".equals(selected)) {
            llTransfer.setBackgroundResource(R.drawable.bg_selected_transfer);
            transactionType = "transfer";
        } else if ("all".equals(selected)) {
            llAll.setBackgroundResource(R.drawable.bg_selected_all);
            transactionType = "all";
        }
    }

    //date filter
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dateFilterUtil.saveInstanceState(outState);
    }

    private void loadDataForCurrentPeriod(Calendar startDate, Calendar endDate, DateFilterUtil.FilterType filterType) {
        if (startDate == null || endDate == null) {
            Log.e(TAG, "Cannot load data: startDate or endDate is null.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Log.i(TAG, "Loading data for period: " +
                dateFormat.format(startDate.getTime()) + " to " +
                dateFormat.format(endDate.getTime()));
    }
    private void initDateFilter(Bundle savedInstanceState) {
        dateFilterUtil = new DateFilterUtil(requireContext(), getParentFragmentManager());
        dateFilterView.setDateFilterUtil(dateFilterUtil, getParentFragmentManager());
        dateFilterUtil.setOnDateFilterChangedListener((startDate, endDate, filterType) -> loadDataForCurrentPeriod(startDate, endDate, filterType));

        if (savedInstanceState != null) {
            dateFilterUtil.restoreInstanceState(savedInstanceState);
        }
    }
}