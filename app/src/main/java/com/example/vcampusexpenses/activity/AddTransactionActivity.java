package com.example.vcampusexpenses.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.AccountRadioAdapter;
import com.example.vcampusexpenses.adapters.CategoryRadioAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.BudgetService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private CategoryService categoryService;
    private TransactionService transactionService;
    private ImageButton btnClose, btnSubmit;
    private LinearLayout llIncome, llOutcome, llTransfer, llDatePicker, llSelectCategory, llSelectAccount, llSelectToAccount, llDescription;
    private TextView txtDate, txtSelectedCategory, txtSelectedAccount, txtSelectedToAccount, txtSelectToAccount;
    private EditText edtAmount, edtDescription;
    private String transactionType;
    private boolean categorySelected, accountSelected, toAccountSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addtransaction);

        btnClose = findViewById(R.id.btn_close);
        btnSubmit = findViewById(R.id.btn_submit);
        llIncome = findViewById(R.id.ll_income);
        llOutcome = findViewById(R.id.ll_outcome);
        llTransfer = findViewById(R.id.ll_transfer);
        llDatePicker = findViewById(R.id.ll_datePicker);
        llSelectCategory = findViewById(R.id.ll_select_category);
        llSelectAccount = findViewById(R.id.ll_select_account);
        llSelectToAccount = findViewById(R.id.ll_select_to_account);
        llDescription = findViewById(R.id.ll_description);
        txtDate = findViewById(R.id.txt_date);
        txtSelectedCategory = findViewById(R.id.txt_selected_category);
        txtSelectedAccount = findViewById(R.id.txt_selected_account);
        //Text bên trong khung chọn acccount tới cho transfer
        txtSelectedToAccount = findViewById(R.id.txt_selected_to_account);
        //Text bên ngoài khung chọn acccount tới cho transfer
        txtSelectToAccount = findViewById(R.id.txt_select_to_account);
        edtAmount = findViewById(R.id.edt_amount);
        edtDescription = findViewById(R.id.edt_description);

        categorySelected = false;
        accountSelected = false;
        toAccountSelected = false;

        //service + UserDataManager
        sessionManager = new SessionManager(this);
        dataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
        accountService = new AccountService(dataManager);
        BudgetService budgetService = new BudgetService(dataManager);
        categoryService = new CategoryService(dataManager);
        transactionService = new TransactionService(dataManager, accountService, budgetService);

        loadDate();
        chooseDate();
        setCategorySelection();
        showCategoryDialog();
        showAccountDialog();
        showToAccountDialog();
        submitTransaction();
        close();
    }

    private void submitTransaction() {
        btnSubmit.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Submitting transaction, type: " + transactionType);
            if (transactionType == null) {
                Log.w("AddTransactionActivity", "Transaction type not selected");
                DisplayToast.Display(this, "Please select a transaction type.");
                return;
            }
            if (edtAmount.getText().toString().isEmpty()) {
                Log.w("AddTransactionActivity", "Amount is empty");
                DisplayToast.Display(this, "Please enter an amount.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(edtAmount.getText().toString());
                if (amount <= 0) {
                    Log.w("AddTransactionActivity", "Amount must be greater than zero");
                    DisplayToast.Display(this, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                Log.e("AddTransactionActivity", "Invalid amount format: " + edtAmount.getText().toString());
                DisplayToast.Display(this, "Invalid amount format.");
                return;
            }
            String date = txtDate.getText().toString();
            String description = edtDescription.getText().toString();

            if (transactionType.equals("transfer")) {
                if (!accountSelected || !toAccountSelected) {
                    Log.w("AddTransactionActivity", "Account or toAccount not selected for transfer");
                    DisplayToast.Display(this, "Please select from and to accounts.");
                    return;
                }
                String fromAccountId = accountService.getAccountId(txtSelectedAccount.getText().toString());
                String toAccountId = accountService.getAccountId(txtSelectedToAccount.getText().toString());
                if (fromAccountId == null || toAccountId == null) {
                    Log.w("AddTransactionActivity", "Invalid account selection");
                    DisplayToast.Display(this, "Invalid account selection.");
                    return;
                }
                if (fromAccountId.equals(toAccountId)) {
                    Log.w("AddTransactionActivity", "From and to accounts are the same");
                    DisplayToast.Display(this, "From and to accounts cannot be the same.");
                    return;
                }
                Transaction transaction = new Transaction("TRANSFER", fromAccountId, toAccountId, amount, date, description);
                transactionService.addTransaction(transaction);
                dataManager.saveData(); // Lưu dữ liệu sau khi thêm giao dịch
                Log.d("AddTransactionActivity", "Transfer transaction added: " + description);
                DisplayToast.Display(this, "Transfer added successfully.");
                resetForm(); // Đặt lại form thay vì đóng activity
            } else {
                if (!categorySelected || !accountSelected) {
                    Log.w("AddTransactionActivity", "Category or account not selected");
                    DisplayToast.Display(this, "Please select a category and account.");
                    return;
                }
                String accountId = accountService.getAccountId(txtSelectedAccount.getText().toString());
                String categoryId = categoryService.getCategoryId(txtSelectedCategory.getText().toString());
                if (accountId == null || categoryId == null) {
                    Log.w("AddTransactionActivity", "Invalid account or category selection");
                    DisplayToast.Display(this, "Invalid account or category selection.");
                    return;
                }
                Transaction transaction = new Transaction(transactionType.toUpperCase(), accountId, categoryId, amount, date, description);
                transactionService.addTransaction(transaction);
                dataManager.saveData(); // Lưu dữ liệu sau khi thêm giao dịch
                Log.d("AddTransactionActivity", transactionType + " transaction added: " + description);
                DisplayToast.Display(this, transactionType + " added successfully.");
                resetForm(); // Đặt lại form thay vì đóng activity
            }
        });
    }

    private void resetForm() {
        edtAmount.setText("");
        edtDescription.setText("");
        txtDate.setText(getString(R.string.year_month_day));
        txtSelectedCategory.setText(getString(R.string.select_category));
        txtSelectedAccount.setText(getString(R.string.select_account));
        txtSelectedToAccount.setText("Select To Account");
        categorySelected = false;
        accountSelected = false;
        toAccountSelected = false;
        transactionType = null;

        llIncome.setBackgroundResource(R.drawable.border);
        llOutcome.setBackgroundResource(R.drawable.border);
        llTransfer.setBackgroundResource(R.drawable.border);
        llSelectCategory.setVisibility(View.VISIBLE);
        llSelectAccount.setVisibility(View.VISIBLE);
        llSelectToAccount.setVisibility(View.GONE);
        txtSelectToAccount.setVisibility(View.GONE);

        loadDate();
    }

    private void showAccountDialog() {
        llSelectAccount.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Showing account selection dialog");
            List<Account> accounts = accountService.getListAccounts();
            if (accounts == null || accounts.isEmpty()) {
                Log.w("AddTransactionActivity", "No accounts available");
                DisplayToast.Display(this, "No accounts available.");
                return;
            }

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_item_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();

            AccountRadioAdapter adapter = new AccountRadioAdapter(accounts, selectedAccount -> {
                txtSelectedAccount.setText(selectedAccount.getName());
                accountSelected = true;
                alertDialog.dismiss();
                Log.d("AddTransactionActivity", "Selected account: " + selectedAccount.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void showToAccountDialog() {
        llSelectToAccount.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Showing to-account selection dialog");
            List<Account> accounts = accountService.getListAccounts();
            if (accounts == null || accounts.isEmpty()) {
                Log.w("AddTransactionActivity", "No accounts available");
                DisplayToast.Display(this, "No accounts available.");
                return;
            }

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_item_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();

            AccountRadioAdapter adapter = new AccountRadioAdapter(accounts, selectedAccount -> {
                txtSelectedToAccount.setText(selectedAccount.getName());
                toAccountSelected = true;
                alertDialog.dismiss();
                Log.d("AddTransactionActivity", "Selected to-account: " + selectedAccount.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void showCategoryDialog() {
        llSelectCategory.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Showing category selection dialog");
            List<Category> categories = categoryService.getListCategories();
            if (categories == null || categories.isEmpty()) {
                Log.w("AddTransactionActivity", "No categories available");
                DisplayToast.Display(this, "No categories available.");
                return;
            }

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_item_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dialogView).create();

            CategoryRadioAdapter adapter = new CategoryRadioAdapter(categories, selectedCategory -> {
                txtSelectedCategory.setText(selectedCategory.getName());
                categorySelected = true;
                alertDialog.dismiss();
                Log.d("AddTransactionActivity", "Selected category: " + selectedCategory.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void setCategorySelection() {
        setCategorySelection(null);
        llIncome.setOnClickListener(v -> setCategorySelection("income"));
        llOutcome.setOnClickListener(v -> setCategorySelection("outcome"));
        llTransfer.setOnClickListener(v -> setCategorySelection("transfer"));
    }

    private void setCategorySelection(String selected) {
        Log.d("AddTransactionActivity", "Setting transaction type: " + selected);
        llIncome.setBackgroundResource(R.drawable.border);
        llOutcome.setBackgroundResource(R.drawable.border);
        llTransfer.setBackgroundResource(R.drawable.border);

        if ("income".equals(selected)) {
            llIncome.setBackgroundResource(R.drawable.bg_selected_income);
            transactionType = "income";
            llSelectCategory.setVisibility(View.VISIBLE);
            llSelectAccount.setVisibility(View.VISIBLE);
            llSelectToAccount.setVisibility(View.GONE);
            txtSelectToAccount.setVisibility(View.GONE);
            categorySelected = false;
            accountSelected = false;
            toAccountSelected = false;
            txtSelectedCategory.setText(getString(R.string.select_category));
            txtSelectedAccount.setText(getString(R.string.select_account));
            txtSelectedToAccount.setText("Select To Account");
        } else if ("outcome".equals(selected)) {
            llOutcome.setBackgroundResource(R.drawable.bg_selected_outcome);
            transactionType = "outcome";
            llSelectCategory.setVisibility(View.VISIBLE);
            llSelectAccount.setVisibility(View.VISIBLE);
            llSelectToAccount.setVisibility(View.GONE);
            txtSelectToAccount.setVisibility(View.GONE);
            categorySelected = false;
            accountSelected = false;
            toAccountSelected = false;
            txtSelectedCategory.setText(getString(R.string.select_category));
            txtSelectedAccount.setText(getString(R.string.select_account));
            txtSelectedToAccount.setText("Select To Account");
        } else if ("transfer".equals(selected)) {
            llTransfer.setBackgroundResource(R.drawable.bg_selected_transfer);
            transactionType = "transfer";
            llSelectCategory.setVisibility(View.GONE);
            llSelectAccount.setVisibility(View.VISIBLE);
            llSelectToAccount.setVisibility(View.VISIBLE);
            txtSelectToAccount.setVisibility(View.VISIBLE);
            categorySelected = false;
            accountSelected = false;
            toAccountSelected = false;
            txtSelectedCategory.setText(getString(R.string.select_category));
            txtSelectedAccount.setText(getString(R.string.select_account));
            txtSelectedToAccount.setText("Select To Account");
        }
    }

    private void chooseDate() {
        llDatePicker.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Opening date picker");
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d",
                                selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        txtDate.setText(selectedDate);
                        Log.d("AddTransactionActivity", "Selected date: " + selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void close() {
        btnClose.setOnClickListener(v -> {
            Log.d("AddTransactionActivity", "Closing activity");
            finish();
        });
    }

    private void loadDate() {
        Log.d("AddTransactionActivity", "Loading current date");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());
        txtDate.setText(currentDate);
    }
}