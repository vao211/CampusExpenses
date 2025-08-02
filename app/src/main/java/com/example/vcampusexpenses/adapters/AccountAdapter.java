package com.example.vcampusexpenses.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.manager.SettingManager;
import com.example.vcampusexpenses.model.Account;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;
    private Context context;
    private OnAccountClickListener onAccountClickListener;
    private boolean isCardView; // True for card view (home), false for list view (all accounts)

    public interface OnAccountClickListener {
        void onAccountClick(Account account, int position);
    }

    public AccountAdapter(Context context, List<Account> accountList, OnAccountClickListener listener, boolean isCardView) {
        this.context = context;
        this.accountList = accountList;
        this.onAccountClickListener = listener;
        this.isCardView = isCardView;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isCardView ? R.layout.item_account_card : R.layout.item_account;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new AccountViewHolder(view, isCardView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        if (isCardView) {
            holder.accountName.setText(account.getName());
            // Set account icon based on name (customize logic as needed)
            if ("Cash".equalsIgnoreCase(account.getName())) {
                holder.accountIcon.setImageResource(R.drawable.ic_cash);
            } else if ("Bank".equalsIgnoreCase(account.getName())) {
                holder.accountIcon.setImageResource(R.drawable.ic_card);
            } else {
                holder.accountIcon.setImageResource(R.drawable.ic_default); // Fallback icon
            }
        } else {
            holder.accountName.setText(account.getName());
        }

        // Get currency symbol from SettingManager
        String currencySymbol = SettingManager.getCurrency(context);

        // Format the balance
        NumberFormat numberFormat = DecimalFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        String formattedBalance = numberFormat.format(account.getBalance());

        holder.accountBalance.setText(String.format("%s %s", formattedBalance, currencySymbol));

        holder.itemView.setOnClickListener(v -> {
            if (onAccountClickListener != null) {
                onAccountClickListener.onAccountClick(account, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList != null ? accountList.size() : 0;
    }

    public void updateAccounts(List<Account> newAccounts) {
        if (newAccounts != null) {
            this.accountList.clear();
            this.accountList.addAll(newAccounts);
            notifyDataSetChanged();
        }
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        ImageView accountIcon;
        TextView accountName;
        TextView accountBalance;

        public AccountViewHolder(@NonNull View itemView, boolean isCardView) {
            super(itemView);
            if (isCardView) {
                accountIcon = itemView.findViewById(R.id.iv_account_icon);
                accountName = itemView.findViewById(R.id.tv_account_name);
                accountBalance = itemView.findViewById(R.id.tv_balance);
            } else {
                accountName = itemView.findViewById(R.id.textViewAccountName);
                accountBalance = itemView.findViewById(R.id.textViewAccountBalance);
            }
            if (accountName == null || accountBalance == null) {
                Log.e("AccountAdapter", "TextView initialization failed for isCardView: " + isCardView);
            }
        }
    }
}