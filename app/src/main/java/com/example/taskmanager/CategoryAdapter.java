package com.example.taskmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<String> categories;
    private OnCategoryClickListener clickListener;

    public CategoryAdapter(List<String> categories, OnCategoryClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryName.setText(category);

        // Configure le clic sur un élément
        holder.itemView.setOnClickListener(v -> clickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<String> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.text_category_name);
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }
}

