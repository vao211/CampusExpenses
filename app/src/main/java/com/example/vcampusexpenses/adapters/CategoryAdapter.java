package com.example.vcampusexpenses.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<Category> categoryList;
    private final OnCategoryClickListener listenter;

    public interface OnCategoryClickListener{
        void editCategory(String categoryID);
        void deleteCategory(String categoryId);
    }
    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listenter) {
        this.categoryList = categoryList;
        this.listenter = listenter;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category,
                parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
//        holder.tvCategoryId.setText("ID: " + category.getCategoryId());
        holder.tvCategoryName.setText(category.getName());
        holder.btnEditItemCategory.setOnClickListener(view -> {
            listenter.editCategory(category.getCategoryId());
        });
        holder.btnDeleteItemCategory.setOnClickListener(view -> {
            listenter.deleteCategory(category.getCategoryId());
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryId;
        TextView tvCategoryName;
        ImageButton btnEditItemCategory, btnDeleteItemCategory;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
//            tvCategoryId = itemView.findViewById(R.id.txtCategoryId);
            tvCategoryName = itemView.findViewById(R.id.txt_categoryName);
            btnEditItemCategory = itemView.findViewById(R.id.btn_editItemCategory);
            btnDeleteItemCategory = itemView.findViewById(R.id.btn_deleteItemCategory);
        }
    }
}