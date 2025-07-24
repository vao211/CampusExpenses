package com.example.vcampusexpenses.methods;

import android.content.Context;

import com.example.vcampusexpenses.datamanager.JsonDataFile;
import com.example.vcampusexpenses.model.Budget;
import com.example.vcampusexpenses.utils.DisplayToast;
import com.example.vcampusexpenses.utils.IdGenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetMethod {
    private final JsonDataFile dataFile;
    private final String userId;

    public BudgetMethod(Context context, String userId) {
        this.dataFile = new JsonDataFile(context);
        this.userId = userId;
    }

    public void addBudget(Budget budget) {
        if (budget == null
                || budget.getName() == null
                || budget.getTotalAmount() <= 0){
            DisplayToast.Display(dataFile.getContext(),"Budget Info is invalid");
        }

        JsonObject userData = dataFile.getUserData(userId);
        JsonObject accounts = userData.getAsJsonObject("accounts");
        JsonObject categories = userData.getAsJsonObject("categories");

        //Kiểm tra account tồn tại
        for (String accountId : budget.getAccountIds()) {
            if (!accounts.has(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }
        //Kiểm tra category tồn tại
        for (String categoryId : budget.getCategoryLimits().keySet()) {
            if (!categories.has(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        String budgetId = IdGenerator.generateId(IdGenerator.ModelType.BUDGET);
        budget.setBudgetId(budgetId);

        JsonObject budgetJson = new JsonObject();
        budgetJson.addProperty("budgetId", budgetId);
        budgetJson.addProperty("name", budget.getName());
        budgetJson.addProperty("totalAmount", budget.getTotalAmount());
        budgetJson.addProperty("remainingAmount", budget.getRemainingAmount());
        budgetJson.addProperty("startDate", budget.getStartDate());
        budgetJson.addProperty("endDate", budget.getEndDate());

        // Thêm account vào budget
        JsonArray accountIds = new JsonArray();
        for (String accountId : budget.getAccountIds()) {
            accountIds.add(accountId);
        }
        budgetJson.add("accountIds", accountIds);

//        // Thêm categoryLimits vào budget
//        JsonObject categoryLimits = new JsonObject();
//        for (String categoryId : budget.getCategoryLimits().keySet()) {
//            categoryLimits.addProperty(categoryId, budget.getCategoryLimits().get(categoryId));
//        }
//        budgetJson.add("categoryLimits", categoryLimits);

        //thêm categoryLimits
        JsonObject categoryLimits = new JsonObject();
        for (Map.Entry<String, Double> entry : budget.getCategoryLimits().entrySet()) {
            categoryLimits.addProperty(entry.getKey(), entry.getValue());
        }
        budgetJson.add("categoryLimits", categoryLimits);

        //thêm budget vào json
        userData.getAsJsonObject("budgets").add(budgetId, budgetJson);

        //update danh sách budget của các Account
        for (String accountId : budget.getAccountIds()) {
            JsonObject accountJson = accounts.getAsJsonObject(accountId);
            JsonArray budgetsArray = accountJson.getAsJsonArray("budgets");
            if (budgetsArray == null) {
                budgetsArray = new JsonArray();
                accountJson.add("budgets", budgetsArray);
            }
            if (!checkContainsInJsonArray(budgetsArray, budgetId)) {
                budgetsArray.add(budgetId);
            }
        }

        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Add budget successfully");
    }

    public void updateBudget(String budgetId, Budget newBudget) {
        if (newBudget == null
                || newBudget.getName() == null
                || newBudget.getTotalAmount() <= 0){
            DisplayToast.Display(dataFile.getContext(),"Budget Info is invalid");
        }
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject budgets = userData.getAsJsonObject("budgets");
        JsonObject accounts = userData.getAsJsonObject("accounts");
        JsonObject categories = userData.getAsJsonObject("categories");

        //Kiểm tra budget tồn tại
        if (!budgets.has(budgetId)) {
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //Kiểm tra account tồn tại
        for (String accountId : newBudget.getAccountIds()) {
            if (!accounts.has(accountId)) {
                DisplayToast.Display(dataFile.getContext(), "Account not found: " + accountId);
                return;
            }
        }

        //Kiểm tra category tồn tại trong categoryLimits
        for (String categoryId : newBudget.getCategoryLimits().keySet()) {
            if (!categories.has(categoryId)) {
                DisplayToast.Display(dataFile.getContext(), "Category not found: " + categoryId);
                return;
            }
        }

        //Xóa budget khỏi account
        JsonObject oldBudgetJson = budgets.getAsJsonObject(budgetId);
        JsonArray oldAccountIds = oldBudgetJson.getAsJsonArray("accountIds");
        for(JsonElement accountId : oldAccountIds) {
            JsonObject accountJson = accounts.getAsJsonObject(accountId.getAsString());
            JsonArray budgetsArray = accountJson.getAsJsonArray("budgets");

            //Xóa budget khỏi danh sách budgets của account (ghi đè)
            if (budgetsArray != null) {
                JsonArray newBudgetsArray = new JsonArray();
                for (JsonElement budgetIdElement : budgetsArray) {
                    if (!budgetIdElement.getAsString().equals(budgetId)) {
                        newBudgetsArray.add(budgetIdElement);
                    }
                }
                accountJson.add("budgets", newBudgetsArray);
            }
        }

        //Update budget
        JsonObject budgetJson = new JsonObject();
        budgetJson.addProperty("budgetId", budgetId);
        budgetJson.addProperty("name", newBudget.getName());
        budgetJson.addProperty("totalAmount", newBudget.getTotalAmount());
        budgetJson.addProperty("remainingAmount", newBudget.getRemainingAmount());
        budgetJson.addProperty("startDate", newBudget.getStartDate());
        budgetJson.addProperty("endDate", newBudget.getEndDate());

        //hêm account mới  vào budget
        JsonArray accountIds = new JsonArray();
        for (String accountId : newBudget.getAccountIds()) {
            accountIds.add(accountId);
        }
        budgetJson.add("accountIds", accountIds);

        //thêm categoryLimits
        JsonObject categoryLimits = new JsonObject();
        for (Map.Entry<String, Double> entry : newBudget.getCategoryLimits().entrySet()) {
            categoryLimits.addProperty(entry.getKey(), entry.getValue());
        }
        budgetJson.add("categoryLimits", categoryLimits);
        budgets.add(budgetId, budgetJson);

        //update danh sách budget của các Account mới
        for(String accountId : newBudget.getAccountIds()) {
            JsonObject accountJson = accounts.getAsJsonObject(accountId);
            JsonArray budgetsArray = accountJson.getAsJsonArray("budgets");
            if (budgetsArray == null) {
                budgetsArray = new JsonArray();
                accountJson.add("budgets", budgetsArray);
            }
            //kiếm tra đã có budget này trong danh sách chưa
            if (!checkContainsInJsonArray(budgetsArray, budgetId)) {
                budgetsArray.add(budgetId);
            }

            dataFile.saveData();
            DisplayToast.Display(dataFile.getContext(), "Update budget successfully");
        }
    }

    public void deleteBudget(String budgetId) {
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject budgets = userData.getAsJsonObject("budgets");
        JsonObject accounts = userData.getAsJsonObject("accounts");

        //Kiểm tra budget tồn tại
        if (!budgets.has(budgetId)) {
            DisplayToast.Display(dataFile.getContext(), "Budget not found: " + budgetId);
            return;
        }

        //xóa budget khỏi account
        JsonObject budgetJson = budgets.getAsJsonObject(budgetId);
        JsonArray accountIds = budgetJson.getAsJsonArray("accountIds");
        for(JsonElement accountId : accountIds) {
            JsonObject accountJson = accounts.getAsJsonObject(accountId.getAsString());
            JsonArray budgetsArray = accountJson.getAsJsonArray("budgets");

            //xóa budget khỏi danh sách budgets
            if (budgetsArray != null) {
                JsonArray newBudgetsArray = new JsonArray();
                for (JsonElement budgetIdElement : budgetsArray) {
                    if (!budgetIdElement.getAsString().equals(budgetId)) {
                        newBudgetsArray.add(budgetIdElement);
                    }
                }
                accountJson.add("budgets", newBudgetsArray);
            }
        }

        //xóa budget
        budgets.remove(budgetId);
        dataFile.saveData();
        DisplayToast.Display(dataFile.getContext(), "Delete budget successfully");
    }

    public List<Budget> getListUserBudgets() {
        List<Budget> listBudgets = new ArrayList<>();
        JsonObject userData = dataFile.getUserData(userId);
        JsonObject budgets = userData.getAsJsonObject("budgets");

        for (String budgetId : budgets.keySet()) {
            JsonObject budgetJson = budgets.getAsJsonObject(budgetId);
            Budget budget = new Budget(
                    budgetId,
                    budgetJson.get("name").getAsString(),
                    budgetJson.get("totalAmount").getAsDouble(),
                    budgetJson.get("remainingAmount").getAsDouble(),
                    budgetJson.get("startDate").getAsString(),
                    budgetJson.get("endDate").getAsString()
            );

            //thêm account id
            JsonArray accountIds = budgetJson.getAsJsonArray("accountIds");
            for (JsonElement accountId : accountIds) {
                budget.addAccount(accountId.getAsString());
            }

            //thêm category limits
            JsonObject categoryLimits = budgetJson.getAsJsonObject("categoryLimits");
            if (categoryLimits != null) {
                for (String categoryId : categoryLimits.keySet()) {
                    budget.addCategoryLimit(categoryId, categoryLimits.get(categoryId).getAsDouble());
                }
            }
            listBudgets.add(budget);
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
}
