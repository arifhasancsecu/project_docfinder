package com.titukumar.doctor.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.titukumar.doctor.R;
import com.titukumar.doctor.activities.DetailsActivity;
import com.titukumar.doctor.model.Doctor;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchDoctorAdapter extends ListAdapter<Doctor, SearchDoctorAdapter.ViewHolder> {

    public SearchDoctorAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Doctor> DIFF_CALLBACK = new DiffUtil.ItemCallback<Doctor>() {
        @Override
        public boolean areItemsTheSame(@NonNull Doctor oldItem, @NonNull Doctor newItem) {
            return oldItem.getDoctorId().equals(newItem.getDoctorId());
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
       String docId = doctor.getDoctorId();

        // Rating
        holder.ratingBar.setRating(2.5f);
        if (docId != null && !docId.isEmpty()) {
            DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                    .getReference("Ratings")
                    .child(docId);

            RatingBar localRatingBar = holder.ratingBar;

            ratingRef.get().addOnSuccessListener(snapshot -> {
                float total = 0;
                int count = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Float rating = ds.child("rating").getValue(Float.class);
                    if (rating != null) {
                        total += rating;
                        count++;
                    }
                }

                float averageRating = count > 0 ? total / count : 2.5f;;
                if (localRatingBar != null) {
                    localRatingBar.setRating(averageRating);
                }
            }).addOnFailureListener(e -> {
                if (localRatingBar != null) localRatingBar.setRating(2.5f);
            });
        } else {
            holder.ratingBar.setRating(0);
        }



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailsActivity.class);
            intent.putExtra("doctor", doctor);
            intent.putExtra("doctorId", doctor.getDoctorId());
            v.getContext().startActivity(intent);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name, degree;
        RatingBar ratingBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.search_doc_img_id);
            name = itemView.findViewById(R.id.search_doc_name_id);
            degree = itemView.findViewById(R.id.search_doc_degree_id);
            ratingBar = itemView.findViewById(R.id.doc_search_layout_ratingBar);

        }
    }
}
