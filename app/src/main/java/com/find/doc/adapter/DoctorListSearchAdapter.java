package com.find.doc.adapter;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.find.doc.activities.DetailsActivity;
import com.find.doc.activities.DocumentShareActivity;
import com.find.doc.model.Doctor;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.find.doc.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorListSearchAdapter extends ListAdapter<Doctor, DoctorListSearchAdapter.ViewHolder> {

    private final String sourceScreen;

    public DoctorListSearchAdapter(String sourceScreen) {
        super(DIFF_CALLBACK);
        this.sourceScreen = sourceScreen;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_search_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doctor = getItem(position);
        holder.name.setText(doctor.getName());
        holder.degree.setText(doctor.getDegree());

        Glide.with(holder.image.getContext())
                .load(doctor.getImage())
                .placeholder(R.drawable.doc_icon)
                .circleCrop()
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if ("add".equals(sourceScreen)) {
                intent = new Intent(v.getContext(), DocumentShareActivity.class);
            } else {
                intent = new Intent(v.getContext(), DetailsActivity.class);
            }
            intent.putExtra("doctor", doctor);
            intent.putExtra("doctorId", doctor.getDoctorId());
            v.getContext().startActivity(intent);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, degree;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.search_doc_img_id);
            name = itemView.findViewById(R.id.search_doc_name_id);
            degree = itemView.findViewById(R.id.search_doc_degree_id);
        }
    }

    public static final DiffUtil.ItemCallback<Doctor> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Doctor>() {
                @Override
                public boolean areItemsTheSame(@NonNull Doctor oldItem, @NonNull Doctor newItem) {
                    return oldItem.getDoctorId() != null &&
                            oldItem.getDoctorId().equals(newItem.getDoctorId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Doctor oldItem, @NonNull Doctor newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getDegree().equals(newItem.getDegree()) &&
                            oldItem.getHospital().equals(newItem.getHospital()) &&
                            oldItem.getCategory().equals(newItem.getCategory()) &&
                            oldItem.getImage().equals(newItem.getImage());
                }
            };
}
