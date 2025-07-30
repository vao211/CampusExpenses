package com.example.vcampusexpenses.fragments; // Replace with your actual package name

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.CardAccount;
import com.example.vcampusexpenses.model.BankAccount;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView txtHomeTitle, txtDateFilterDisplay;
    private TextView txtTotalIncome, txtTotalOutcome, txtCurrentBalance;
    private ImageButton btnSetting, btnCalendar, btnPreviousPeriod, btnNextPeriod, btnAdd;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // For date filtering
    private Calendar currentMonthCalendar; // To keep track of the displayed month/period
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // If storing dates as Strings


    // --- New UI elements for Accounts ---
    private ScrollView scrollViewAccounts;

    // General Account
    private LinearLayout layoutGeneralAccountDetails; // To show/hide if no account
    private TextView txtGeneralAccountName, txtGeneralAccountBalance;

    // Card Account
    private LinearLayout layoutCardAccountDetails; // To show/hide if no card
    private TextView txtCardName, txtCardLast4, txtCardCurrentBalance, txtCardCreditLimit, txtCardAvailableBalance;

    // Bank Account
    private LinearLayout layoutBankAccountDetails; // To show/hide if no bank account
    private TextView txtBankName, txtBankAccountLast4, txtBankAccountBalance;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentMonthCalendar = Calendar.getInstance(); // Initialize to current month
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize UI elements
        txtHomeTitle = view.findViewById(R.id.txt_home_title);
        txtDateFilterDisplay = view.findViewById(R.id.txt_date_filter_display);
        btnSetting = view.findViewById(R.id.btnSetting);
        btnCalendar = view.findViewById(R.id.btn_calendar);
        btnPreviousPeriod = view.findViewById(R.id.btnPreviousPeriod);
        btnNextPeriod = view.findViewById(R.id.btnNextPeriod);
        btnAdd = view.findViewById(R.id.btn_add);

        // New TextViews for summary
        txtTotalIncome = view.findViewById(R.id.txt_total_income);
        txtTotalOutcome = view.findViewById(R.id.txt_total_outcome);
        txtCurrentBalance = view.findViewById(R.id.txt_current_balance);

        // --- Initialize New Account UI Elements ---
        scrollViewAccounts = view.findViewById(R.id.scroll_view_accounts);



        // General Account
        layoutGeneralAccountDetails = view.findViewById(R.id.layout_general_account_details);
        txtGeneralAccountName = view.findViewById(R.id.txt_general_account_name);
        txtGeneralAccountBalance = view.findViewById(R.id.txt_general_account_balance);

        // Card Account
        layoutCardAccountDetails = view.findViewById(R.id.layout_card_account_details);
        txtCardName = view.findViewById(R.id.txt_card_name);
        txtCardLast4 = view.findViewById(R.id.txt_card_last4);
        txtCardCurrentBalance = view.findViewById(R.id.txt_card_current_balance);
        txtCardCreditLimit = view.findViewById(R.id.txt_card_credit_limit);
        txtCardAvailableBalance = view.findViewById(R.id.txt_card_available_balance);

        // Bank Account
        layoutBankAccountDetails = view.findViewById(R.id.layout_bank_account_details);
        txtBankName = view.findViewById(R.id.txt_bank_name);
        txtBankAccountLast4 = view.findViewById(R.id.txt_bank_account_last4);
        txtBankAccountBalance = view.findViewById(R.id.txt_bank_account_balance);


        setupListeners();
        updateDateFilterDisplay();
        loadTransactionSummary();
        loadAccountsData(); // New method call

        return view;

    }

    private void setupListeners() {
        btnCalendar.setOnClickListener(v -> showDatePickerDialog());
        btnPreviousPeriod.setOnClickListener(v -> changePeriod(-1));
        btnNextPeriod.setOnClickListener(v -> changePeriod(1));
        txtDateFilterDisplay.setOnClickListener(v -> showDatePickerDialog()); // Allow clicking the text too
    }

    private void updateDateFilterDisplay() {
        txtDateFilterDisplay.setText(monthYearFormat.format(currentMonthCalendar.getTime()));
    }

    private void changePeriod(int monthOffset) {
        currentMonthCalendar.add(Calendar.MONTH, monthOffset);
        updateDateFilterDisplay();
        loadTransactionSummary();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    currentMonthCalendar.set(Calendar.YEAR, year);
                    currentMonthCalendar.set(Calendar.MONTH, month);
                    // We are filtering by month, so day is less relevant here unless you change logic
                    currentMonthCalendar.set(Calendar.DAY_OF_MONTH, 1); // Set to first day for consistency
                    updateDateFilterDisplay();
                    loadTransactionSummary();
                },
                currentMonthCalendar.get(Calendar.YEAR),
                currentMonthCalendar.get(Calendar.MONTH),
                currentMonthCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadTransactionSummary() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            // Optionally, navigate to login screen
            clearSummary(); // Clear previous data
            return;
        }

        // Define the start and end of the current month for querying
        Calendar startOfMonth = (Calendar) currentMonthCalendar.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Date startDate = startOfMonth.getTime();

        Calendar endOfMonth = (Calendar) currentMonthCalendar.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);
        endOfMonth.set(Calendar.MILLISECOND, 999);
        Date endDate = endOfMonth.getTime();

        Log.d(TAG, "Fetching transactions for user: " + currentUser.getUid() + " from " + startDate + " to " + endDate);

        // Assumes your "transactions" collection and documents have a "userId" field
        // and a "date" field of type Timestamp (or a String that can be compared lexicographically if you haven't changed it yet)
        db.collection("transactions")
                .whereEqualTo("userId", currentUser.getUid()) // Filter by current user
                .whereGreaterThanOrEqualTo("date", startDate) // Use the Date objects
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", Query.Direction.DESCENDING) // Optional: order by date
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalIncome = 0;
                        double totalOutcome = 0;
                        List<Transaction> transactions = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Transaction transaction = document.toObject(Transaction.class);
                                // Important: Set the ID from the document if not done in toObject()
                                transaction.setTransactionId(document.getId());
                                transactions.add(transaction);

                                if ("INCOME".equalsIgnoreCase(transaction.getType())) {
                                    totalIncome += transaction.getAmount();
                                } else if ("OUTCOME".equalsIgnoreCase(transaction.getType())) {
                                    totalOutcome += transaction.getAmount();
                                }
                                // "TRANSFER" transactions affect balance but might not be directly summed as income/outcome
                                // depending on your accounting. For this summary, we focus on Income/Outcome.

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing transaction: " + document.getId(), e);
                            }
                        }
                        updateSummaryUI(totalIncome, totalOutcome);
                        // TODO: You can pass the 'transactions' list to a RecyclerView adapter here
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(), "Error fetching transactions.", Toast.LENGTH_SHORT).show();
                        clearSummary();
                    }
                });
    }

    private void updateSummaryUI(double income, double outcome) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(); // Uses default locale

        txtTotalIncome.setText(currencyFormat.format(income));
        txtTotalOutcome.setText(currencyFormat.format(outcome));

        double balance = income - outcome;
        txtCurrentBalance.setText(currencyFormat.format(balance));

        // Set balance text color based on positive/negative
        if (balance >= 0) {
            txtCurrentBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Or your theme's positive color
        } else {
            txtCurrentBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // Or your theme's negative color
        }
    }

    private void clearSummary() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        txtTotalIncome.setText(currencyFormat.format(0));
        txtTotalOutcome.setText(currencyFormat.format(0));
        txtCurrentBalance.setText(currencyFormat.format(0));
        txtCurrentBalance.setTextColor(getResources().getColor(android.R.color.black)); // Reset color
    }


    private void loadAccountsData() {
        if (currentUser == null) {
            // Hide account sections if user is not logged in
            layoutGeneralAccountDetails.setVisibility(View.GONE);
            layoutCardAccountDetails.setVisibility(View.GONE);
            layoutBankAccountDetails.setVisibility(View.GONE);
            return;
        }
        String userId = currentUser.getUid();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        // --- Load General Account (Example: fetch first one or a specific one) ---
        // For simplicity, fetching one document. For multiple, use a collection query.
        // Assume you have a collection "accounts" and each doc has a "type":"GENERAL" field
        // or a dedicated collection like "general_accounts"
        db.collection("accounts") // Or your specific collection for general accounts
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "GENERAL") // Assuming you use a type field
                .limit(1) // Get the first one for this example
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Account account = task.getResult().getDocuments().get(0).toObject(Account.class);
                        if (account != null) {
                            txtGeneralAccountName.setText(account.getAccountName());
                            txtGeneralAccountBalance.setText(currencyFormat.format(account.getBalance()));
                            layoutGeneralAccountDetails.setVisibility(View.VISIBLE);
                        } else {
                            layoutGeneralAccountDetails.setVisibility(View.GONE);
                        }
                    } else {
                        layoutGeneralAccountDetails.setVisibility(View.GONE); // Hide if no data or error
                        if (task.getException() != null) {
                            Log.w(TAG, "Error getting general account.", task.getException());
                        }
                    }
                });

        // --- Load Card Account (Example: fetch first one) ---
        db.collection("card_accounts") // Assuming a collection "card_accounts"
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        CardAccount card = task.getResult().getDocuments().get(0).toObject(CardAccount.class);
                        if (card != null) {
                            txtCardName.setText(card.getCardName());
                            txtCardLast4.setText("**** " + card.getLast4Digits());
                            txtCardCurrentBalance.setText("Current Balance: " + currencyFormat.format(card.getCurrentBalance()));
                            txtCardCreditLimit.setText("Credit Limit: " + currencyFormat.format(card.getCreditLimit()));
                            txtCardAvailableBalance.setText("Available: " + currencyFormat.format(card.getAvailableBalance()));
                            layoutCardAccountDetails.setVisibility(View.VISIBLE);
                        } else {
                            layoutCardAccountDetails.setVisibility(View.GONE);
                        }
                    } else {
                        layoutCardAccountDetails.setVisibility(View.GONE);
                        if (task.getException() != null) {
                            Log.w(TAG, "Error getting card account.", task.getException());
                        }
                    }
                });


        // --- Load Bank Account (Example: fetch first one) ---
        db.collection("bank_accounts") // Assuming a collection "bank_accounts"
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        BankAccount bank = task.getResult().getDocuments().get(0).toObject(BankAccount.class);
                        if (bank != null) {
                            txtBankName.setText(bank.getBankName());
                            txtBankAccountLast4.setText("Acct: **** " + bank.getAccountNumberLast4());
                            txtBankAccountBalance.setText("Balance: " + currencyFormat.format(bank.getBalance()));
                            layoutBankAccountDetails.setVisibility(View.VISIBLE);
                        } else {
                            layoutBankAccountDetails.setVisibility(View.GONE);
                        }
                    } else {
                        layoutBankAccountDetails.setVisibility(View.GONE);
                        if (task.getException() != null) {
                            Log.w(TAG, "Error getting bank account.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Reload data if the user might have changed or data might be stale
        // This is a simple approach; for more complex scenarios, consider LiveData or other reactive patterns.
        currentUser = mAuth.getCurrentUser(); // Refresh current user
        if (currentUser != null) {
            loadTransactionSummary();
        } else {
            clearSummary();
            // Optionally, navigate to login
        }
    }
}
