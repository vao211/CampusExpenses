package com.example.vcampusexpenses.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.example.vcampusexpenses.adapters.CategoryAdapter;
import com.example.vcampusexpenses.database.UserDB;
import com.example.vcampusexpenses.services.CategoryService;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.session.SessionManager;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.List;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener{
    private RecyclerView recyclerViewCategories;
    private TextView txtEmptyCategories;
    private CategoryService categoryService;
    private ImageButton btnAddCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerViewCategories = view.findViewById(R.id.rvCategory);
        txtEmptyCategories = view.findViewById(R.id.txtEmptyCategories);
        SessionManager sessionManager = new SessionManager(requireContext());
        String userId = sessionManager.getUserId();
        categoryService = new CategoryService(requireContext(), userId);
        btnAddCategory = view.findViewById(R.id.btn_addCategory);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        addCategory();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories();
    }
    private void addCategory() {
        btnAddCategory.setOnClickListener(v -> {
            //add category
            DisplayToast.Display(requireContext(), "Add category (thêm sau)");
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Add Category");

            final EditText input = new EditText(requireContext());
            input.setHint("Enter Category Name");
            builder.setView(input);

            //xác nhận
            builder.setPositiveButton("Add", (dialog, which) -> {
                String categoryName = input.getText().toString().trim();
                // Kiểm tra tên không rỗng
                if (categoryName.isEmpty()) {
                    DisplayToast.Display(requireContext(), "Category name can't be null");
                    return;
                }
                //Tạo đối tượng Category và gọi addCategory
                Category newCategory = new Category(categoryName);
                newCategory.setName(categoryName);
                categoryService.addCategory(newCategory);
                loadCategories(); // Làm mới danh sách sau khi thêm
            });

            // hủy
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        });
    }
    private void loadCategories() {
        if (txtEmptyCategories == null || recyclerViewCategories == null) {
            return;
        }

        List<Category> categoryList = categoryService.getListCategories();

        if (categoryList == null || categoryList.isEmpty()) {
            txtEmptyCategories.setVisibility(View.VISIBLE);
            recyclerViewCategories.setVisibility(View.GONE);
        } else {
            txtEmptyCategories.setVisibility(View.GONE);
            recyclerViewCategories.setVisibility(View.VISIBLE);
            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList, this);
            recyclerViewCategories.setAdapter(categoryAdapter);
        }
    }
    @Override
    public void editCategory(String categoryID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        //Làm cảnh báo cho người dùng
        builder.setTitle("Enter new Category name");
        final EditText input = new EditText(requireContext());
        input.setHint("Enter new Category name");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()){
                DisplayToast.Display(requireContext(), "Category name can't be empty");
                return;
            }
            categoryService.updateCategory(categoryID, newName);
            loadCategories();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    @Override
    public void deleteCategory(String categoryId) {
        //Làm cảnh báo cho người dùng
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this category ?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    //Gọi deleteCategory và làm mới danh sách
                    categoryService.deleteCategory(categoryId);
                    loadCategories();
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
}