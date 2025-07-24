package com.example.vcampusexpenses.methods;

import android.content.Context;

import com.example.vcampusexpenses.datamanager.JsonDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonObject;
import java.util.List;

public class TransactionMethod {
    private final JsonDataManager dataFile;
    private final String userId;

    public TransactionMethod(Context context, String userId) {
        this.dataFile = new JsonDataManager(context);
        this.userId = userId;
    }

    protected void saveTransaction(Transaction transaction) {
        JsonObject transactionJson = new JsonObject();
        transactionJson.addProperty("transactionId", transaction.getTransactionId());
        transactionJson.addProperty("type", transaction.getType());
        transactionJson.addProperty("amount", transaction.getAmount());
        transactionJson.addProperty("date", transaction.getDate());
        transactionJson.addProperty("description", transaction.getDescription());

        if (transaction.isTransfer()) {
            transactionJson.addProperty("fromAccountId", transaction.getFromAccountId());
            transactionJson.addProperty("toAccountId", transaction.getToAccountId());
        } else {
            transactionJson.addProperty("accountId", transaction.getAccountId());
            transactionJson.addProperty("categoryId", transaction.getCategoryId());
        }

        dataFile.getUserData(userId)
                .getAsJsonObject("transactions")
                .add(transaction.getTransactionId(), transactionJson);

        dataFile.saveData();
    }
    private void processIncome(Transaction transaction) {
        AccountMethod accountMethod = new AccountMethod(dataFile.getContext(), userId);
        Account account = accountMethod.getAccount(transaction.getAccountId());
        account.updateBalance(transaction.getAmount());
        accountMethod.saveAccount(account);
        BudgetMethod budgetMethod = new BudgetMethod(dataFile.getContext(), userId);
        budgetMethod.updateBudgets(transaction);
    }

    private void processOutcome(Transaction transaction) {
        AccountMethod accountMethod = new AccountMethod(dataFile.getContext(), userId);
        BudgetMethod budgetMethod = new BudgetMethod(dataFile.getContext(), userId);
        Account account = accountMethod.getAccount(transaction.getAccountId());
        if (account.getBalance() >= transaction.getAmount()) {
            account.updateBalance(-transaction.getAmount());
            accountMethod.saveAccount(account);
            budgetMethod.updateBudgets(transaction);
        } else {
            DisplayToast.Display(dataFile.getContext(), "not enough balance");
            return;
        }
    }

