package com.example.vcampusexpenses.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.adapters.CategoryAdapter;
import com.example.vcampusexpenses.database.UserDB;
import com.example.vcampusexpenses.methods.CategoryMethod;
import com.example.vcampusexpenses.model.Category;
import java.util.List;

public class CategoriesFragment extends Fragment {
    private RecyclerView recyclerViewCategories;
    private CategoryMethod categoryMethod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        UserDB userDB = new UserDB();
        String userId = userDB.getCurrentUserId();
        categoryMethod = new CategoryMethod(requireContext(), userId);
        recyclerViewCategories = view.findViewById(R.id.rvCategory);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
//        loadCategories();
        return view;
    }


    //Lỗi null pointer tại txtEmptyCategories
    private void loadCategories() {
        List<Category> categoryList = categoryMethod.getListCategories();
        TextView txtEmptyCategories = getView().findViewById(R.id.txtEmptyCategories);

        if (categoryList.isEmpty()) {
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