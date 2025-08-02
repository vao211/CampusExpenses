package com.example.vcampusexpenses.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.SettingService;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;
    private OnAccountClickListener onAccountClickListener;
    private AccountService accountService;
    private Account account;


    public interface OnAccountClickListener {
        void onEditAccount(String accountId);
        void onDeleteAccount(String accountId);
    }

    public AccountAdapter(List<Account> accountList,AccountService accountService, OnAccountClickListener listener) {
        this.accountList = accountList;
        this.accountService = accountService;
        this.onAccountClickListener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account,
                parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        SettingService settingService = new SettingService(holder.itemView.getContext());

        Account account = accountList.get(position);
        holder.txtAccountName.setText(account.getName());
        holder.txtAccountBalance.setText(String.valueOf(account.getBalance() +" "+ settingService.getCurrency()));

        holder.btnEditAccount.setOnClickListener(view -> {
            onAccountClickListener.onEditAccount(account.getAccountId());
        });
        holder.btnDeleteAccount.setOnClickListener(view -> {
            onAccountClickListener.onDeleteAccount(account.getAccountId());
        });

    }

    @Override
    public int getItemCount() {
        return accountList != null ? accountList.size() : 0;
    }

    public void updateAccounts(List<Account> accountList) {
        if (accountList != null) {
            this.accountList = accountList;
            notifyDataSetChanged();
        }
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView txtAccountName, txtAccountBalance;
        ImageButton btnEditAccount, btnDeleteAccount;


        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAccountName = itemView.findViewById(R.id.txt_account_name);
            txtAccountBalance = itemView.findViewById(R.id.txt_account_balance);
            btnEditAccount = itemView.findViewById(R.id.btn_edit);
            btnDeleteAccount = itemView.findViewById(R.id.btn_delete);

        }
    }
}