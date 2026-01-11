package com.find.doc.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.find.doc.R;
import com.find.doc.activities.DoctorListActivity;
import com.find.doc.model.CategoryEnum;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    private List<CategoryEnum> categories;
    private boolean isVertical;

    public CategoryAdapter(List<CategoryEnum> categories, boolean isVertical) {
        this.categories = categories;
        this.isVertical = isVertical;
    }

    public void updateList(List<CategoryEnum> filtered) {
        categories = filtered;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isVertical
                ? R.layout.recycler_item_category_vertical
                : R.layout.recycler_item_category;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CategoryEnum category = categories.get(position);

        holder.categoryTv.setText(category.getDisplayName());
        holder.imageView1.setImageResource(category.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            Intent doctorDetailsIntent = new Intent(holder.itemView.getContext(), DoctorListActivity.class);
            doctorDetailsIntent.putExtra("category", category.getDisplayName());
            holder.itemView.getContext().startActivity(doctorDetailsIntent);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView1;
        TextView categoryTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView1 = itemView.findViewById(R.id.image);
            categoryTv = itemView.findViewById(R.id.text);
        }
    }
}
