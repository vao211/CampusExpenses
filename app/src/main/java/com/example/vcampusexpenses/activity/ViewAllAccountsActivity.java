package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapter.AccountAdapter;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.datamanager.UserDataManager;

import java.util.ArrayList;
import java.util.List;

public class ViewAllAccountsActivity extends AppCompatActivity {

    private RecyclerView rvAllAccounts;
    private AccountAdapter accountAdapter;
    private AccountService accountService;
    private ImageButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_view_all_accounts);
            Log.d("ViewAllAccountsActivity", "Layout set successfully");

            // Initialize SessionManager and AccountService
            SessionManager sessionManager = new SessionManager(this);
            UserDataManager dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
            accountService = new AccountService(dataManager);

            // Initialize RecyclerView
            rvAllAccounts = findViewById(R.id.rv_all_accounts);
            if (rvAllAccounts == null) {
                Log.e("ViewAllAccountsActivity", "rv_all_accounts not found in layout");
                return;
            }
            rvAllAccounts.setLayoutManager(new LinearLayoutManager(this));

            // Initialize Adapter
            List<Account> accountList = accountService.getListAccounts();
            if (accountList == null) {
                accountList = new ArrayList<>();
                Log.w("ViewAllAccountsActivity", "accountList is null, initialized empty");
            }
            accountAdapter = new AccountAdapter(this, accountList, (account, position) -> {
                // Handle account click if needed
            }, false); // Use list view
            rvAllAccounts.setAdapter(accountAdapter);

            // Initialize Add Button
            btnAdd = findViewById(R.id.btn_add);
            if (btnAdd != null) {
                btnAdd.setOnClickListener(v -> showAddAccountDialog());
            } else {
                Log.e("ViewAllAccountsActivity", "btn_add not found in layout");
            }
        } catch (Exception e) {
            Log.e("ViewAllAccountsActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddAccountDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_account, null);
        builder.setView(dialogView);

        EditText etAccountName = dialogView.findViewById(R.id.et_account_name);
        EditText etInitialBalance = dialogView.findViewById(R.id.et_initial_balance);

        builder.setTitle("Add Account")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etAccountName.getText().toString().trim();
                    String balanceStr = etInitialBalance.getText().toString().trim();
                    double balance;
                    try {
                        balance = balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);
                        if (balance < 0) {
                            throw new NumberFormatException("Negative balance");
                        }
                    } catch (NumberFormatException e) {
                        // Toast is handled by AccountService
                        return;
                    }

                    Account account = new Account(name, balance);
                    if (accountService.addAccount(account)) {
                        accountAdapter.updateAccounts(accountService.getListAccounts());
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh account list
        List<Account> accountList = accountService.getListAccounts();
        if (accountList != null) {
            accountAdapter.updateAccounts(accountList);
        }
    }
}