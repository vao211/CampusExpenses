package com.example.vcampusexpenses.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;

import java.util.ArrayList;
import java.util.List;

public class AccountBudgetAdapter extends RecyclerView.Adapter<AccountBudgetAdapter.BudgetViewHolder> {
    private List<AccountBudget> accountBudgetList;
    private AccountBudgetService accountBudgetService;
    private AccountService accountService;
    private OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onEditBudgetClick(String budgetId);
        void onDeleteBudgetClick(String budgetId);
    }

    public AccountBudgetAdapter(List<AccountBudget> accountBudgetList, AccountBudgetService accountBudgetService, AccountService accountService, OnBudgetClickListener listener) {
        this.accountBudgetList = accountBudgetList != null ? accountBudgetList : new ArrayList<>();
        this.accountBudgetService = accountBudgetService;
        this.accountService = accountService;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        AccountBudget accountBudget = accountBudgetList.get(position);
        List<String> accountBudgetList = accountBudget.getListAccountIds();
        StringBuilder accountNames = new StringBuilder();

        if (accountBudgetList != null && !accountBudgetList.isEmpty()) {
            for (String accountId : accountBudgetList) {
                Account account = accountService.getAccount(accountId);
                if (account != null && account.getName() != null) {
                    accountNames.append(account.getName()).append(", ");
                }
            }
            if (accountNames.length() > 0) {
                accountNames.setLength(accountNames.length() - 2); // Xóa dấu phẩy và khoảng trắng cuối
            }
        } else {
            accountNames.append("No accounts");
        }
        holder.txtBudgetName.setText(accountBudget.getName() != null ? accountBudget.getName() : "Unnamed Budget");
        holder.txtBudgetDate.setText(String.valueOf(accountBudget.getStartDate() + " to " + accountBudget.getEndDate()));
        holder.txtAccountNames.setText(accountNames.toString());
        holder.txtRemainingAmount.setText(String.valueOf("remain: "+accountBudget.getRemainingAmount()));
        holder.txtTotalAmount.setText(String.valueOf("total: " + accountBudget.getTotalAmount()));
        holder.btnEditBudget.setOnClickListener(v -> listener.onEditBudgetClick(accountBudget.getBudgetId()));
        holder.btnDeleteBudget.setOnClickListener(v -> listener.onDeleteBudgetClick(accountBudget.getBudgetId()));
    }

    @Override
    public int getItemCount() {
        return accountBudgetList.size();
    }

    public void updateData(List<AccountBudget> newAccountBudgetList) {
        this.accountBudgetList = newAccountBudgetList != null ? newAccountBudgetList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView txtRemainingAmount ,txtBudgetName, txtBudgetDate, txtAccountNames, txtTotalAmount;
        ImageButton btnEditBudget, btnDeleteBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBudgetName = itemView.findViewById(R.id.txt_budget_name);
            txtBudgetDate = itemView.findViewById(R.id.txt_budget_date);
            txtAccountNames = itemView.findViewById(R.id.txt_account_name);
            txtRemainingAmount = itemView.findViewById(R.id.txt_remaining_amount);
            txtTotalAmount = itemView.findViewById(R.id.txt_total_amount);
            btnEditBudget = itemView.findViewById(R.id.btn_edit_budget);
            btnDeleteBudget = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}