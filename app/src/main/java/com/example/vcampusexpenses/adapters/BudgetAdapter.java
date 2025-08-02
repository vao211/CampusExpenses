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
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>{
    private List<Budget> budgetList;
    private BudgetService budgetService;
    private OnBudgetClickListener listener;
    private AccountService accountService;
    private Budget budget;
    public interface OnBudgetClickListener {
        void onEditBudgetClick(String budgetId);
        void onDeleteBudgetClick(String budgetId);
    }
    public BudgetAdapter(List<Budget> budgetList, BudgetService budgetService, OnBudgetClickListener listener){
        this.budgetList = budgetList;
        this.budgetService = budgetService;
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

        budget = budgetList.get(position);
        List<String> budgetListAccounts = budget.getListAccountIds();
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
        holder.txtBudgetName.setText(budget.getName());
        holder.txtBudgetDate.setText(budget.getStartDate() + " to " + budget.getEndDate());
        holder.txtAccountNames.setText(accountNames.toString());
        holder.txtTotalAmount.setText(String.valueOf(budget.getTotalAmount()));
        holder.btnEditBudget.setOnClickListener(v -> listener.onEditBudgetClick(budget.getBudgetId()));
        holder.btnDeleteBudget.setOnClickListener(v -> listener.onDeleteBudgetClick(budget.getBudgetId()));
    }


    @Override
    public int getItemCount() {
        return budgetList != null ? budgetList.size() : 0;
    }
    public void updateData(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList;
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
