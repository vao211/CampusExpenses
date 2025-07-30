package com.example.vcampusexpenses.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vcampusexpenses.R;
import com.example.vcampusexpenses.model.Category;

import java.util.List;

public class CategoryRadioAdapter extends RecyclerView.Adapter<CategoryRadioAdapter.ViewHolder> {

    private final List<Category> categoryList;
    private int selectedPosition = -1;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category selectedCategory);
    }

    public CategoryRadioAdapter(List<Category> categoryList, OnCategorySelectedListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView categoryName;
        public ViewHolder(View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_button);
            categoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }

    @Override
    public CategoryRadioAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_radio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryRadioAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getName());
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onCategorySelected(category);
        });

        holder.radioButton.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onCategorySelected(category);
        });
    }
    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}

