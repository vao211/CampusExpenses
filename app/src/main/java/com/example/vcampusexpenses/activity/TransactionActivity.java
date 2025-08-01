package com.example.vcampusexpenses.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
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

public class TransactionActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private UserDataManager dataManager;
    private AccountService accountService;
    private CategoryService categoryService;
    private TransactionService transactionService;
    private ImageButton btnClose, btnSubmit;
    private LinearLayout llIncome, llOutcome, llTransfer, llDatePicker, llSelectCategory, llSelectAccount, llSelectToAccount;
    private TextView txtDate, txtSelectedCategory, txtSelectedAccount, txtSelectedToAccount, txtSelectToAccount, txtTitle;
    private EditText edtAmount, edtDescription;
    private String transactionType;
    private boolean categorySelected, accountSelected, toAccountSelected;
    String transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        btnClose = findViewById(R.id.btn_close);
        btnSubmit = findViewById(R.id.btn_submit);
        llIncome = findViewById(R.id.ll_income);
        llOutcome = findViewById(R.id.ll_outcome);
        llTransfer = findViewById(R.id.ll_transfer);
        llDatePicker = findViewById(R.id.ll_datePicker);
        llSelectCategory = findViewById(R.id.ll_select_category);
        llSelectAccount = findViewById(R.id.ll_select_account);
        llSelectToAccount = findViewById(R.id.ll_select_to_account);
        txtTitle = findViewById(R.id.txt_title);
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
        setTransactionTypeSelection();
        loadDate();
        checkIsCreateOrEdit();
        chooseDate();
        showCategoryDialog();
        showAccountDialog();
        showToAccountDialog();
        submitTransaction();
        close();
    }
    private void checkIsCreateOrEdit() {
        Log.d("TransactionActivity", "Checking if it's a create or edit ");
        transactionId = getIntent().getStringExtra("transactionId");
        if (transactionId != null) {
            loadTransaction(transactionId);
            Log.d("TransactionActivity", "Edit ");
            txtTitle.setText(R.string.edit_transaction);
        }
        else{
            Log.d("TransactionActivity", "Create ");
        }
    }
    private void loadTransaction(String transactionId) {
        Log.d("TransactionActivity", "Load transaction: " + transactionId);
        Transaction transaction = transactionService.getTransaction(transactionId);
        if (transaction == null) {
            Log.w("TransactionActivity", "Transaction not found: " + transactionId);
            DisplayToast.Display(this, "Error on load transaction");
            return;
        }
        transactionType = transaction.getType().toLowerCase();
        Log.d("TransactionActivity", "Transaction type: " + transactionType);
        setTransactionTypeSelection(transactionType);

        txtDate.setText(transaction.getDate());
        edtAmount.setText(String.valueOf(transaction.getAmount()));
        edtDescription.setText(transaction.getDescription());

        if(transactionType.equals("transfer")){
            Account fromAccount = accountService.getAccount(transaction.getFromAccountId());
            Account toAccount = accountService.getAccount(transaction.getToAccountId());
            if (fromAccount == null || toAccount == null) {
                Log.w("TransactionActivity: loadTransaction", "Invalid account ");
                return;
            }
            txtSelectedAccount.setText(fromAccount.getName());
            txtSelectedToAccount.setText(toAccount.getName());
            accountSelected = true;
            toAccountSelected = true;
        }
        else{
            Account account = accountService.getAccount(transaction.getAccountId());
            Category category = categoryService.getCategory(transaction.getCategoryId());
            if (account == null || category == null) {
                Log.w("TransactionActivity: loadTransaction", "Invalid account or category");
                return;
            }
            txtSelectedAccount.setText(account.getName());
            txtSelectedCategory.setText(category.getName());
            accountSelected = true;
            categorySelected = true;
        }
    }

    private void submitTransaction() {
        btnSubmit.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Submitting transaction, type: " + transactionType);
            if (transactionType == null) {
                Log.w("TransactionActivity", "Transaction type not selected");
                DisplayToast.Display(this, "Please select a transaction type.");
                return;
            }
            if (edtAmount.getText().toString().isEmpty()) {
                Log.w("TransactionActivity", "Amount is empty");
                DisplayToast.Display(this, "Please enter an amount.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(edtAmount.getText().toString());
                if (amount <= 0) {
                    Log.w("TransactionActivity", "Amount must be greater than zero");
                    DisplayToast.Display(this, "Amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                Log.e("TransactionActivity", "Invalid amount format: " + edtAmount.getText().toString());
                DisplayToast.Display(this, "Invalid amount format.");
                return;
            }
            String date = txtDate.getText().toString();
            String description = edtDescription.getText().toString();

            if (transactionType.equals("transfer")) {
                if (!accountSelected || !toAccountSelected) {
                    Log.w("TransactionActivity", "Account or toAccount not selected for transfer");
                    DisplayToast.Display(this, "Please select from and to accounts.");
                    return;
                }
                String fromAccountId = accountService.getAccountId(txtSelectedAccount.getText().toString());
                String toAccountId = accountService.getAccountId(txtSelectedToAccount.getText().toString());
                if (fromAccountId == null || toAccountId == null) {
                    Log.w("TransactionActivity", "Invalid account selection");
                    DisplayToast.Display(this, "Invalid account selection.");
                    return;
                }
                if (fromAccountId.equals(toAccountId)) {
                    Log.w("TransactionActivity", "From and to accounts are the same");
                    DisplayToast.Display(this, "From and to accounts cannot be the same.");
                    return;
                }
                Transaction transaction = new Transaction(fromAccountId, toAccountId, amount, date, description);
                //check có phải update không
                if(transactionId == null){
                    transactionService.addTransaction(transaction);
                    dataManager.saveData();
                    Log.d("TransactionActivity", "Transfer transaction added: " + description);
                    DisplayToast.Display(this, "Transfer added successfully.");
                    resetForm();
                }
                else{
                    transactionService.updateTransaction(transactionId, transaction);
                    dataManager.saveData();
                    Log.d("TransactionActivity", "Transfer transaction updated: " + description);
                    DisplayToast.Display(this, "Transfer updated successfully.");
                    finish();
                }
            }
            else {
                if (!categorySelected || !accountSelected) {
                    Log.w("TransactionActivity", "Category or account not selected");
                    DisplayToast.Display(this, "Please select a category and account.");
                    return;
                }
                String accountId = accountService.getAccountId(txtSelectedAccount.getText().toString());
                String categoryId = categoryService.getCategoryId(txtSelectedCategory.getText().toString());
                if (accountId == null || categoryId == null) {
                    Log.w("TransactionActivity", "Invalid account or category selection");
                    DisplayToast.Display(this, "Invalid account or category selection.");
                    return;
                }
                Transaction transaction = new Transaction(transactionType.toUpperCase(), accountId, categoryId, amount, date, description);
                //check có phải update không
                if(transactionId == null){
                    transactionService.addTransaction(transaction);
                    dataManager.saveData();
                    Log.d("TransactionActivity", transactionType + " transaction added: " + description);
                    DisplayToast.Display(this, transactionType + " added successfully.");
                    resetForm();
                }
                else{
                    transactionService.updateTransaction(transactionId, transaction);
                    dataManager.saveData();
                    Log.d("TransactionActivity", transactionType + " transaction updated: " + description);
                    DisplayToast.Display(this, transactionType + " updated successfully.");
                    finish();
                }
            }
        });
    }

    private void resetForm() {
        edtAmount.setText("");
        edtDescription.setText("");
        txtDate.setText(getString(R.string.year_month_day));
        txtSelectedCategory.setText(getString(R.string.select_category));
        txtSelectedAccount.setText(getString(R.string.select_account));
        txtSelectedToAccount.setText(R.string.select_to_account);
        categorySelected = false;
        accountSelected = false;
        toAccountSelected = false;
        llSelectCategory.setVisibility(View.VISIBLE);
        llSelectAccount.setVisibility(View.VISIBLE);
        llSelectToAccount.setVisibility(View.GONE);
        txtSelectToAccount.setVisibility(View.GONE);
        setTransactionTypeSelection(transactionType);
        loadDate();
    }

    private void showAccountDialog() {
        llSelectAccount.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Showing account selection dialog");
            List<Account> accounts = accountService.getListAccounts();
            if (accounts == null || accounts.isEmpty()) {
                Log.w("TransactionActivity", "No accounts available");
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
                Log.d("TransactionActivity", "Selected account: " + selectedAccount.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void showToAccountDialog() {
        llSelectToAccount.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Showing to-account selection dialog");
            List<Account> accounts = accountService.getListAccounts();
            if (accounts == null || accounts.isEmpty()) {
                Log.w("TransactionActivity", "No accounts available");
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
                txtSelectedToAccount.setText(selectedAccount.getName());
                toAccountSelected = true;
                alertDialog.dismiss();
                Log.d("TransactionActivity", "Selected to-account: " + selectedAccount.getName());
            });
            recyclerView.setAdapter(adapter);
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }

    private void showCategoryDialog() {
        llSelectCategory.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Showing category selection dialog");
            List<Category> categories = categoryService.getListCategories();

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_item, null);
            RecyclerView recyclerView = dialogView.findViewById(R.id.rv_item_list);
            Button btnAdd = dialogView.findViewById(R.id.btn_add);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            CategoryRadioAdapter adapter = new CategoryRadioAdapter(categories, selectedCategory -> {
                txtSelectedCategory.setText(selectedCategory.getName());
                categorySelected = true;
                alertDialog.dismiss();
                Log.d("TransactionActivity", "Selected category: " + selectedCategory.getName());
            });
            recyclerView.setAdapter(adapter);

            if (categories == null || categories.isEmpty()) {
                Log.w("TransactionActivity", "No categories available");
                txtSelectedCategory.setText("No categories available");
                txtSelectedCategory.setTextColor(Color.RED);
                return;
            }

            btnAdd.setOnClickListener(v1 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add Category");

                final EditText input = new EditText(this);
                input.setHint("Enter Name");
                builder.setView(input);

                builder.setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (categoryName.isEmpty()) {
                        DisplayToast.Display(this, "Name cannot be empty");
                        return;
                    }
                    Category newCategory = new Category(null, categoryName);
                    categoryService.addCategory(newCategory);
                    dataManager.saveData();
                    Log.d("TransactionActivity", "Category added: " + categoryName);

                    //refresh
                    List<Category> updatedCategories = categoryService.getListCategories();
                    adapter.updateCategories(updatedCategories);
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            });

            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            alertDialog.show();
        });
    }
    private void setTransactionTypeSelection() {
        setTransactionTypeSelection(null);
        llIncome.setOnClickListener(v -> setTransactionTypeSelection("income"));
        llOutcome.setOnClickListener(v -> setTransactionTypeSelection("outcome"));
        llTransfer.setOnClickListener(v -> setTransactionTypeSelection("transfer"));
    }

    private void setTransactionTypeSelection(String selected) {
        Log.d("TransactionActivity", "Setting transaction type: " + selected);
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
            if (transactionId == null) {
                categorySelected = false;
                accountSelected = false;
                toAccountSelected = false;
                txtSelectedCategory.setText(getString(R.string.select_category));
                txtSelectedAccount.setText(getString(R.string.select_account));
                txtSelectedToAccount.setText("Select To Account");
            }
        } else if ("outcome".equals(selected)) {
            llOutcome.setBackgroundResource(R.drawable.bg_selected_outcome);
            transactionType = "outcome";
            llSelectCategory.setVisibility(View.VISIBLE);
            llSelectAccount.setVisibility(View.VISIBLE);
            llSelectToAccount.setVisibility(View.GONE);
            txtSelectToAccount.setVisibility(View.GONE);
            if (transactionId == null) {
                categorySelected = false;
                accountSelected = false;
                toAccountSelected = false;
                txtSelectedCategory.setText(getString(R.string.select_category));
                txtSelectedAccount.setText(getString(R.string.select_account));
                txtSelectedToAccount.setText("Select To Account");
            }
        } else if ("transfer".equals(selected)) {
            llTransfer.setBackgroundResource(R.drawable.bg_selected_transfer);
            transactionType = "transfer";
            llSelectCategory.setVisibility(View.GONE);
            llSelectAccount.setVisibility(View.VISIBLE);
            llSelectToAccount.setVisibility(View.VISIBLE);
            txtSelectToAccount.setVisibility(View.VISIBLE);
            if (transactionId == null) {
                categorySelected = false;
                accountSelected = false;
                toAccountSelected = false;
                txtSelectedCategory.setText(getString(R.string.select_category));
                txtSelectedAccount.setText(getString(R.string.select_account));
                txtSelectedToAccount.setText("Select To Account");
            }
        }
    }
    private void chooseDate() {
        llDatePicker.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Opening date picker");
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDayOfMonth);
                        txtDate.setText(selectedDate);
                        Log.d("TransactionActivity", "Selected date: " + selectedDate);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void close() {
        btnClose.setOnClickListener(v -> {
            Log.d("TransactionActivity", "Closing activity");
            finish();
        });
    }

    private void loadDate() {
        Log.d("TransactionActivity", "Loading current date");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(calendar.getTime());
        txtDate.setText(currentDate);
    }
}