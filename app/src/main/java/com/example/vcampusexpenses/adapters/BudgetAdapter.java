package com.example.vcampusexpenses.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>{
    private List<Budget> budgetList;
    private BudgetService budgetService;
    private OnBudgetClickListener listener;

    public interface OnBudgetClickListener {
        void onEditBudgetClick(String budgetId);
        void onDeleteBudgetClick(String budgetId);
    }

    public BudgetAdapter(List<Budget> budgetList, OnBudgetClickListener listener) {
        this.budgetList = budgetList;
        this.listener = listener;
    }
    public BudgetAdapter(List<Budget> budgetList, BudgetService budgetService, OnBudgetClickListener listener){
        this.budgetList = budgetList;
        this.budgetService = budgetService;
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
        Budget budget = budgetList.get(position);
        holder.tvBudgetName.setText(budget.getName());
        holder.tvBudgetDate.setText(budget.getStartDate() + " to " + budget.getEndDate());
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
        TextView tvBudgetName, tvBudgetDate;
        ImageButton btnEditBudget, btnDeleteBudget;
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetName = itemView.findViewById(R.id.txt_budget_name);
            tvBudgetDate = itemView.findViewById(R.id.txt_budget_date);
            btnEditBudget = itemView.findViewById(R.id.btn_edit_budget);
            btnDeleteBudget = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}
