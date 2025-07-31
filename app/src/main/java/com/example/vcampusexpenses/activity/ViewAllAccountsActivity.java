package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_accounts);

        // Initialize SessionManager and AccountService
        SessionManager sessionManager = new SessionManager(this);
        UserDataManager dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountService = new AccountService(dataManager);

        // Initialize RecyclerView
        rvAllAccounts = findViewById(R.id.rv_all_accounts);
        rvAllAccounts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter
        List<Account> accountList = accountService.getListAccounts();
        if (accountList == null) {
            accountList = new ArrayList<>();
        }
        accountAdapter = new AccountAdapter(this, accountList, (account, position) -> {
            // Handle account click if needed
        }, false); // Use list view
        rvAllAccounts.setAdapter(accountAdapter);
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
