package com.example.vcampusexpenses.EXAMPLE_CODE;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.AccountBudget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.AccountBudgetService;
import com.example.vcampusexpenses.services.AccountService;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;

import java.util.List;

public class Example extends AppCompatActivity {
    SessionManager sessionManager;
    UserDataManager userDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tạo các service
        sessionManager = new SessionManager(this);
        //Lấy instance của UserDataManager
        userDataManager = UserDataManager.getInstance(this, sessionManager.getUserId());
    }
    private void categoryMethods() {
        //tạo instance của CategoryService -> cần UserDataManager(truyền vào)
        CategoryService categoryService = new CategoryService(userDataManager);

        //Add
        String categoryName = "Example category";
        Category category = new Category(categoryName);
        categoryService.addCategory(category);
        //save sau khi thao tác thêm sửa xóa
        userDataManager.saveData();

        //Update
        String categoryId = categoryService.getCategoryId(categoryName);
        String newCategoryName = "Updated Example category";
        categoryService.updateCategory(categoryId, newCategoryName);
        userDataManager.saveData();

        //Delete
        categoryService.deleteCategory(categoryId);
        userDataManager.saveData();

        //Get list
        List<Category> listCategories = categoryService.getListCategories();

        //Get id
        String getCategoryId = categoryService.getCategoryId(categoryName);
    }

    private void accountMethods() {
        AccountService accountService = new AccountService(userDataManager);

        //Add
        String accountName = "Example account";
        double balance = 1000;
        Account account = new Account(accountName, balance);
        accountService.addAccount(account);
        userDataManager.saveData();

        //Update
        String accountId = accountService.getAccountId(accountName);
        String newAccountName = "Updated Example account";
        double newBalance = 2000;
        accountService.updateAccount(accountId, newAccountName, newBalance);
        userDataManager.saveData();

        //Delete
        accountService.deleteAccount(accountId);
        userDataManager.saveData();

        //Get list
        List<Account> listAccounts = accountService.getListAccounts();

        //Get id
        String getAccountId = accountService.getAccountId(accountName);
    }

    //2 hàm dưới dùng để thêm tài khoản và danh mục vào budget
    private String addAccountForBudget(String accountName, AccountService accountService) {
        Account account = new Account(accountName, 1000.0);
        accountService.addAccount(account);
        String accountId = accountService.getAccountId(accountName);
        if (accountId != null) {
            userDataManager.saveData();
        }
        return accountId;
    }

    private String addCategoryForBudget(String categoryName, CategoryService categoryService) {
        Category category = new Category(categoryName);
        categoryService.addCategory(category);
        String categoryId = categoryService.getCategoryId(categoryName);
        if (categoryId != null) {
            userDataManager.saveData();
        }
        return categoryId;
    }
    private void budgetMethods() {
        // Khởi tạo các service
        AccountBudgetService accountBudgetService = new AccountBudgetService(userDataManager);
        AccountService accountService = new AccountService(userDataManager);
        CategoryService categoryService = new CategoryService(userDataManager);

        // Tạo tài khoản và danh mục để sử dụng trong ngân sách
        String accountId1 = addAccountForBudget("Example account", accountService);
        String accountId2 = addAccountForBudget("Secondary account", accountService);
        String categoryId1 = addCategoryForBudget("Example category", categoryService);
        String categoryId2 = addCategoryForBudget("Food category", categoryService);

        // Add AccountBudget
        String budgetName = "Example accountBudget";
        double totalAmount = 200;
        double remainingAmount = 200;
        String startDate = "2025-07-01";
        String endDate = "2025-07-31";
        AccountBudget accountBudget = new AccountBudget(budgetName, totalAmount, remainingAmount, startDate, endDate);
        // Thêm account và category limit vào ngân sách
        if (accountId1 != null) {
            accountBudget.addAccount(accountId1);
        }
        accountBudgetService.addBudget(accountBudget);
        userDataManager.saveData();

        // Update AccountBudget (bảo toàn categoryLimits và accountIds)
        String budgetId = accountBudgetService.getBudgetId(budgetName);
        if (budgetId != null) {
            String newBudgetName = "Updated Example accountBudget";
            double newTotalAmount = 300;
            double newRemainingAmount = 300;
            String newStartDate = "2025-08-01";
            String newEndDate = "2025-08-31";
            AccountBudget newAccountBudget = new AccountBudget(budgetId, newBudgetName, newTotalAmount, newRemainingAmount, newStartDate, newEndDate);
            // Không thêm categoryLimits hoặc accountIds, chọn override = false để bảo toàn
            accountBudgetService.updateBudget(budgetId, newAccountBudget, false);
            userDataManager.saveData();
        }

        // Update AccountBudget (ghi đè categoryLimits và accountIds)
        if (budgetId != null) {
            String newBudgetName = "Overridden Example accountBudget";
            double newTotalAmount = 400;
            double newRemainingAmount = 400;
            String newStartDate = "2025-09-01";
            String newEndDate = "2025-09-30";
            AccountBudget newAccountBudget = new AccountBudget(budgetId, newBudgetName, newTotalAmount, newRemainingAmount, newStartDate, newEndDate);
            if (accountId2 != null) {
                newAccountBudget.addAccount(accountId2);
            }

            // Chọn override = true để ghi đè danh sách
            accountBudgetService.updateBudget(budgetId, newAccountBudget, true);
            userDataManager.saveData();
        }

        // Add Account to AccountBudget
        if (budgetId != null && accountId1 != null) {
            accountBudgetService.addAccountToBudget(budgetId, accountId1);
            userDataManager.saveData();
        }

        // Update Account in AccountBudget
        if (budgetId != null && accountId1 != null && accountId2 != null) {
            accountBudgetService.updateAccountInBudget(budgetId, accountId1, accountId2);
            userDataManager.saveData();
        }

        // Delete Account from AccountBudget
        if (budgetId != null && accountId2 != null) {
            accountBudgetService.deleteAccountFromBudget(budgetId, accountId2);
            userDataManager.saveData();
        }

        // Delete AccountBudget
        if (budgetId != null) {
            accountBudgetService.deleteBudget(budgetId);
            userDataManager.saveData();
        }

        // Get List of Budgets
        List<AccountBudget> listAccountBudgets = accountBudgetService.getListAccountBudgets();
    }


    private void transactionMethods() {
        // Khởi tạo các service
        AccountBudgetService accountBudgetService = new AccountBudgetService(userDataManager);
        AccountService accountService = new AccountService(userDataManager);
        CategoryService categoryService = new CategoryService(userDataManager);
        TransactionService transactionService = new TransactionService(userDataManager, accountService, accountBudgetService);

        // Tạo tài khoản và danh mục để sử dụng trong giao dịch
        String accountId1 = addAccountForBudget("Example account", accountService);
        String accountId2 = addAccountForBudget("Secondary account", accountService);
        String categoryId1 = addCategoryForBudget("Example category", categoryService);
        String categoryId2 = addCategoryForBudget("Food category", categoryService);

        // Tạo ngân sách để kiểm tra giới hạn cho giao dịch OUTCOME
        String budgetName = "Example accountBudget";
        double totalAmount = 200;
        double remainingAmount = 200;
        String startDate = "2025-07-01";
        String endDate = "2025-07-31";
        AccountBudget accountBudget = new AccountBudget(budgetName, totalAmount, remainingAmount, startDate, endDate);
        if (accountId1 != null) {
            accountBudget.addAccount(accountId1);
        }

        accountBudgetService.addBudget(accountBudget);
        userDataManager.saveData();

        // Add Transaction (INCOME)
        if (accountId1 != null && categoryId1 != null) {
            Transaction incomeTransaction = new Transaction("INCOME", accountId1, categoryId1, 500, "2025-07-02", "Salary deposit");
            transactionService.addTransaction(incomeTransaction);
            userDataManager.saveData();
        }

        // Add Transaction (OUTCOME)
        if (accountId1 != null && categoryId1 != null) {
            Transaction outcomeTransaction = new Transaction("OUTCOME", accountId1, categoryId1, 50, "2025-07-03", "Grocery purchase");
            transactionService.addTransaction(outcomeTransaction);
            userDataManager.saveData();
        }

        // Add Transaction (TRANSFER)
        if (accountId1 != null && accountId2 != null) {
            Transaction transferTransaction = new Transaction("TRANSFER", accountId1, accountId2, 100, "2025-07-04", "Transfer to secondary account");
            transactionService.addTransaction(transferTransaction);
            userDataManager.saveData();
        }

        // Update Transaction (OUTCOME)
        String transactionId = null;
        List<Transaction> transactions = transactionService.getListTransactions();
        for (Transaction t : transactions) {
            if (t.getType().equals("OUTCOME") && t.getDescription().equals("Grocery purchase")) {
                transactionId = t.getTransactionId();
                break;
            }
        }
        if (transactionId != null && accountId1 != null && categoryId2 != null) {
            Transaction updatedTransaction = new Transaction("OUTCOME", accountId1, categoryId2, 75, "2025-07-03", "Updated grocery purchase");
            transactionService.updateTransaction(transactionId, updatedTransaction);
            userDataManager.saveData();
        }

        // Delete Transaction (OUTCOME)
        if (transactionId != null) {
            transactionService.deleteTransaction(transactionId);
            userDataManager.saveData();
        }

        // Get List of Transactions
        List<Transaction> transactionList = transactionService.getListTransactions();
    }

}
