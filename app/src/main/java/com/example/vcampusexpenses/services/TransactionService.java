package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;
    private final AccountService accountService;
    private final AccountBudgetService accountBudgetService;
    private final SimpleDateFormat dateFormat;

    public TransactionService(UserDataManager dataManager, AccountService accountService, AccountBudgetService accountBudgetService) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        this.accountService = accountService;
        this.accountBudgetService = accountBudgetService;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Log.d("TransactionService", "Initialized with userId: " + userId);
    }

    public Transaction getTransaction(String transactionId) {
        Log.d("TransactionService", "Getting transaction: " + transactionId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            return null;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (transactions == null || !transactions.containsKey(transactionId)) {
            Log.e("TransactionService", "Transaction not found: " + transactionId);
            return null;
        }
        Transaction transaction = transactions.get(transactionId);
        Log.d("TransactionService", "Transaction found: " + transaction.getDescription());
        return transaction;
    }

    protected void saveTransaction(Transaction transaction) {
        Log.d("TransactionService", "Saving transaction: " + transaction.getDescription());
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (transactions == null) {
            transactions = new HashMap<>();
            userData.getUser().getData().setTransactions(transactions);
        }
        transactions.put(transaction.getTransactionId(), transaction);
        Log.d("TransactionService", "Transaction saved: " + transaction.getDescription());
    }

    private void processIncome(Transaction transaction) {
        Log.d("TransactionService", "Processing INCOME transaction: " + transaction.getDescription());
        Account account = accountService.getAccount(transaction.getAccountId());
        if (account == null) {
            Log.e("TransactionService", "Account not found: " + transaction.getAccountId());
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }
        Log.d("TransactionService", "AccountID: " + account.getAccountId() + ", Amount: " + transaction.getAmount());
        accountService.updateBalance(account.getAccountId(), transaction.getAmount());
        accountBudgetService.updateBudgetsInTransaction(transaction);
        saveTransaction(transaction);
    }

    private void processOutcome(Transaction transaction) {
        Log.d("TransactionService", "Processing OUTCOME transaction: " + transaction.getDescription());
        Account account = accountService.getAccount(transaction.getAccountId());
        if (account == null) {
            Log.e("TransactionService", "Account not found: " + transaction.getAccountId());
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }
        if (account.getBalance() >= transaction.getAmount()) {
            accountService.updateBalance(account.getAccountId(), -transaction.getAmount());
            accountBudgetService.updateBudgetsInTransaction(transaction);
            saveTransaction(transaction);
        } else {
            Log.w("TransactionService", "Not enough balance for account: " + account.getAccountId());
            DisplayToast.Display(dataFile.getContext(), "Not enough balance");
        }
    }

    private void processTransfer(Transaction transaction) {
        Log.d("TransactionService", "Processing TRANSFER transaction: " + transaction.getDescription());
        Account fromAccount = accountService.getAccount(transaction.getFromAccountId());
        Account toAccount = accountService.getAccount(transaction.getToAccountId());
        if (fromAccount == null || toAccount == null) {
            Log.e("TransactionService", "Invalid from or to account");
            DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
            return;
        }
        if (fromAccount.getBalance() >= transaction.getAmount()) {
            accountService.updateBalance(fromAccount.getAccountId(), -transaction.getAmount());
            accountService.updateBalance(toAccount.getAccountId(), transaction.getAmount());
            saveTransaction(transaction);
        } else {
            Log.w("TransactionService", "Not enough balance to transfer from account: " + fromAccount.getAccountId());
            DisplayToast.Display(dataFile.getContext(), "Not enough balance to transfer");
        }
    }

    public void addTransaction(Transaction transaction) {
        Log.d("TransactionService", "Adding transaction: " + transaction.getDescription());
        if (!transaction.isValid()) {
            Log.e("TransactionService", "Invalid Transaction");
            DisplayToast.Display(dataFile.getContext(), "Invalid Transaction");
            return;
        }
        if (transaction == null) {
            Log.e("TransactionService", "Null Transaction");
            DisplayToast.Display(dataFile.getContext(), "Invalid Transaction");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (accounts == null || categories == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //Nếu là transfer, kiểm tra tài khoản tồn tại
        if (transaction.isTransfer()) {
            if (!accounts.containsKey(transaction.getFromAccountId()) ||
                    !accounts.containsKey(transaction.getToAccountId())) {
                Log.e("TransactionService", "Invalid Account");
                DisplayToast.Display(dataFile.getContext(), "Invalid Account");
                return;
            }
        }
        //Không phải transfer (income/outcome)
        else {
            if (!accounts.containsKey(transaction.getAccountId())) {
                Log.e("TransactionService", "Account not found: " + transaction.getAccountId());
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            // Kiểm tra danh mục
            if (!categories.containsKey(transaction.getCategoryId())) {
                Log.e("TransactionService", "Category not found: " + transaction.getCategoryId());
                DisplayToast.Display(dataFile.getContext(), "Category not found");
                return;
            }
        }

        //Ktra budget của outcome
        if (transaction.getType().equals("OUTCOME")) {
            List<AccountBudget> accountBudgets = accountBudgetService.getListUserBudgets();
            for (AccountBudget accountBudget : accountBudgets) {
                if (accountBudget.appliesToTransaction(transaction)) {
                    if (transaction.getAmount() > accountBudget.getRemainingAmount()) {
                        Log.w("TransactionService", "Transaction exceeds accountBudget: " + accountBudget.getName());
                        DisplayToast.Display(dataFile.getContext(), "Transaction exceeds accountBudget");
                        return;
                    }
                }
            }
        }

        String transactionId = IdGenerator.generateId(IdGenerator.ModelType.TRANSACTION);
        transaction.setTransactionId(transactionId);
        switch (transaction.getType()) {
            case "INCOME":
                processIncome(transaction);
                break;
            case "OUTCOME":
                processOutcome(transaction);
                break;
            case "TRANSFER":
                processTransfer(transaction);
                break;
            default:
                Log.e("TransactionService", "Invalid Transaction Type: " + transaction.getType());
                DisplayToast.Display(dataFile.getContext(), "Invalid Transaction Type");
                return;
        }
        Log.d("TransactionService", "Transaction added successfully: " + transaction.getDescription());
        DisplayToast.Display(dataFile.getContext(), "Transaction added successfully");
    }

    public void deleteTransaction(String transactionId) {
        Log.d("TransactionService", "Deleting transactionId: " + transactionId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (transactions == null || !transactions.containsKey(transactionId)) {
            Log.e("TransactionService", "Transaction not found: " + transactionId);
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }
        //temp transaction để khôi phục số dư
        Transaction transactionTemp = transactions.get(transactionId);

        //Đảo ngược giao dịch
        if (transactionTemp.isTransfer()) {
            Account fromAccount = accountService.getAccount(transactionTemp.getFromAccountId());
            Account toAccount = accountService.getAccount(transactionTemp.getToAccountId());
            if (fromAccount == null || toAccount == null) {
                Log.e("TransactionService", "Invalid from or to account");
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
            accountService.updateBalance(fromAccount.getAccountId(), transactionTemp.getAmount());
            accountService.updateBalance(toAccount.getAccountId(), -transactionTemp.getAmount());
        } else {
            Account account = accountService.getAccount(transactionTemp.getAccountId());
            if (account == null) {
                Log.e("TransactionService", "Account not found: " + transactionTemp.getAccountId());
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (transactionTemp.getType().equals("INCOME")) {
                accountService.updateBalance(account.getAccountId(), -transactionTemp.getAmount());
            } else if (transactionTemp.getType().equals("OUTCOME")) {
                accountService.updateBalance(account.getAccountId(), transactionTemp.getAmount());
            }
            //Đảo ngược tác động lên ngân sách
            accountBudgetService.reverseBudgetUpdate(transactionTemp);
        }
        //Xóa giao dịch
        transactions.remove(transactionId);
        Log.d("TransactionService", "Transaction deleted successfully: " + transactionId);
        DisplayToast.Display(dataFile.getContext(), "Transaction deleted successfully");
    }

    public void updateTransaction(String transactionId, Transaction newTransaction) {
        Log.d("TransactionService", "Updating transactionId: " + transactionId);
        if (newTransaction == null || !newTransaction.isValid()) {
            Log.e("TransactionService", "Invalid Transaction");
            DisplayToast.Display(dataFile.getContext(), "Invalid Transaction");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (transactions == null || accounts == null || categories == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        if (!transactions.containsKey(transactionId)) {
            Log.e("TransactionService", "Transaction not found: " + transactionId);
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }

        if (newTransaction.isTransfer()) {
            if (!accounts.containsKey(newTransaction.getFromAccountId()) ||
                    !accounts.containsKey(newTransaction.getToAccountId())) {
                Log.e("TransactionService", "Invalid from or to account");
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
        } else {
            if (!accounts.containsKey(newTransaction.getAccountId())) {
                Log.e("TransactionService", "Account not found: " + newTransaction.getAccountId());
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (!categories.containsKey(newTransaction.getCategoryId())) {
                Log.e("TransactionService", "Category not found: " + newTransaction.getCategoryId());
                DisplayToast.Display(dataFile.getContext(), "Category not found");
                return;
            }
        }

        //Temp transaction để khôi phục số dư
        Transaction tempTransaction = transactions.get(transactionId);

        //Đảo ngược giao dịch
        if (tempTransaction.isTransfer()) {
            Account fromAccount = accountService.getAccount(tempTransaction.getFromAccountId());
            Account toAccount = accountService.getAccount(tempTransaction.getToAccountId());
            if (fromAccount == null || toAccount == null) {
                Log.e("TransactionService", "Invalid from or to account");
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
            accountService.updateBalance(fromAccount.getAccountId(), tempTransaction.getAmount());
            accountService.updateBalance(toAccount.getAccountId(), -tempTransaction.getAmount());
        } else {
            Account account = accountService.getAccount(tempTransaction.getAccountId());
            if (account == null) {
                Log.e("TransactionService", "Account not found: " + tempTransaction.getAccountId());
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (tempTransaction.getType().equals("INCOME")) {
                accountService.updateBalance(account.getAccountId(), -tempTransaction.getAmount());
            } else if (tempTransaction.getType().equals("OUTCOME")) {
                accountService.updateBalance(account.getAccountId(), tempTransaction.getAmount());
            }
            //Đảo ngược tác động lên ngân sách
            accountBudgetService.reverseBudgetUpdate(tempTransaction);
        }
        //transaction mới
        newTransaction.setTransactionId(transactionId);
        switch (newTransaction.getType()) {
            case "INCOME":
                processIncome(newTransaction);
                break;
            case "OUTCOME":
                processOutcome(newTransaction);
                break;
            case "TRANSFER":
                processTransfer(newTransaction);
                break;
            default:
                Log.e("TransactionService", "Invalid Transaction Type: " + newTransaction.getType());
                DisplayToast.Display(dataFile.getContext(), "Invalid Transaction Type");
                return;
        }
        Log.d("TransactionService", "Transaction updated successfully: " + transactionId);
        DisplayToast.Display(dataFile.getContext(), "Transaction updated successfully");
    }

    public List<Transaction> getListTransactions() {
        Log.d("TransactionService", "Getting list of transactions");
        List<Transaction> transactions = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("TransactionService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return transactions;
        }
        Map<String, Transaction> transactionMap = userData.getUser().getData().getTransactions();
        if (transactionMap != null) {
            transactions.addAll(transactionMap.values());
            Log.d("TransactionService", "Found " + transactions.size() + " transactions");
        } else {
            Log.w("TransactionService", "No transactions found");
        }
        return transactions;
    }

    public double getTotalIncome(long startTime, long endTime) {
        Log.d("TransactionService", "Calculating total income from " + startTime + " to " + endTime);
        double totalIncome = 0.0;
        List<Transaction> transactions = getListTransactions();
        if (transactions == null || transactions.isEmpty()) {
            Log.w("TransactionService", "No transactions found for income calculation");
            return totalIncome;
        }

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("INCOME")) {
                try {
                    long transactionTime = dateFormat.parse(transaction.getDate()).getTime();
                    if (transactionTime >= startTime && transactionTime <= endTime) {
                        totalIncome += transaction.getAmount();
                    }
                } catch (ParseException e) {
                    Log.e("TransactionService", "Error parsing transaction date: " + transaction.getDate(), e);
                }
            }
        }
        Log.d("TransactionService", "Total income: " + totalIncome);
        return totalIncome;
    }

    public double getTotalOutcome(long startTime, long endTime) {
        Log.d("TransactionService", "Calculating total outcome from " + startTime + " to " + endTime);
        double totalOutcome = 0.0;
        List<Transaction> transactions = getListTransactions();
        if (transactions == null || transactions.isEmpty()) {
            Log.w("TransactionService", "No transactions found for outcome calculation");
            return totalOutcome;
        }

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("OUTCOME")) {
                try {
                    long transactionTime = dateFormat.parse(transaction.getDate()).getTime();
                    if (transactionTime >= startTime && transactionTime <= endTime) {
                        totalOutcome += transaction.getAmount();
                    }
                } catch (ParseException e) {
                    Log.e("TransactionService", "Error parsing transaction date: " + transaction.getDate(), e);
                }
            }
        }
        Log.d("TransactionService", "Total outcome: " + totalOutcome);
        return totalOutcome;
    }
}