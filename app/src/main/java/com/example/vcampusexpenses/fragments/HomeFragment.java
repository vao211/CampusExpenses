package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.AccountActivity;
import com.example.vcampusexpenses.activity.SettingActivity;
import com.example.vcampusexpenses.activity.TransactionActivity;
import com.example.vcampusexpenses.adapters.HomeAccountAdapter;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.utils.DateFilterUtil;
import com.example.vcampusexpenses.utils.DateFilterView;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int MAX_DISPLAY_ACCOUNTS = 2;

    private ImageButton btnSetting, btnAdd;
    private TextView txtTotalIncome, txtTotalOutcome, txtNoAccounts;
    private RecyclerView rvAccounts;
    private Button btnViewAll;
    private DateFilterView dateFilterView;
    private DateFilterUtil dateFilterUtil;

    private HomeAccountAdapter accountAdapter;
    private AccountService accountService;
    private TransactionService transactionService;
    private AccountBudgetService accountBudgetService;
    private SessionManager sessionManager;
    private UserDataManager dataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeServices();
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();

        if (savedInstanceState != null) {
            dateFilterUtil.restoreInstanceState(savedInstanceState);
        } else {
            dateFilterUtil.applyFilter(DateFilterUtil.FilterType.THIS_MONTH, false);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAccountList();
        loadDataForCurrentPeriod();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dateFilterUtil.saveInstanceState(outState);
    }

    private void initializeServices() {
        sessionManager = new SessionManager(requireContext());
        dataManager = UserDataManager.getInstance(requireContext(), sessionManager.getUserId());
        accountService = new AccountService(dataManager);
        accountBudgetService = new AccountBudgetService(dataManager);
        transactionService = new TransactionService(dataManager, accountService, accountBudgetService);
    }

    private void initializeViews(View view) {
        btnSetting = view.findViewById(R.id.btn_setting);
        btnAdd = view.findViewById(R.id.btn_add);
        txtTotalIncome = view.findViewById(R.id.txt_total_income);
        txtTotalOutcome = view.findViewById(R.id.txt_total_outcome);
        rvAccounts = view.findViewById(R.id.rv_HomeAccounts);
        btnViewAll = view.findViewById(R.id.btn_view_all);
        txtNoAccounts = view.findViewById(R.id.txt_NoHomeAccounts);
        dateFilterView = view.findViewById(R.id.date_filter_view);
        dateFilterUtil = new DateFilterUtil(requireContext(), getParentFragmentManager());
        dateFilterView.setDateFilterUtil(dateFilterUtil, getParentFragmentManager());
        dateFilterUtil.setOnDateFilterChangedListener(this::onDateFilterChanged);
    }

    private void setupRecyclerView() {
        rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<Account> accountList = getLimitedAccountList();
        accountAdapter = new HomeAccountAdapter(requireContext(), accountList);
        rvAccounts.setAdapter(accountAdapter);
        updateAccountVisibility(accountList);
    }

    private List<Account> getLimitedAccountList() {
        List<Account> accountList = accountService.getListAccounts();
        if (accountList == null) {
            Log.w(TAG, "Account list is null. Returning empty list.");
            return new ArrayList<>();
        }
        return accountList.size() > MAX_DISPLAY_ACCOUNTS
                ? new ArrayList<>(accountList.subList(0, MAX_DISPLAY_ACCOUNTS))
                : new ArrayList<>(accountList);
    }

    private void updateAccountVisibility(List<Account> accountList) {
        boolean isEmpty = accountList.isEmpty();
        txtNoAccounts.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvAccounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void refreshAccountList() {
        List<Account> newAccountList = getLimitedAccountList();
        accountAdapter.updateAccounts(newAccountList);
        updateAccountVisibility(newAccountList);
    }

    private void setupClickListeners() {
        btnSetting.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingActivity.class)));
        btnAdd.setOnClickListener(v -> startActivity(new Intent(requireContext(), TransactionActivity.class)));
        btnViewAll.setOnClickListener(v -> startActivity(new Intent(requireContext(), AccountActivity.class)));
        txtNoAccounts.setOnClickListener(v -> startActivity(new Intent(requireContext(), AccountActivity.class)));
    }

    private void onDateFilterChanged(Calendar startDate, Calendar endDate, DateFilterUtil.FilterType filterType) {
        loadDataForCurrentPeriod();
    }

    private void loadDataForCurrentPeriod() {
        Calendar startDate = dateFilterUtil.getStartDate();
        Calendar endDate = dateFilterUtil.getEndDate();
        if (startDate == null || endDate == null) {
            Log.e(TAG, "Cannot load data: startDate or endDate is null.");
            DisplayToast.Display(requireContext(), "Error: Date range not set.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Log.i(TAG, String.format("Loading data for period: %s to %s",
                dateFormat.format(startDate.getTime()),
                dateFormat.format(endDate.getTime())));

        //lọc income và outcome
        double totalIncome = transactionService.getTotalIncome(startDate.getTimeInMillis(), endDate.getTimeInMillis());
        double totalOutcome = transactionService.getTotalOutcome(startDate.getTimeInMillis(), endDate.getTimeInMillis());

        txtTotalIncome.setText(String.valueOf(totalIncome));
        txtTotalOutcome.setText(String.valueOf(totalOutcome));
    }
}