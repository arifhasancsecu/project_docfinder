package com.titukumar.doctor.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.titukumar.doctor.R;
import com.titukumar.doctor.activities.DetailsActivity;
import com.titukumar.doctor.activities.DocumentShareActivity;
import com.titukumar.doctor.model.Doctor;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DoctorAdapter extends FirebaseRecyclerAdapter<Doctor, DoctorAdapter.myViewHolder> {

    private final String sourceScreen;
    private List<Doctor> customList = null;
    private boolean isCustomListActive = false;

    public DoctorAdapter(@NonNull FirebaseRecyclerOptions<Doctor> options, String sourceScreen) {
        super(options);
        this.sourceScreen = sourceScreen;
    }

    public void updateWithCustomList(List<Doctor> doctors) {
        this.customList = doctors;
        this.isCustomListActive = (doctors != null);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (isCustomListActive && customList != null) {
            return customList.size();
        }
        return super.getItemCount();
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        if (isCustomListActive && customList != null) {
            Doctor doctor = customList.get(position);
            holder.bind(doctor, doctor.getDoctorId());
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Doctor doctor) {
        String doctorId = getRef(position).getKey();
        holder.bind(doctor, doctorId);

        // Rating
        holder.ratingBar.setRating(2.5f);
        if (doctorId != null && !doctorId.isEmpty()) {
            DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                    .getReference("Ratings")
                    .child(doctorId);

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

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_doctor, parent, false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView name, degree;
        Doctor doctor;
        String doctorId;
        RatingBar ratingBar;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image1);
            name = itemView.findViewById(R.id.text1);
            degree = itemView.findViewById(R.id.text2);
            ratingBar = itemView.findViewById(R.id.doc_layout_ratingBar);


            itemView.setOnClickListener(view -> {
                Intent intent;
                if ("fab".equals(sourceScreen)) {
                    intent = new Intent(itemView.getContext(), DocumentShareActivity.class);
                } else {
                    intent = new Intent(itemView.getContext(), DetailsActivity.class);
                }
                intent.putExtra("doctor", doctor);
                intent.putExtra("doctorId", doctorId);
                itemView.getContext().startActivity(intent);
            });
        }

        public void bind(Doctor doctor, String docId) {
            this.doctor = doctor;
            this.doctorId = docId;
            doctor.setDoctorId(docId);

            if ("UserAddedDoctor".equals(doctorId)) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0,0));
                return;
            }

            name.setText(doctor.getName());
            degree.setText(doctor.getDegree());

            String defaultUrl = "https://firebasestorage.googleapis.com/v0/b/doctor-f506d.appspot.com/o/doc.png?alt=media&token=36b2a113-8094-40c0-ad0e-e6b496840f96";
            String doctorUrl = doctor.getImage();
            if (doctorUrl != null && doctorUrl.equals(defaultUrl)) {
                Glide.with(imageView)
                        .load(R.drawable.doc_icon)
                        .circleCrop()
                        .into(imageView);
            } else {
                Glide.with(imageView)
                        .load(doctorUrl)
                        .placeholder(R.drawable.doc_icon)
                        .circleCrop()
                        .error(R.drawable.doc_icon)
                        .into(imageView);
            }
        }
    }
}
