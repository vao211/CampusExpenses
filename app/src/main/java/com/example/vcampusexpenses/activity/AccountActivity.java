package com.example.vcampusexpenses.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.AccountAdapter;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.List;

public class AccountActivity extends AppCompatActivity implements AccountAdapter.OnAccountClickListener{

    private RecyclerView rvAllAccounts;
    private AccountAdapter accountAdapter;
    private AccountService accountService;
    private SessionManager sessionManager;
    private UserDataManager userDataManager;
    private ImageButton btnAdd, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        btnAdd = findViewById(R.id.btn_add);
        btnClose = findViewById(R.id.btn_close);

        sessionManager = new SessionManager(this);
        userDataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountService = new AccountService(userDataManager);
        List<Account> accountList = accountService.getListAccounts();

        rvAllAccounts = findViewById(R.id.rv_all_accounts);
        rvAllAccounts.setLayoutManager(new LinearLayoutManager(this));
        accountAdapter = new AccountAdapter(accountList, accountService, this);
        rvAllAccounts.setAdapter(accountAdapter);
        onAddAccountClick();
        close();
    }

    private void close(){
        btnClose.setOnClickListener(v -> {
            finish();
        });
    }
    public void onAddAccountClick() {
        btnAdd.setOnClickListener(v -> {
            showAddAccountDialog();
        });
    }
    @Override
    public void onEditAccount(String accountId) {
        showEditAccountDialog(accountId);
    }

    @Override
    public void onDeleteAccount(String accountId) {
        accountService.deleteAccount(accountId);
        userDataManager.saveData();
        accountAdapter.updateAccounts(accountService.getListAccounts());
    }

    private void showEditAccountDialog(String accountId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_account, null);
        builder.setView(dialogView);

        EditText edtAccountName = dialogView.findViewById(R.id.edt_account_name);
        EditText edtBalance = dialogView.findViewById(R.id.edt_initial_balance);
        Account account = accountService.getAccount(accountId);
        if (account != null) {
            edtAccountName.setText(account.getName());
            edtBalance.setText(String.valueOf(account.getBalance()));
        }
        builder.setTitle("Edit Account")
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = edtAccountName.getText().toString().trim();
                    String balanceStr = edtBalance.getText().toString().trim();
                    double balance;
                    try {
                        balance = balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);
                        if (balance < 0) {
                            throw new NumberFormatException("Negative balance");
                        }
                        accountService.updateAccount(accountId, name, balance);
                        userDataManager.saveData();
                        accountAdapter.updateAccounts(accountService.getListAccounts());
                    } catch (NumberFormatException e) {
                        DisplayToast.Display(this, "Invalid balance");
                    }
                }).setNegativeButton("Cancel", null);
        builder.create().show();
    }
    private void showAddAccountDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_account, null);
        builder.setView(dialogView);

        EditText etAccountName = dialogView.findViewById(R.id.edt_account_name);
        EditText etInitialBalance = dialogView.findViewById(R.id.edt_initial_balance);

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
                        Account account = new Account(name, balance);
                        if (accountService.addAccount(account)) {
                            userDataManager.saveData();
                            accountAdapter.updateAccounts(accountService.getListAccounts());
                        }
                    } catch (NumberFormatException e) {
                        DisplayToast.Display(this, "Invalid balance");
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