package com.example.vcampusexpenses.services;

import android.util.Log;

import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Account;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.model.UserData;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private final UserDataManager dataFile;
    private final UserData userData;
    private final String userId;

    public BudgetService(UserDataManager dataManager) {
        this.dataFile = dataManager;
        this.userData = dataManager.getUserDataObject();
        this.userId = dataManager.getUserId();
        Log.d("BudgetService", "Initialized with userId: " + userId);
    }

    public String getBudgetId(String budgetName) {
        Log.d("BudgetService", "Getting budgetId for budgetName: " + budgetName);
        if (budgetName == null || budgetName.trim().isEmpty()) {
            Log.e("BudgetService", "Invalid budget name");
            DisplayToast.Display(dataFile.getContext(), "Invalid budget name");
            return null;
        }
        JsonObject jsonData = dataFile.getUserData(userId);
        if (jsonData == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return null;
        }
        JsonObject budgets = jsonData.getAsJsonObject("budgets");
        if (budgets == null) {
            Log.e("BudgetService", "No budgets found");
            DisplayToast.Display(dataFile.getContext(), "No budgets found");
            return null;
        }
        for (Map.Entry<String, JsonElement> entry : budgets.entrySet()) {
            JsonObject budgetJson = entry.getValue().getAsJsonObject();
            if (budgetJson.has("name") && budgetJson.get("name").getAsString().equals(budgetName)) {
                String budgetId = budgetJson.get("budgetId").getAsString();
                Log.d("BudgetService", "Found budgetId: " + budgetId + " for budgetName: " + budgetName);
                return budgetId;
            }
        }
        Log.w("BudgetService", "Budget not found: " + budgetName);
        DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetName);
        return null;
    }

    public void addBudget(Budget budget) {
        Log.d("BudgetService", "Adding budget: " + (budget != null ? budget.getName() : "null"));
        if (budget == null || budget.getName() == null || budget.getTotalAmount() <= 0) {
            Log.e("BudgetService", "Budget info is invalid");
            DisplayToast.Display(dataFile.getContext(), "Budget info is invalid");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();

        if (accounts == null) {
            accounts = new HashMap<>();
        }
        if (categories == null) {
            categories = new HashMap<>();
        }
        if (budgets == null) {
            budgets = new HashMap<>();
        }

        userData.getUser().getData().setAccount(accounts);
        userData.getUser().getData().setCategories(categories);
        userData.getUser().getData().setBudgets(budgets);

        //ktra account tồn tại
        for (String accountId : budget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }
        //ktra category tồn tại
        for (String categoryId : budget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                Log.e("BudgetService", "Category not found: " + categoryId);
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        String budgetId = IdGenerator.generateId(IdGenerator.ModelType.BUDGET);
        budget.setBudgetId(budgetId);

        //thêm budget vào budgets
        budgets.put(budgetId, budget);

        //update list budget của các Account
        for (String accountId : budget.getAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray == null) {
                budgetsArray = new ArrayList<>();
                account.setBudgetIds(budgetsArray);
            }
            if (!budgetsArray.contains(budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        Log.d("BudgetService", "Budget added: " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Budget added successfully");
    }
    //Kiểm tra có  ghi đè các list account và category
    public void updateBudget(String budgetId, Budget newBudget, boolean override) {
        Log.d("BudgetService", "Updating budgetId: " + budgetId + ", override: " + override);

        // Kiểm tra tính hợp lệ của newBudget
        if (newBudget == null || newBudget.getName() == null || newBudget.getTotalAmount() <= 0) {
            Log.e("BudgetService", "Budget info is invalid");
            DisplayToast.Display(dataFile.getContext(), "Budget info is invalid");
            return;
        }

        // Kiểm tra userData có được khởi tạo đúng không
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        // Lấy dữ liệu từ userData
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        // Kiểm tra budgets, accounts, categories có tồn tại không
        if (budgets == null || accounts == null || categories == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        // Kiểm tra ngân sách có tồn tại không
        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        // Kiểm tra xem các tài khoản trong newBudget có tồn tại không
        for (String accountId : newBudget.getAccountIds()) {
            if (!accounts.containsKey(accountId)) {
                Log.e("BudgetService", "Account not found: " + accountId);
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        // Kiểm tra xem các danh mục trong newBudget có tồn tại không
        for (String categoryId : newBudget.getCategoryLimits().keySet()) {
            if (!categories.containsKey(categoryId)) {
                Log.e("BudgetService", "Category not found: " + categoryId);
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        // Lấy ngân sách cũ
        Budget oldBudget = budgets.get(budgetId);

        // Xóa budgetId khỏi danh sách budgetIds của các tài khoản cũ
        for (String accountId : oldBudget.getAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        // Tạo ngân sách mới để cập nhật
        Budget updatedBudget = new Budget(
                budgetId,
                newBudget.getName(),
                newBudget.getTotalAmount(),
                newBudget.getRemainingAmount(),
                newBudget.getStartDate(),
                newBudget.getEndDate()
        );

        // Xử lý accountIds
        if (override || newBudget.getAccountIds().isEmpty()) {
            // Nếu override = true hoặc newBudget không cung cấp accountIds, sử dụng accountIds từ newBudget
            updatedBudget.setAccountIds(new ArrayList<>(newBudget.getAccountIds()));
        } else {
            // Nếu override = false và newBudget có accountIds, giữ nguyên accountIds từ oldBudget
            updatedBudget.setAccountIds(new ArrayList<>(oldBudget.getAccountIds()));
        }

        // Xử lý categoryLimits
        if (override || newBudget.getCategoryLimits().isEmpty()) {
            // Nếu override = true hoặc newBudget không cung cấp categoryLimits, sử dụng categoryLimits từ newBudget
            updatedBudget.setCategoryLimits(new HashMap<>(newBudget.getCategoryLimits()));
        } else {
            // Nếu override = false và newBudget có categoryLimits, giữ nguyên categoryLimits từ oldBudget
            updatedBudget.setCategoryLimits(new HashMap<>(oldBudget.getCategoryLimits()));
        }

        // Cập nhật ngân sách trong danh sách budgets
        budgets.put(budgetId, updatedBudget);

        // Cập nhật budgetIds cho các tài khoản mới
        for (String accountId : updatedBudget.getAccountIds()) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray == null) {
                budgetsArray = new ArrayList<>();
                account.setBudgetIds(budgetsArray);
            }
            if (!budgetsArray.contains(budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        Log.d("BudgetService", "Budget updated: " + updatedBudget.getName());
        DisplayToast.Display(dataFile.getContext(), "Budget updated successfully");
    }

    public void deleteBudget(String budgetId) {
        Log.d("BudgetService", "Deleting budgetId: " + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || accounts == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }

        //Ktra budget tồn tại
        if (!budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //xóa budget khỏi account
        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        for (String accountId : accountIds) {
            Account account = accounts.get(accountId);
            List<String> budgetsArray = account.getBudgetIds();
            if (budgetsArray != null) {
                budgetsArray.remove(budgetId);
            }
        }

        budgets.remove(budgetId);
        Log.d("BudgetService", "Budget deleted: " + budgetId);
        DisplayToast.Display(dataFile.getContext(), "Budget deleted successfully");
    }

    public List<Budget> getListUserBudgets() {
        Log.d("BudgetService", "Getting list of budgets");
        List<Budget> listBudgets = new ArrayList<>();
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return listBudgets;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        if (budgets != null) {
            listBudgets.addAll(budgets.values());
            Log.d("BudgetService", "Found " + listBudgets.size() + " budgets");
        } else {
            Log.w("BudgetService", "No budgets found");
        }
        return listBudgets;
    }

    private boolean checkContainsInJsonArray(JsonArray array, String value) {
        for (JsonElement element : array) {
            if (element.getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    protected void updateBudgetsInTransaction(Transaction transaction) {
        Log.d("BudgetService", "Updating budgets for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping budget update");
            return;
        }

        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                budget.updateRemaining(transaction);
                Log.d("BudgetService", "Updated budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
        // Không gọi saveData() ở đây
    }

    protected void reverseBudgetUpdate(Transaction transaction) {
        Log.d("BudgetService", "Reversing budget update for transaction: " + transaction.getDescription());
        if (transaction.isTransfer()) {
            Log.d("BudgetService", "Transaction is a transfer, skipping reverse budget update");
            return;
        }
        List<Budget> budgets = getListUserBudgets();
        for (Budget budget : budgets) {
            if (budget.appliesToTransaction(transaction)) {
                if (transaction.getType().equals("INCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() - transaction.getAmount());
                } else if (transaction.getType().equals("OUTCOME")) {
                    budget.setRemainingAmount(budget.getRemainingAmount() + transaction.getAmount());
                }
                Log.d("BudgetService", "Reversed budget: " + budget.getName() + ", remaining: " + budget.getRemainingAmount());
            }
        }
    }
    // --- Quản lý category limits ---

    public void addCategoryLimit(String budgetId, String categoryId, double limit) {
        Log.d("BudgetService", "Adding category limit: categoryId=" + categoryId + ", limit=" + limit + " to budgetId=" + budgetId);
        if (limit <= 0) {
            Log.e("BudgetService", "Invalid category limit: " + limit);
            DisplayToast.Display(dataFile.getContext(), "Invalid category limit");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("BudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return;
        }

        Budget budget = budgets.get(budgetId);
        Map<String, Double> categoryLimits = budget.getCategoryLimits();
        if (categoryLimits.containsKey(categoryId)) {
            Log.w("BudgetService", "Category limit already exists for categoryId: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category limit already exists");
            return;
        }

        budget.addCategoryLimit(categoryId, limit);
        Log.d("BudgetService", "Added category limit: categoryId=" + categoryId + ", limit=" + limit + " to budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Category limit added successfully");
    }

    public void updateCategoryLimit(String budgetId, String categoryId, double newLimit) {
        Log.d("BudgetService", "Updating category limit: categoryId=" + categoryId + ", newLimit=" + newLimit + " for budgetId=" + budgetId);
        if (newLimit <= 0) {
            Log.e("BudgetService", "Invalid category limit: " + newLimit);
            DisplayToast.Display(dataFile.getContext(), "Invalid category limit");
            return;
        }
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("BudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return;
        }

        Budget budget = budgets.get(budgetId);
        Map<String, Double> categoryLimits = budget.getCategoryLimits();
        if (!categoryLimits.containsKey(categoryId)) {
            Log.e("BudgetService", "Category limit not found for categoryId: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category limit not found");
            return;
        }

        budget.addCategoryLimit(categoryId, newLimit); // Sử dụng addCategoryLimit để cập nhật giá trị
        Log.d("BudgetService", "Updated category limit: categoryId=" + categoryId + ", newLimit=" + newLimit + " for budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Category limit updated successfully");
    }

    public void deleteCategoryLimit(String budgetId, String categoryId) {
        Log.d("BudgetService", "Deleting category limit: categoryId=" + categoryId + " from budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Category> categories = userData.getUser().getData().getCategories();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (categories == null || !categories.containsKey(categoryId)) {
            Log.e("BudgetService", "Category not found: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
            return;
        }

        Budget budget = budgets.get(budgetId);
        Map<String, Double> categoryLimits = budget.getCategoryLimits();
        if (!categoryLimits.containsKey(categoryId)) {
            Log.e("BudgetService", "Category limit not found for categoryId: " + categoryId);
            DisplayToast.Display(dataFile.getContext(), "Category limit not found");
            return;
        }

        categoryLimits.remove(categoryId);
        Log.d("BudgetService", "Deleted category limit: categoryId=" + categoryId + " from budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Category limit deleted successfully");
    }

    // --- Quản lý accounts áp dụng ---
    public void addAccountToBudget(String budgetId, String accountId) {
        Log.d("BudgetService", "Adding account: accountId=" + accountId + " to budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("BudgetService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }

        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        if (accountIds.contains(accountId)) {
            Log.w("BudgetService", "Account already applied to budget: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account already applied to budget");
            return;
        }

        budget.addAccount(accountId);
        Account account = accounts.get(accountId);
        List<String> budgetIds = account.getBudgetIds();
        if (budgetIds == null) {
            budgetIds = new ArrayList<>();
            account.setBudgetIds(budgetIds);
        }
        if (!budgetIds.contains(budgetId)) {
            budgetIds.add(budgetId);
        }

        Log.d("BudgetService", "Added account: accountId=" + accountId + " to budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account added to budget successfully");
    }

    public void updateAccountInBudget(String budgetId, String oldAccountId, String newAccountId) {
        Log.d("BudgetService", "Updating account: oldAccountId=" + oldAccountId + " to newAccountId=" + newAccountId + " in budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(oldAccountId) || !accounts.containsKey(newAccountId)) {
            Log.e("BudgetService", "Account not found: oldAccountId=" + oldAccountId + ", newAccountId=" + newAccountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found");
            return;
        }

        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        if (!accountIds.contains(oldAccountId)) {
            Log.e("BudgetService", "Old account not found in budget: " + oldAccountId);
            DisplayToast.Display(dataFile.getContext(), "Old account not found in budget");
            return;
        }
        if (accountIds.contains(newAccountId)) {
            Log.w("BudgetService", "New account already applied to budget: " + newAccountId);
            DisplayToast.Display(dataFile.getContext(), "New account already applied to budget");
            return;
        }

        accountIds.remove(oldAccountId);
        budget.addAccount(newAccountId);

        Account oldAccount = accounts.get(oldAccountId);
        List<String> oldBudgetIds = oldAccount.getBudgetIds();
        if (oldBudgetIds != null) {
            oldBudgetIds.remove(budgetId);
        }

        Account newAccount = accounts.get(newAccountId);
        List<String> newBudgetIds = newAccount.getBudgetIds();
        if (newBudgetIds == null) {
            newBudgetIds = new ArrayList<>();
            newAccount.setBudgetIds(newBudgetIds);
        }
        if (!newBudgetIds.contains(budgetId)) {
            newBudgetIds.add(budgetId);
        }

        Log.d("BudgetService", "Updated account: oldAccountId=" + oldAccountId + " to newAccountId=" + newAccountId + " in budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account updated in budget successfully");
    }

    public void deleteAccountFromBudget(String budgetId, String accountId) {
        Log.d("BudgetService", "Deleting account: accountId=" + accountId + " from budgetId=" + budgetId);
        if (userData == null || userData.getUser() == null || userData.getUser().getData() == null) {
            Log.e("BudgetService", "User data not initialized");
            DisplayToast.Display(dataFile.getContext(), "User data not initialized");
            return;
        }
        Map<String, Budget> budgets = userData.getUser().getData().getBudgets();
        Map<String, Account> accounts = userData.getUser().getData().getAccount();

        if (budgets == null || !budgets.containsKey(budgetId)) {
            Log.e("BudgetService", "Budget not found: " + budgetId);
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }
        if (accounts == null || !accounts.containsKey(accountId)) {
            Log.e("BudgetService", "Account not found: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
            return;
        }

        Budget budget = budgets.get(budgetId);
        List<String> accountIds = budget.getAccountIds();
        if (!accountIds.contains(accountId)) {
            Log.e("BudgetService", "Account not found in budget: " + accountId);
            DisplayToast.Display(dataFile.getContext(), "Account not found in budget");
            return;
        }

        accountIds.remove(accountId);
        Account account = accounts.get(accountId);
        List<String> budgetIds = account.getBudgetIds();
        if (budgetIds != null) {
            budgetIds.remove(budgetId);
        }

        Log.d("BudgetService", "Deleted account: accountId=" + accountId + " from budget " + budget.getName());
        DisplayToast.Display(dataFile.getContext(), "Account removed from budget successfully");
    }
}