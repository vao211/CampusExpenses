package com.example.vcampusexpenses.adapters;

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

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>{
    private List<AccountBudget> accountBudgetList;
    private AccountBudgetService accountBudgetService;
    private OnBudgetClickListener listener;
    private AccountService accountService;
    private AccountBudget accountBudget;
    public interface OnBudgetClickListener {
        void onEditBudgetClick(String budgetId);
        void onDeleteBudgetClick(String budgetId);
    }
    public BudgetAdapter(List<AccountBudget> accountBudgetList, AccountBudgetService accountBudgetService, AccountService accountService, OnBudgetClickListener listener){
        this.accountBudgetList = accountBudgetList;
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
        accountBudget = accountBudgetList.get(position);
        List<String> budgetListAccounts = accountBudget.getListAccountIds();
        StringBuilder accountNames = new StringBuilder();

        if (budgetListAccounts != null) {
            for (String accountId : budgetListAccounts) {
                Account account = accountService.getAccount(accountId);
                if (account != null && account.getName() != null) {
                    accountNames.append(account.getName()).append(", ");
                }
            }
            // Xóa dấu phẩy và khoảng trắng cuối cùng nếu có
            if (accountNames.length() > 0) {
                accountNames.setLength(accountNames.length() - 2);
            }
        }
        holder.txtBudgetName.setText(accountBudget.getName());
        holder.txtBudgetDate.setText(accountBudget.getStartDate() + " - " + accountBudget.getEndDate());
        holder.txtAccountNames.setText(accountNames.toString());
        holder.txtTotalAmount.setText(String.valueOf(accountBudget.getTotalAmount()));
        holder.btnEditBudget.setOnClickListener(v -> listener.onEditBudgetClick(accountBudget.getBudgetId()));
        holder.btnDeleteBudget.setOnClickListener(v -> listener.onDeleteBudgetClick(accountBudget.getBudgetId()));
    }


    @Override
    public int getItemCount() {
        return accountBudgetList != null ? accountBudgetList.size() : 0;
    }
    public void updateData(List<AccountBudget> newAccountBudgetList) {
        this.accountBudgetList = newAccountBudgetList;
        notifyDataSetChanged();
    }
    static class BudgetViewHolder extends RecyclerView.ViewHolder{
        TextView txtBudgetName, txtBudgetDate, txtAccountNames, txtTotalAmount;
        ImageButton btnEditBudget, btnDeleteBudget;
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBudgetName = itemView.findViewById(R.id.txt_budget_name);
            txtBudgetDate = itemView.findViewById(R.id.txt_budget_date);
            txtAccountNames = itemView.findViewById(R.id.txt_account_name);
            txtTotalAmount = itemView.findViewById(R.id.txt_total_amount);
            btnEditBudget = itemView.findViewById(R.id.btn_edit_budget);
            btnDeleteBudget = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}
