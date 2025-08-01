package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.activity.ChartCategoryActivity;
import com.example.vcampusexpenses.adapters.CategoryAdapter;
import com.example.vcampusexpenses.datamanager.UserDataManager;
import com.example.vcampusexpenses.model.Transaction;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.services.TransactionService;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;
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

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {
    private RecyclerView recyclerViewCategories;
    private TextView txtEmptyCategories;
    private CategoryService categoryService;
    private ImageButton btnAddCategory, btnChart;
    private UserDataManager dataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerViewCategories = view.findViewById(R.id.rv_category);
        txtEmptyCategories = view.findViewById(R.id.txt_empty_categories);
        btnAddCategory = view.findViewById(R.id.btn_add_category);
        btnChart = view.findViewById(R.id.btn_chart);

        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();
        dataManager = UserDataManager.getInstance(requireContext(), userId);
        categoryService = new CategoryService(dataManager);

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        addCategory();
        goToChart();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories();
    }
    private void goToChart(){
        btnChart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChartCategoryActivity.class);
            startActivity(intent);
        });
    }
    private void addCategory() {
        btnAddCategory.setOnClickListener(v -> {
            Log.d("CategoriesFragment", "Adding new category");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add Category");

            final EditText input = new EditText(requireContext());
            input.setHint("Enter Name");
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String categoryName = input.getText().toString().trim();
                if (categoryName.isEmpty()) {
                    Log.w("CategoriesFragment", "Category name is empty");
                    DisplayToast.Display(requireContext(), "Name cannot be empty");
                    return;
                }
                Category newCategory = new Category(null, categoryName);
                categoryService.addCategory(newCategory);
                dataManager.saveData();
                Log.d("CategoriesFragment", "Category added: " + categoryName);
                loadCategories();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                Log.d("CategoriesFragment", "Add category cancelled");
                dialog.cancel();
            });

            builder.show();
        });
    }

    private void loadCategories() {
        Log.d("CategoriesFragment", "Loading categories");
        if (txtEmptyCategories == null || recyclerViewCategories == null) {
            Log.e("CategoriesFragment", "UI components not initialized");
            return;
        }

        List<Category> categoryList = categoryService.getListCategories();

        if (categoryList == null || categoryList.isEmpty()) {
            Log.d("CategoriesFragment", "No categories found");
            txtEmptyCategories.setVisibility(View.VISIBLE);
            recyclerViewCategories.setVisibility(View.GONE);
        } else {
            Log.d("CategoriesFragment", "Found " + categoryList.size() + " categories");
            txtEmptyCategories.setVisibility(View.GONE);
            recyclerViewCategories.setVisibility(View.VISIBLE);
            //tạo lại adapter để hiển thị danh sách mới
            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, this);
            recyclerViewCategories.setAdapter(categoryAdapter);
        }
    }

    @Override
    public void editCategory(String categoryID) {
        Log.d("CategoriesFragment", "Editing category with ID: " + categoryID);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Category");
        final EditText input = new EditText(requireContext());
        input.setHint("Enter new Category name");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Log.w("CategoriesFragment", "New category name is empty");
                DisplayToast.Display(requireContext(), "Category name can't be empty");
                return;
            }
            categoryService.updateCategory(categoryID, newName);
            dataManager.saveData(); //Lưu dữ liệu sau khi cập nhật
            Log.d("CategoriesFragment", "Category updated: " + newName);
            loadCategories();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Log.d("CategoriesFragment", "Update category cancelled");
            dialog.cancel();
        });

        builder.show();
    }

    @Override
    public void deleteCategory(String categoryId) {
        Log.d("CategoriesFragment", "Deleting category with ID: " + categoryId);
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoryService.deleteCategory(categoryId);
                    dataManager.saveData();//Lưu data sau khi xóa danh mục
                    Log.d("CategoriesFragment", "Category deleted: " + categoryId);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.d("CategoriesFragment", "Delete category cancelled");
                    dialog.cancel();
                })
                .show();
    }
}