    private void processTransfer(Transaction transaction) {
        AccountMethod accountMethod = new AccountMethod(dataFile.getContext(), userId);
        Account fromAccount = accountMethod.getAccount(transaction.getFromAccountId());
        Account toAccount = accountMethod.getAccount(transaction.getToAccountId());
        if (fromAccount.getBalance() >= transaction.getAmount()) {
            fromAccount.updateBalance(-transaction.getAmount());
            toAccount.updateBalance(transaction.getAmount());
            accountMethod.saveAccount(fromAccount);
            accountMethod.saveAccount(toAccount);
        } else {
            DisplayToast.Display(dataFile.getContext(), "Not enough balance to transfer");
            return;
        }
    }
    public void addTransaction(Transaction transaction) {
        if(transaction == null || !transaction.isValid()){
            DisplayToast.Display(dataFile.getContext(), "InValid Transaction");
            return;
        }

        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accounts = userData.getAsJsonObject("accounts");

        //nếu là transfer, kiểm tra tài khoản tồn tại
        if(transaction.isTransfer()){
            if(!accounts.has(transaction.getFromAccountId()) ||
                    !accounts.has(transaction.getToAccountId())){
                DisplayToast.Display(dataFile.getContext(), "Invalid Account");
            }
        }
        //không phải transfer (income/outcome)
        else {
            if(!accounts.has(transaction.getAccountId())){
                DisplayToast.Display(dataFile.getContext(), "Account not found");
            }
            //kiểm tra danh mục
            JsonObject categories = userData.getAsJsonObject("categories");
            if(!categories.has(transaction.getCategoryId())){
                DisplayToast.Display(dataFile.getContext(), "Category not found");
            }
        }

        //kiem tra budget của outcome
        if (transaction.getType().equals("OUTCOME")) {
            BudgetMethod budgetMethod = new BudgetMethod(dataFile.getContext(), userId);
            List<Budget> budgets = budgetMethod.getListUserBudgets();
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
        switch (transaction.getType()){
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
        DisplayToast.Display(dataFile.getContext(),
                "Transaction added successfully");
    }

    public void deleteTransaction(String transactionId) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject transactions = userData.getAsJsonObject("transactions");

        JsonObject transactionObjectJson = transactions.getAsJsonObject(transactionId);
        if (!transactions.has(transactionId)) {
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }
        //tạo giao dịch để khôi phục số dư
        Transaction transactionTemp;
        String type = transactionObjectJson.get("type").getAsString();
        if (type.equals("TRANSFER")) {
            transactionTemp = new Transaction(
                    transactionObjectJson.get("fromAccountId").getAsString(),
                    transactionObjectJson.get("toAccountId").getAsString(),
                    transactionObjectJson.get("amount").getAsDouble(),
                    transactionObjectJson.get("date").getAsString(),
                    transactionObjectJson.get("description").getAsString()
            );
        }
        else {
            transactionTemp = new Transaction(
                    transactionObjectJson.get("accountId").getAsString(),
                    transactionObjectJson.get("categoryId").getAsString(),
                    transactionObjectJson.get("amount").getAsDouble(),
                    transactionObjectJson.get("date").getAsString(),
                    transactionObjectJson.get("description").getAsString()
            );
        }
        transactionTemp.setTransactionId(transactionId);

        //Đảo nguoực giao dịch
        AccountMethod accountMethod = new AccountMethod(dataFile.getContext(), userId);
        if (transactionTemp.isTransfer()){
            Account fromAccount = accountMethod.getAccount(transactionTemp.getFromAccountId());
            Account toAccount = accountMethod.getAccount(transactionTemp.getToAccountId());
            fromAccount.updateBalance(transactionTemp.getAmount());
            toAccount.updateBalance(-transactionTemp.getAmount());
            accountMethod.saveAccount(fromAccount);
            accountMethod.saveAccount(toAccount);
        }
        else {
            Account account = accountMethod.getAccount(transactionTemp.getAccountId());
            if (transactionTemp.getType().equals("INCOME")) {
                account.updateBalance(-transactionTemp.getAmount());
            } else if (transactionTemp.getType().equals("OUTCOME")) {
                account.updateBalance(transactionTemp.getAmount());
            }
            accountMethod.saveAccount(account);
            //đảo ngược tác động lên ngân sách
            BudgetMethod budgetMethod = new BudgetMethod(dataFile.getContext(), userId);
            budgetMethod.reverseBudgetUpdate(transactionTemp);
        }
        //xóa temp
        transactions.remove(transactionId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Transaction deleted successfully");
    }

    public void updateTransaction(String transactionId, Transaction newTransaction) {
        if(newTransaction == null || !newTransaction.isValid()){
            DisplayToast.Display(dataFile.getContext(), "InValid Transaction");
            return;
        }
        JsonObject userData = dataFile.getUserData(userId);
        //all transacrtion
        JsonObject transactionsJson = userData.getAsJsonObject("transactions");
        JsonObject accountsJson = transactionsJson.getAsJsonObject(transactionId);

        JsonObject oldTransactionJson = transactionsJson.getAsJsonObject(transactionId);
        if(oldTransactionJson == null){
            DisplayToast.Display(dataFile.getContext(), "Transaction not found");
            return;
        }

        if(newTransaction.isTransfer()){
            if(!accountsJson.has(newTransaction.getFromAccountId()) ||
                    !accountsJson.has(newTransaction.getToAccountId())){
                DisplayToast.Display(dataFile.getContext(), "Invalid from or to account");
                return;
            }
        }
        else {
            if(!accountsJson.has(newTransaction.getAccountId())) {
                DisplayToast.Display(dataFile.getContext(), "Account not found");
                return;
            }
        }

        if(!newTransaction.isTransfer()){
            JsonObject categories = userData.getAsJsonObject("categories");
            if(!categories.has(newTransaction.getCategoryId())){
                DisplayToast.Display(dataFile.getContext(), "Category not found");
                return;
            }
        }

        //temp transaction để khôi phục số dư
        Transaction tempTransaction;
        String type = oldTransactionJson.get("type").getAsString();
        if (type.equals("TRANSFER")) {
            tempTransaction = new Transaction(
                    oldTransactionJson.get("fromAccountId").getAsString(),
                    oldTransactionJson.get("toAccountId").getAsString(),
                    oldTransactionJson.get("amount").getAsDouble(),
                    oldTransactionJson.get("date").getAsString(),
                    oldTransactionJson.get("description").getAsString()
            );
        }
        else {
            tempTransaction = new Transaction(
                    oldTransactionJson.get("accountId").getAsString(),
                    oldTransactionJson.get("categoryId").getAsString(),
                    oldTransactionJson.get("amount").getAsDouble(),
                    oldTransactionJson.get("date").getAsString(),
                    oldTransactionJson.get("description").getAsString()
            );
        }
        tempTransaction.setTransactionId(transactionId);

        //đảo ngược giao dịch
        AccountMethod accountMethod = new AccountMethod(dataFile.getContext(), userId);
        if (tempTransaction.isTransfer()){
            Account fromAccount = accountMethod.getAccount(tempTransaction.getFromAccountId());
            Account toAccount = accountMethod.getAccount(tempTransaction.getToAccountId());
            fromAccount.updateBalance(tempTransaction.getAmount());
            toAccount.updateBalance(-tempTransaction.getAmount());
            accountMethod.saveAccount(fromAccount);
            accountMethod.saveAccount(toAccount);
        }
        else {
            Account account = accountMethod.getAccount(tempTransaction.getAccountId());
            if (tempTransaction.getType().equals("INCOME")) {
                account.updateBalance(-tempTransaction.getAmount());
            } else if (tempTransaction.getType().equals("OUTCOME")) {
                account.updateBalance(tempTransaction.getAmount());
            }
            accountMethod.saveAccount(account);
            //đảo ngược tác động lên ngân sách
            BudgetMethod budgetMethod = new BudgetMethod(dataFile.getContext(), userId);
            budgetMethod.reverseBudgetUpdate(tempTransaction);
        }
        //transaction mowsi
        newTransaction.setTransactionId(transactionId);
        switch (newTransaction.getType()){
            case "INCOME":
                processIncome(newTransaction);
                break;
            case "OUTCOME":
                processOutcome(newTransaction);
                break;
            default:
                DisplayToast.Display(dataFile.getContext(), "Invalid Transaction Type");
                return;
        }
        saveTransaction(newTransaction);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Transaction updated successfully");
    }
}
