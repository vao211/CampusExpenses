package com.example.vcampusexpenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.SettingService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeAccountAdapter extends RecyclerView.Adapter<HomeAccountAdapter.AccountViewHolder> {

    private static final String TAG = "HomeAccountAdapter";
    private List<Account> accountList;
    private final SettingService settingService;

    public HomeAccountAdapter(Context context, List<Account> accountList) {
        this.accountList = accountList != null ? new ArrayList<>(accountList) : new ArrayList<>();
        this.settingService = new SettingService(context);
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_card, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.bind(account, settingService);
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public void updateAccounts(List<Account> newAccountList) {
        this.accountList = newAccountList != null ? new ArrayList<>(newAccountList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtAccountName;
        private final TextView txtAccountBalance;
        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAccountName = itemView.findViewById(R.id.txt_account_name);
            txtAccountBalance = itemView.findViewById(R.id.txt_balance);
        }
        void bind(Account account, SettingService settingService) {
            txtAccountName.setText(account.getName());
            txtAccountBalance.setText(String.valueOf(account.getBalance() + " " + settingService.getCurrency()));
        }
    }
}