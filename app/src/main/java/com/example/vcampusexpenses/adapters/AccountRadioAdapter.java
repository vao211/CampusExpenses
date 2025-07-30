package com.example.vcampusexpenses.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Account;

import java.util.List;

public class AccountRadioAdapter extends RecyclerView.Adapter<AccountRadioAdapter.ViewHolder> {
    private final List<Account> accountList;
    private int selectedPosition = -1;
     private final OnAccountSelectedListener listener;

     public interface OnAccountSelectedListener {
         void onAccountSelected(Account selectedAccount);
     }
     public class ViewHolder extends RecyclerView.ViewHolder {
         RadioButton radioButton;
         TextView accountName, accountBalance;
         public ViewHolder(View itemView) {
             super(itemView);
             radioButton = itemView.findViewById(R.id.radio_button);
             accountName = itemView.findViewById(R.id.txt_account_name);
             accountBalance = itemView.findViewById(R.id.txt_account_balance);
         }

     }
    public AccountRadioAdapter(List<Account> accountList, OnAccountSelectedListener listener) {
        this.accountList = accountList;
        this.listener = listener;
    }

     @Override
     public AccountRadioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account_radio,
                 parent,false);
         return new ViewHolder(view);
     }

     @Override
     public void onBindViewHolder(AccountRadioAdapter.ViewHolder holder, int position) {
         Account account = accountList.get(position);
         holder.accountName.setText(account.getName());
         holder.accountBalance.setText(String.valueOf(account.getBalance()));
         holder.radioButton.setChecked(position == selectedPosition);

         holder.itemView.setOnClickListener(v -> {
             selectedPosition = holder.getAdapterPosition();
             notifyDataSetChanged();
             listener.onAccountSelected(account);
         });
         holder.radioButton.setOnClickListener(v -> {
             selectedPosition = holder.getAdapterPosition();
             notifyDataSetChanged();
             listener.onAccountSelected(account);
         });
     }

     @Override
     public int getItemCount() {
         return accountList.size();
         }
}
