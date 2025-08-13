package com.example.vcampusexpenses.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.AccountRadioAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetAccountActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private AccountBudgetService accountBudgetService;
    private ImageButton btnClose, btnSubmit;
    private LinearLayout llStartDatePicker, llEndDatePicker, llSelectAccount;
    private TextView txtStartDate, txtEndDate, txtSelectedAccount, txtTitle;
    private EditText edtAmount, edtRemain;
    private String budgetId;
    private boolean accountSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Initialize UI elements
        btnClose = findViewById(R.id.btn_close);
        btnSubmit = findViewById(R.id.btn_submit);
        llStartDatePicker = findViewById(R.id.ll_StartDatePicker);
        llEndDatePicker = findViewById(R.id.ll_EndDatePicker);
        llSelectAccount = findViewById(R.id.ll_select_account);
        txtTitle = findViewById(R.id.txt_title);
        txtStartDate = findViewById(R.id.txt_start_date);
        txtEndDate = findViewById(R.id.txt_end_date);
        txtSelectedAccount = findViewById(R.id.txt_selected_account);
        edtAmount = findViewById(R.id.edt_amount);
        edtRemain = findViewById(R.id.edt_remain);

        // Initialize services
        sessionManager = new SessionManager(this);
        dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountService = new AccountService(dataManager);
        accountBudgetService = new AccountBudgetService(dataManager);

        accountSelected = false;

        // Set up functionality
        loadDates();
        checkIsCreateOrEdit();
        chooseStartDate();
        chooseEndDate();
        showAccountDialog();
        submitBudget();
        close();
    }

    private void checkIsCreateOrEdit() {
        Log.d("BudgetAccountActivity", "Checking if it's create or edit");
        budgetId = getIntent().getStringExtra("budgetId");
        if (budgetId != null) {
            loadBudget(budgetId);
            txtTitle.setText(R.string.edit_budget);
            Log.d("BudgetAccountActivity", "Edit mode");
        } else {
            Log.d("BudgetAccountActivity", "Create mode");
        }
    }

    private void loadBudget(String budgetId) {
        Log.d("BudgetAccountActivity", "Loading accountBudget: " + budgetId);
        AccountBudget accountBudget = accountBudgetService.getBudget(budgetId);
        if (accountBudget == null) {
            Log.w("BudgetAccountActivity", "AccountBudget not found: " + budgetId);
            DisplayToast.Display(this, "Error on load accountBudget");
            return;
        }

        txtStartDate.setText(accountBudget.getStartDate());
        txtEndDate.setText(accountBudget.getEndDate());
        edtAmount.setText(String.valueOf(accountBudget.getTotalAmount()));
        edtRemain.setText(String.valueOf(accountBudget.getRemainingAmount()));

        List<String> accountIds = accountBudget.getListAccountIds();
        if (!accountIds.isEmpty()) {
            Account account = accountService.getAccount(accountIds.get(0));
            if (account != null) {
                txtSelectedAccount.setText(account.getName());
                accountSelected = true;
            } else {
                Log.w("BudgetAccountActivity", "Invalid account in accountBudget");
            }
        }
    }

    private void submitBudget() {
        btnSubmit.setOnClickListener(v -> {
            Log.d("BudgetAccountActivity", "Submitting accountBudget");
            if (!accountSelected) {
                Log.w("BudgetAccountActivity", "Account not selected");
                DisplayToast.Display(this, "Please select an account.");
                return;
            }
            if (edtAmount.getText().toString().isEmpty()) {
                Log.w("BudgetAccountActivity", "Amount is empty");
                DisplayToast.Display(this, "Please enter an amount.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(edtAmount.getText().toString());
                if (amount <= 0) {
                    Log.w("BudgetAccountActivity", "Amount must be greater than zero");
                    DisplayToast.Display(this, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                Log.e("BudgetAccountActivity", "Invalid amount format: " + edtAmount.getText().toString());
                DisplayToast.Display(this, "Invalid amount format.");
                return;
            }
            String startDate = txtStartDate.getText().toString();
            String endDate = txtEndDate.getText().toString();
            String accountId = accountService.getAccountId(txtSelectedAccount.getText().toString());
            if (accountId == null) {
                Log.w("BudgetAccountActivity", "Invalid account selection");
                DisplayToast.Display(this, "Invalid account selection.");
                return;
            }
            if (startDate.equals(getString(R.string.start_date)) || endDate.equals(getString(R.string.end_date))) {
                Log.w("BudgetAccountActivity", "Invalid date selection");
                DisplayToast.Display(this, "Please select valid start and end dates.");
                return;
            }
            //check end datê
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date start = sdf.parse(startDate);
                Date end = sdf.parse(endDate);
                if (end.before(start)) {
                    Log.w("BudgetAccountActivity", "End date is before start date");
                    DisplayToast.Display(this, "End date cannot be before start date.");
                    return;
                }
            } catch (ParseException e) {
                Log.e("BudgetAccountActivity", "Date parsing error: " + e.getMessage());
                DisplayToast.Display(this, "Invalid date format.");
                return;
            }
            AccountBudget accountBudget = new AccountBudget(budgetId, "AccountBudget", amount, amount, startDate, endDate);
            accountBudget.addAccount(accountId);

            if (budgetId == null) {
                accountBudgetService.addBudget(accountBudget);
                dataManager.saveData();
                Log.d("BudgetAccountActivity", "AccountBudget added");
                DisplayToast.Display(this, "AccountBudget added successfully.");
                resetForm();
            } else {
                accountBudgetService.updateBudget(budgetId, accountBudget, true);
                dataManager.saveData();
                Log.d("BudgetAccountActivity", "AccountBudget updated");
                DisplayToast.Display(this, "AccountBudget updated successfully.");
                finish();
            }
        });
    }

    private void resetForm() {
        edtAmount.setText("");
        edtRemain.setText("");
        txtStartDate.setText(getString(R.string.start_date));
        txtEndDate.setText(getString(R.string.end_date));
        txtSelectedAccount.setText(getString(R.string.select_account));
        accountSelected = false;
        loadDates();
    }

    private void showAccountDialog() {
        llSelectAccount.setOnClickListener(v -> {
            Log.d("BudgetAccountActivity", "Showing account selection dialog");
            List<Account> accounts = accountService.getListAccounts();
            if (accounts == null || accounts.isEmpty()) {
                Log.w("BudgetAccountActivity", "No accounts available");
                DisplayToast.Display(this, "No accounts available.");
                return;
            }

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_item_list);
            Button btnAdd = dialogView.findViewById(R.id.btn_add);
            btnAdd.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();

            AccountRadioAdapter adapter = new AccountRadioAdapter(accounts, selectedAccount -> {
                txtSelectedAccount.setText(selectedAccount.getName());
                accountSelected = true;
                alertDialog.dismiss();
                Log.d("BudgetAccountActivity", "Selected account: " + selectedAccount.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void chooseStartDate() {
        llStartDatePicker.setOnClickListener(v -> {
            Log.d("BudgetAccountActivity", "Opening start date picker");
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        txtStartDate.setText(selectedDate);
                        txtEndDate.setText(selectedDate);
                        Log.d("BudgetAccountActivity", "Selected start date: " + selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void chooseEndDate() {
        llEndDatePicker.setOnClickListener(v -> {
            Log.d("BudgetAccountActivity", "Opening end date picker");
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        txtEndDate.setText(selectedDate);
                        Log.d("BudgetAccountActivity", "Selected end date: " + selectedDate);
                    },
                    year, month, day);
            try {
                String startDateText = txtStartDate.getText().toString();
                if (!startDateText.equals(getString(R.string.start_date))) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date startDate = sdf.parse(startDateText);
                    if (startDate != null) {
                        datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                    }
                }
            } catch (ParseException e) {
                Log.e("BudgetAccountActivity", "Error parsing start date: " + e.getMessage());
            }
            datePickerDialog.show();
        });
    }

    private void close() {
        btnClose.setOnClickListener(v -> {
            Log.d("BudgetAccountActivity", "Closing activity");
            finish();
        });
    }

    private void loadDates() {
        Log.d("BudgetAccountActivity", "Loading current dates");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());
        txtStartDate.setText(currentDate);
        txtEndDate.setText(currentDate);
    }
}