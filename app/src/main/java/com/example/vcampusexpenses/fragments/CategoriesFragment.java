package com.example.vcampusexpenses.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.CategoryAdapter;
import com.example.vcampusexpenses.database.UserDB;
import com.example.vcampusexpenses.methods.CategoryMethod;
import com.example.vcampusexpenses.model.Category;
import com.example.vcampusexpenses.utils.DisplayToast;

import java.util.List;

public class CategoriesFragment extends Fragment {
    private RecyclerView recyclerViewCategories;
    private TextView txtEmptyCategories;
    private CategoryMethod categoryMethod;

    private ImageButton btnAddCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerViewCategories = view.findViewById(R.id.rvCategory);
        txtEmptyCategories = view.findViewById(R.id.txtEmptyCategories);

        UserDB userDB = new UserDB();
        String userId = userDB.getCurrentUserId();
        categoryMethod = new CategoryMethod(requireContext(), userId);
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
        });
    }
    private void loadCategories() {
        if (txtEmptyCategories == null || recyclerViewCategories == null) {
            return;
        }

        List<Category> categoryList = categoryMethod.getListCategories();

        if (categoryList == null || categoryList.isEmpty()) {
            txtEmptyCategories.setVisibility(View.VISIBLE);
            recyclerViewCategories.setVisibility(View.GONE);
        } else {
            txtEmptyCategories.setVisibility(View.GONE);
            recyclerViewCategories.setVisibility(View.VISIBLE);
            CategoryAdapter categoryAdapter = new CategoryAdapter(categoryList);
            recyclerViewCategories.setAdapter(categoryAdapter);
        }
    }
}