package com.example.vcampusexpenses.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.TransactionActivity;
import com.example.vcampusexpenses.adapters.TransactionAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DateFilterUtil;
import com.example.vcampusexpenses.utils.DateFilterView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TransactionFragment extends Fragment implements TransactionAdapter.OnTransactionListener {
    private static final String TAG = "TransactionFragment";
    private DateFilterView dateFilterView;
    private DateFilterUtil dateFilterUtil;
    private LinearLayout llIncome, llOutcome, llTransfer, llAll;
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private CategoryService categoryService;
    private BudgetService budgetService;
    private TransactionService transactionService;
    private String transactionType = "all";
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private ImageButton btnAdd;
    private TextView txtNoTransactions;

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
        txtNoTransactions = view.findViewById(R.id.txt_no_transactions);

        //recyclerView
        rvTransactions = view.findViewById(R.id.rv_transactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(dataManager,transactionService, accountService, categoryService, this);
        rvTransactions.setAdapter(transactionAdapter);

        dateFilterView = view.findViewById(R.id.date_filter_view);
        initDateFilter(savedInstanceState);
        setTransactionTypeSelection();
        setTransactionTypeSelection("all");
        addTransaction();
        loadTransactions(null, null, DateFilterUtil.FilterType.THIS_MONTH);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions(dateFilterUtil.getStartDate(), dateFilterUtil.getEndDate(), dateFilterUtil.getCurrentFilterType());
    }

    private void addTransaction() {
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TransactionActivity.class);
            startActivity(intent);
        });
    }

    private void setTransactionTypeSelection() {
        setTransactionTypeSelection(null);
        llIncome.setOnClickListener(v -> {
            setTransactionTypeSelection("income");
            loadTransactions(dateFilterUtil.getStartDate(), dateFilterUtil.getEndDate(), dateFilterUtil.getCurrentFilterType());
        });
        llOutcome.setOnClickListener(v -> {
            setTransactionTypeSelection("outcome");
            loadTransactions(dateFilterUtil.getStartDate(), dateFilterUtil.getEndDate(), dateFilterUtil.getCurrentFilterType());
        });
        llTransfer.setOnClickListener(v -> {
            setTransactionTypeSelection("transfer");
            loadTransactions(dateFilterUtil.getStartDate(), dateFilterUtil.getEndDate(), dateFilterUtil.getCurrentFilterType());
        });
        llAll.setOnClickListener(v -> {
            setTransactionTypeSelection("all");
            loadTransactions(dateFilterUtil.getStartDate(), dateFilterUtil.getEndDate(), dateFilterUtil.getCurrentFilterType());
        });
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

    private void loadTransactions(Calendar startDate, Calendar endDate, DateFilterUtil.FilterType filterType) {
        List<Transaction> transactions = transactionService.getListTransactions();
        List<Transaction> filteredTransactions = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Transaction transaction : transactions) {
            try {
                if (!"all".equalsIgnoreCase(transactionType) && !transaction.getType().equalsIgnoreCase(transactionType)) {
                    continue;
                }

                if (startDate != null && endDate != null) {
                    String transactionDate = transaction.getDate();
                    Calendar transDate = Calendar.getInstance();
                    transDate.setTime(dateFormat.parse(transactionDate));
                    setCalendarToBeginningOfDay(transDate);
                    if (transDate.before(startDate) || transDate.after(endDate)) {
                        continue;
                    }
                }

                filteredTransactions.add(transaction);
            } catch (Exception e) {
                Log.e(TAG, "Error parse transaction date: " + e.getMessage(), e);
            }
        }

            Collections.reverse(filteredTransactions);
//        Collections.sort(filteredTransactions, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));
        transactionAdapter.setTransactions(filteredTransactions);
        if (filteredTransactions.isEmpty()) {
            txtNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            txtNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void setCalendarToBeginningOfDay(Calendar cal) {
        if (cal == null) return;
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void initDateFilter(Bundle savedInstanceState) {
        dateFilterUtil = new DateFilterUtil(requireContext(), getParentFragmentManager());
        dateFilterView.setDateFilterUtil(dateFilterUtil, getParentFragmentManager());
        dateFilterUtil.setOnDateFilterChangedListener(this::loadTransactions);

        if (savedInstanceState != null) {
            dateFilterUtil.restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        dateFilterUtil.saveInstanceState(outState);
    }

    @Override
    public void onEditTransaction(Transaction transaction) {
        Intent intent = new Intent(requireContext(), TransactionActivity.class);
        intent.putExtra("transactionId", transaction.getTransactionId());
        startActivity(intent);
    }
}