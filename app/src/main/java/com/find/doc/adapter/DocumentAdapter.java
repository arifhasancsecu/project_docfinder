package com.find.doc.adapter;
import android.content.Context;

import android.content.Intent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.find.doc.R;

import java.util.List;
import com.find.doc.activities.DocumentDetailsActivity;
import com.find.doc.model.Doctor;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private List<Doctor> documentList;

    public DocumentAdapter(Context context, List<Doctor> documentList) {
        this.context = context;
        this.documentList = documentList;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doc_item, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Doctor doc = documentList.get(position);

        holder.doctorName.setText(doc.getName() != null ? doc.getName() : "Unknown");

        if (doc.getDegree() != null && !doc.getDegree().isEmpty()) {
            holder.docDegree.setText(doc.getDegree());
        } else if (doc.getCategory() != null && !doc.getCategory().isEmpty()) {
            holder.docDegree.setText(doc.getCategory());
        } else {
            holder.docDegree.setText("N/A");
        }

        Glide.with(context)
                .load(doc.getImage() != null && !doc.getImage().isEmpty() ? doc.getImage() : null)
                .placeholder(R.drawable.doc_icon)
                .into(holder.doctorImage);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DocumentDetailsActivity.class);
            intent.putExtra("doctorName", doc.getName());
            intent.putExtra("doctorImage", doc.getImage());
            intent.putExtra("doctorId", doc.getDoctorId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        ImageView doctorImage;
        TextView doctorName, docDegree;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorImage = itemView.findViewById(R.id.item_doctor_image);
            doctorName = itemView.findViewById(R.id.item_doctor_name);
            docDegree = itemView.findViewById(R.id.item_doc_degree);
        }
    }


}
