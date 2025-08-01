package com.example.vcampusexpenses.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.SettingService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final OnTransactionListener listener;
    private UserDataManager userDataManager;
    public interface OnTransactionListener {
        void onEditTransaction(Transaction transaction);
    }

    public TransactionAdapter(UserDataManager userDataManager, TransactionService transactionService, AccountService accountService,
                              CategoryService categoryService, OnTransactionListener listener) {
        this.transactions = new ArrayList<>();
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.userDataManager = userDataManager;
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryName, txtAccountName, txtToAccountName, txtTypeTransaction, txtAmount, txtCurrency;
        ImageButton btnEdit, btnDelete;
        SettingService settingService;
        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryName = itemView.findViewById(R.id.txt_category_name);
            txtAccountName = itemView.findViewById(R.id.txt_account_name);
            txtToAccountName = itemView.findViewById(R.id.txt_to_account_name);
            txtTypeTransaction = itemView.findViewById(R.id.txt_type_transaction);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtCurrency = itemView.findViewById(R.id.txt_type_currency);
            btnEdit = itemView.findViewById(R.id.btn_editItemTransaction);
            btnDelete = itemView.findViewById(R.id.btn_deleteItemTransaction);
            settingService = new SettingService(itemView.getContext());
        }

        void bind(Transaction transaction) {
            txtTypeTransaction.setText(transaction.getType());
            txtCurrency.setText(settingService.getCurrency());
            txtAmount.setText(String.valueOf(transaction.getAmount()));

            if (transaction.isTransfer()) {
                Account fromAccount = accountService.getAccount(transaction.getFromAccountId());
                Account toAccount = accountService.getAccount(transaction.getToAccountId());
                txtAccountName.setText(fromAccount != null ? fromAccount.getName() : "Unknown");
                txtToAccountName.setText(toAccount != null ? toAccount.getName() : "Unknown");
                txtCategoryName.setText(R.string.transfer);
                txtToAccountName.setVisibility(View.VISIBLE);
            } else {
                Account account = accountService.getAccount(transaction.getAccountId());
                Category category = categoryService.getCategory(transaction.getCategoryId());

                txtAccountName.setText(account != null ? account.getName() : "Unknown");
                txtCategoryName.setText(category != null ? category.getName() : "Unknown");
                txtToAccountName.setVisibility(View.GONE);
            }

            btnEdit.setOnClickListener(v -> listener.onEditTransaction(transaction));
            btnDelete.setOnClickListener(v -> {
                transactionService.deleteTransaction(transaction.getTransactionId());
                userDataManager.saveData();
                transactions.remove(transaction);
                notifyDataSetChanged();
            });
        }
    }
}