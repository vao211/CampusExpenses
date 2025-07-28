package com.example.vcampusexpenses.services;

import android.content.Context;
import com.example.vcampusexpenses.datamanager.JsonDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionService {
    private final JsonDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public TransactionService(Context context, String userId) {
        this.dataFile = new JsonDataManager(context, userId);
        this.userData = dataFile.getUserDataObject();
        this.userId = userId;
    }

    protected void saveTransaction(Transaction transaction) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (transactions == null) {
            transactions = new HashMap<>();
            userData.getUser().getData().setTransactions(transactions);
        }
        transactions.put(transaction.getTransactionId(), transaction);
        dataFile.saveData();
    }

    private void processIncome(Transaction transaction) {
        AccountService accountService = new AccountService(dataFile.getContext(), userId);
        Account account = accountService.getAccount(transaction.getAccountId());
        if (account == null) {
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }
        account.updateBalance(transaction.getAmount());
        accountService.saveAccount(account);
        BudgetService budgetService = new BudgetService(dataFile.getContext(), userId);
        budgetService.updateBudgetsInTransaction(transaction);
    }

    private void processOutcome(Transaction transaction) {
        AccountService accountService = new AccountService(dataFile.getContext(), userId);
        BudgetService budgetService = new BudgetService(dataFile.getContext(), userId);
        Account account = accountService.getAccount(transaction.getAccountId());
        if (account == null) {
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }
        if (account.getBalance() >= transaction.getAmount()) {
            account.updateBalance(-transaction.getAmount());
            accountService.saveAccount(account);
            budgetService.updateBudgetsInTransaction(transaction);
        } else {
            DisplayToast.Display(dataFile.getContext(), "not enough balance");
            return;
        }
    }

    private void processTransfer(Transaction transaction) {
        AccountService accountService = new AccountService(dataFile.getContext(), userId);
        Account fromAccount = accountService.getAccount(transaction.getFromAccountId());
        Account toAccount = accountService.getAccount(transaction.getToAccountId());
        if (fromAccount == null || toAccount == null) {
            DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
            return;
        }
        if (fromAccount.getBalance() >= transaction.getAmount()) {
            fromAccount.updateBalance(-transaction.getAmount());
            toAccount.updateBalance(transaction.getAmount());
            accountService.saveAccount(fromAccount);
            accountService.saveAccount(toAccount);
        } else {
            DisplayToast.Display(dataFile.getContext(), "Not enough balance to transfer");
            return;
        }
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null || !transaction.isValid()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Transaction");
            return;
        }

        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (accounts == null || categories == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //nếu là transfer, kiểm tra tài khoản tồn tại
        if (transaction.isTransfer()) {
            if (!accounts.containsKey(transaction.getFromAccountId()) ||
                    !accounts.containsKey(transaction.getToAccountId())) {
                DisplayToast.Display(dataFile.getContext(), "Invalid Account");
                return;
            }
        }
        //không phải transfer (income/outcome)
        else {
            if (!accounts.containsKey(transaction.getAccountId())) {
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            //kiểm tra danh mục
            if (!categories.containsKey(transaction.getCategoryId())) {
                DisplayToast.Display(dataFile.getContext(), "Category not found");
                return;
            }
        }

        //kiem tra budget của outcome
        if (transaction.getType().equals("OUTCOME")) {
            BudgetService budgetService = new BudgetService(dataFile.getContext(), userId);
            List<Budget> budgets = budgetService.getListUserBudgets();
            for (Budget budget : budgets) {
                if (budget.appliesToTransaction(transaction)) {
                    Double categoryLimit = budget.getCategoryLimits().get(transaction.getCategoryId());
                    if (categoryLimit != null && transaction.getAmount() > budget.getRemainingAmount()) {
                        DisplayToast.Display(dataFile.getContext(), "Transaction exceeds category limit");
                        return;
                    }
                }
            }
        }
        //sinh transactionId tự động
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
                DisplayToast.Display(dataFile.getContext(), "Invalid Transaction Type");
                return;
        }
        saveTransaction(transaction);
        DisplayToast.Display(dataFile.getContext(), "Transaction added successfully");
    }

    public void deleteTransaction(String transactionId) {
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        if (transactions == null || !transactions.containsKey(transactionId)) {
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }
        //tạo giao dịch để khôi phục số dư
        Transaction transactionTemp = transactions.get(transactionId);

        //Đảo ngược giao dịch
        AccountService accountService = new AccountService(dataFile.getContext(), userId);
        if (transactionTemp.isTransfer()) {
            Account fromAccount = accountService.getAccount(transactionTemp.getFromAccountId());
            Account toAccount = accountService.getAccount(transactionTemp.getToAccountId());
            if (fromAccount == null || toAccount == null) {
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
            fromAccount.updateBalance(transactionTemp.getAmount());
            toAccount.updateBalance(-transactionTemp.getAmount());
            accountService.saveAccount(fromAccount);
            accountService.saveAccount(toAccount);
        } else {
            Account account = accountService.getAccount(transactionTemp.getAccountId());
            if (account == null) {
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (transactionTemp.getType().equals("INCOME")) {
                account.updateBalance(-transactionTemp.getAmount());
            } else if (transactionTemp.getType().equals("OUTCOME")) {
                account.updateBalance(transactionTemp.getAmount());
            }
            accountService.saveAccount(account);
            //đảo ngược tác động lên ngân sách
            BudgetService budgetService = new BudgetService(dataFile.getContext(), userId);
            budgetService.reverseBudgetUpdate(transactionTemp);
        }
        //xóa temp
        transactions.remove(transactionId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Transaction deleted successfully");
    }

    public void updateTransaction(String transactionId, Transaction newTransaction) {
        if (newTransaction == null || !newTransaction.isValid()) {
            DisplayToast.Display(dataFile.getContext(), "InValid Transaction");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Transaction> transactions = userData.getUser().getData().getTransactions();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        if (transactions == null || accounts == null || categories == null) {
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        if (!transactions.containsKey(transactionId)) {
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }

        if (newTransaction.isTransfer()) {
            if (!accounts.containsKey(newTransaction.getFromAccountId()) ||
                    !accounts.containsKey(newTransaction.getToAccountId())) {
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
        } else {
            if (!accounts.containsKey(newTransaction.getAccountId())) {
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (!categories.containsKey(newTransaction.getCategoryId())) {
                DisplayToast.Display(dataFile.getContext(), "Category not found");
                return;
            }
        }

        //temp transaction để khôi phục số dư
        Transaction tempTransaction = transactions.get(transactionId);

        //đảo ngược giao dịch
        AccountService accountService = new AccountService(dataFile.getContext(), userId);
        if (tempTransaction.isTransfer()) {
            Account fromAccount = accountService.getAccount(tempTransaction.getFromAccountId());
            Account toAccount = accountService.getAccount(tempTransaction.getToAccountId());
            if (fromAccount == null || toAccount == null) {
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
            fromAccount.updateBalance(tempTransaction.getAmount());
            toAccount.updateBalance(-tempTransaction.getAmount());
            accountService.saveAccount(fromAccount);
            accountService.saveAccount(toAccount);
        } else {
            Account account = accountService.getAccount(tempTransaction.getAccountId());
            if (account == null) {
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
            if (tempTransaction.getType().equals("INCOME")) {
                account.updateBalance(-tempTransaction.getAmount());
            } else if (tempTransaction.getType().equals("OUTCOME")) {
                account.updateBalance(tempTransaction.getAmount());
            }
            accountService.saveAccount(account);
            //đảo ngược tác động lên ngân sách
            BudgetService budgetService = new BudgetService(dataFile.getContext(), userId);
            budgetService.reverseBudgetUpdate(tempTransaction);
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
                DisplayToast.Display(dataFile.getContext(), "Invalid Transaction Type");
                return;
        }
        saveTransaction(newTransaction);
        DisplayToast.Display(dataFile.getContext(), "Transaction updated successfully");
    }
    public List<Transaction> getListTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            DisplayToast.Display(dataFile.getContext(), "");
            return transactions;
        }
        Map<String, Transaction> transactionMap = userData.getUser().getData().getTransactions();
        if (transactionMap != null) {
            transactions.addAll(transactionMap.values());
        }
        return transactions;
    }
}