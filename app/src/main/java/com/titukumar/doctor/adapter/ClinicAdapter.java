package com.titukumar.doctor.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.titukumar.doctor.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.titukumar.doctor.activities.ClinicDetails;
import com.titukumar.doctor.model.Clinic;

import java.util.ArrayList;
import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.ClinicViewHolder> {
    private final List<Clinic> displayList;
    private List<Clinic> fullList;

    public ClinicAdapter(List<Clinic> clinicList) {
        this.displayList = clinicList;
        this.fullList = new ArrayList<>(clinicList);
    }

    public void updateData(List<Clinic> newData) {
        fullList.clear();
        fullList.addAll(newData);
        displayList.clear();
        displayList.addAll(newData);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayList.addAll(fullList);
        } else {
            String lower = query.toLowerCase();
            for (Clinic c : fullList) {
                if (c.getClinicName() != null &&
                        c.getClinicName().toLowerCase().contains(lower)) {
                    displayList.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClinicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_clinic, parent, false);
        return new ClinicViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClinicViewHolder holder, int position) {
        Clinic clinic = displayList.get(position);
        holder.tvClinicName.setText(clinic.getClinicName());
        holder.tvLocation.setText("Location: " + clinic.getLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ClinicDetails.class);
                intent.putExtra("hospital_name", clinic.getClinicName());
                intent.putExtra("location", clinic.getLocation());
                ArrayList<String> hotlineList = new ArrayList<>();
                if (clinic.getHotlines() != null && !clinic.getHotlines().isEmpty()) {
                    hotlineList.addAll(clinic.getHotlines());
                }
                intent.putStringArrayListExtra("hotlines", hotlineList);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class ClinicViewHolder extends RecyclerView.ViewHolder {
        TextView tvClinicName, tvLocation;

        ClinicViewHolder(View itemView) {
            super(itemView);
            tvClinicName = itemView.findViewById(R.id.tvClinicName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
