package com.example.vcampusexpenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.CategoryBudget;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryBudgetService;
import com.example.vcampusexpenses.services.SettingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder> {
    private final Context context;
    private final List<CategoryBudget> categoryBudgets;
    private final String categoryId;
    private final AccountService accountService;
    private final CategoryBudgetService categoryBudgetService;
    private final OnCategoryBudgetActionListener listener;

    public interface OnCategoryBudgetActionListener {
        void onEdit(CategoryBudget categoryBudget);
        void onDelete(CategoryBudget categoryBudget);
    }

    public CategoryBudgetAdapter(Context context, String categoryId, AccountService accountService, CategoryBudgetService categoryBudgetService, OnCategoryBudgetActionListener listener) {
        this.context = context;
        this.categoryId = categoryId;
        this.accountService = accountService;
        this.categoryBudgetService = categoryBudgetService;
        this.listener = listener;
        this.categoryBudgets = new ArrayList<>();
    }

    public void setCategoryBudgets(Map<String, CategoryBudget> accountBudgets) {
        if (accountBudgets == null) {
            accountBudgets = new HashMap<>();
        }
        categoryBudgets.clear();
        categoryBudgets.addAll(accountBudgets.values());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_budget, parent, false);
        return new CategoryBudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryBudgetViewHolder holder, int position) {
        CategoryBudget categoryBudget = categoryBudgets.get(position);
        SettingService settingService = new SettingService(context);
        Account account = accountService.getAccount(categoryBudget.getAccountId());
        holder.txtAccountName.setText(account != null ? account.getName() : "Unknown Account");
        holder.txtTotalAmount.setText(String.valueOf(categoryBudget.getTotalAmount() + " "+ settingService.getCurrency()));
        holder.txtRemainingAmount.setText(String.valueOf(categoryBudget.getRemainingAmount()));

        holder.btnEditCategoryBudget.setOnClickListener(v -> listener.onEdit(categoryBudget));
        holder.btnDeleteCategoryBudget.setOnClickListener(v -> listener.onDelete(categoryBudget));
    }

    @Override
    public int getItemCount() {
        return categoryBudgets.size();
    }

    static class CategoryBudgetViewHolder extends RecyclerView.ViewHolder {
        TextView txtAccountName, txtTotalAmount, txtRemainingAmount;
        ImageButton btnEditCategoryBudget, btnDeleteCategoryBudget;

        public CategoryBudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAccountName = itemView.findViewById(R.id.txt_account_name);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            txtRemainingAmount = itemView.findViewById(R.id.txtRemainingAmount);
            btnEditCategoryBudget = itemView.findViewById(R.id.btn_edit);
            btnDeleteCategoryBudget = itemView.findViewById(R.id.btn_delete);
        }
    }
}