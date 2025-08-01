package com.example.vcampusexpenses.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartCategoryActivity extends AppCompatActivity {
    private TransactionService transactionService;
    private PieChart pieChartIncomeCategory, pieChartOutcomeCategory;
    private UserDataManager dataManager;
    private CategoryService categoryService;
    private SessionManager sessionManager;

    ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_category);

        btnClose = findViewById(R.id.btn_close);
        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        dataManager = UserDataManager.getInstance(this, userId);
        categoryService = new CategoryService(dataManager);
        transactionService = new TransactionService(dataManager, null, null);
        pieChartIncomeCategory = findViewById(R.id.piechart_income_category);
        pieChartOutcomeCategory = findViewById(R.id.piechart_outcome_category);
        setUpChartData();
        close();
    }
    private void close(){
        btnClose.setOnClickListener(v -> {
           finish();
        });
    }
    private void setUpChartData(){
        List<Transaction> transactionsList = transactionService.getListTransactions();
        List<Category> categoryList = categoryService.getListCategories();
        Log.d("CategoriesFragment", "Transactions count: " + (transactionsList != null ? transactionsList.size() : 0));
        Log.d("CategoriesFragment", "Categories count: " + (categoryList != null ? categoryList.size() : 0));

        if (transactionsList == null || transactionsList.isEmpty()) {
            Log.w("CategoriesFragment", "No transactions available");
            setUpPieChart(pieChartIncomeCategory, new ArrayList<>(), "Income Category");
            setUpPieChart(pieChartOutcomeCategory, new ArrayList<>(), "Outcome Category");
            return;
        }
        if (categoryList == null || categoryList.isEmpty()) {
            Log.w("CategoriesFragment", "No categories available");
            setUpPieChart(pieChartIncomeCategory, new ArrayList<>(), "Income Category");
            setUpPieChart(pieChartOutcomeCategory, new ArrayList<>(), "Outcome Category");
            return;
        }

        Map<String, Float> incomeCategoryMap = new HashMap<>();
        Map<String, Float> outcomeCategoryMap = new HashMap<>();

        for (Category category : categoryList) {
            incomeCategoryMap.put(category.getCategoryId(), 0f);
            outcomeCategoryMap.put(category.getCategoryId(), 0f);
        }

        for (Transaction transaction : transactionsList) {
            String categoryId = transaction.getCategoryId();
            Log.d("CategoriesFragment", "Transaction: type=" + transaction.getType() + ", categoryId=" + categoryId + ", amount=" + transaction.getAmount());
            if (categoryId != null && incomeCategoryMap.containsKey(categoryId)) {
                float amount = (float) transaction.getAmount();
                if ("income".equalsIgnoreCase(transaction.getType())) {
                    incomeCategoryMap.put(categoryId, incomeCategoryMap.getOrDefault(categoryId, 0f) + amount);
                } else if ("outcome".equalsIgnoreCase(transaction.getType())) {
                    outcomeCategoryMap.put(categoryId, outcomeCategoryMap.getOrDefault(categoryId, 0f) + amount);
                }
            } else {
                Log.w("CategoriesFragment", "Invalid or missing categoryId: " + categoryId);
            }
        }

        //Chart income
        List<PieEntry> incomeEntries = new ArrayList<>();
        for(Category c : categoryList){
            float amount = incomeCategoryMap.get(c.getCategoryId());
            if(amount > 0){
                incomeEntries.add(new PieEntry(amount, c.getName()));
            }
        }
        //Chart outcome
        List<PieEntry> outcomeEntries = new ArrayList<>();
        for(Category c : categoryList){
            float amount = outcomeCategoryMap.get(c.getCategoryId());
            if(amount > 0){
                outcomeEntries.add(new PieEntry(amount, c.getName()));
            }
        }
        Log.d("CategoriesFragment", "Income entries count: " + incomeEntries.size());
        Log.d("CategoriesFragment", "Outcome entries count: " + outcomeEntries.size());
        setUpPieChart(pieChartIncomeCategory, incomeEntries, "Income by Category");
        setUpPieChart(pieChartOutcomeCategory, outcomeEntries, "Outcome by Category");
    }
    private void setUpPieChart(PieChart pieChart, List<PieEntry> entries, String tittle){
        if (entries.isEmpty()){
            pieChart.setData(null);
            pieChart.setNoDataText("No data available");
            pieChart.getDescription().setEnabled(false);
            pieChart.invalidate();
            return;
        }
        PieDataSet dataSet = new PieDataSet(entries, tittle);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setUsePercentValues(true);

        pieChart.setCenterText(tittle);
        pieChart.setCenterTextSize(16f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.R.color.transparent);

        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(android.R.color.black);
        pieChart.getLegend().setEnabled(true);
        pieChart.getDescription().setEnabled(false);

        pieChart.animateY(500);

        pieChart.setData(data);
        pieChart.setTouchEnabled(true);
        pieChart.invalidate(); // Cập nhật biểu đồ

    }
}
