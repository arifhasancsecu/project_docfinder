package com.find.doc.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.find.doc.R;
import com.find.doc.model.DoctorAppointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private final List<DoctorAppointment> appointments;
    private final Context context;
    private final String userPhone;

    public AppointmentAdapter(Context context, List<DoctorAppointment> appointments, String userPhone) {
        this.context = context;
        this.appointments = appointments;
        this.userPhone = userPhone;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_layout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorAppointment item = appointments.get(position);

        // Displaying the appointment date
        String appointmentDateStr = item.getAppointmentDate();
        if (appointmentDateStr != null && !appointmentDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdfStored = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date date = sdfStored.parse(appointmentDateStr);
                SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String displayDate = date != null ? sdfDisplay.format(date) : appointmentDateStr;
                holder.dateText.setText("Appointment on: " + displayDate);
            } catch (ParseException e) {
                e.printStackTrace();
                holder.dateText.setText("Appointment on: " + appointmentDateStr);
            }
        } else {
            holder.dateText.setText("Appointment date N/A");
        }

        // Set status
        holder.appointStatus.setText(getAppointmentStatus(appointmentDateStr));

        if(item.getDoctor() != null) {
            holder.docName.setText(item.getDoctor().getName());
            String imageUrl = item.getDoctor().getImage();
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl != null ? imageUrl : "")
                    .placeholder(R.drawable.doc_icon)
                    .into(holder.docImage);

        } else {
            holder.docName.setText("Unknown");
            holder.docImage.setImageResource(R.drawable.ic_placeholder);
        }

        holder.itemView.setOnLongClickListener(v -> {
            showOptionsBottomSheet(v.getContext(), item);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, docName, appointStatus;
        ImageView docImage;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.appointment_dateId);
            docName = itemView.findViewById(R.id.appointment_docname);
            docImage = itemView.findViewById(R.id.appointment_docimage);
            appointStatus = itemView.findViewById(R.id.appmnt_statusId);
        }
    }

    private String getAppointmentStatus(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "N/A";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date appointmentDate = sdf.parse(dateStr);
            Date today = new Date();

            if (appointmentDate == null) return "Invalid date";

            if (appointmentDate.after(today)) return "Pending";
            if (isSameDay(appointmentDate, today)) return "Today";
            return "Completed";

        } catch (ParseException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void showOptionsBottomSheet(Context context, DoctorAppointment item) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView edit = sheetView.findViewById(R.id.bs_edit);
        TextView delete = sheetView.findViewById(R.id.bs_delete);

        edit.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openEditScreen(item);
        });

        delete.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            confirmDelete(item);
        });

        bottomSheetDialog.show();
    }

    private void confirmDelete(DoctorAppointment item) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Appointment")
                .setIcon(R.drawable.alert_icon)
                .setMessage("Are you sure you want to delete this appointment?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    DatabaseReference dbRef = FirebaseDatabase.getInstance()
                            .getReference("Appointments")
                            .child(userPhone)
                            .child(item.getDoctor().getDoctorId());

                    dbRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Appointment deleted", Toast.LENGTH_SHORT).show();
                            appointments.remove(item);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openEditScreen(DoctorAppointment item) {
        String currentDateStr = item.getAppointmentDate();
        Calendar calendar = Calendar.getInstance();

        if (currentDateStr != null && !currentDateStr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                Date date = sdf.parse(currentDateStr);
                if (date != null) calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String newDateStr = sdf.format(selectedDate.getTime());

                    DatabaseReference dbRef = FirebaseDatabase.getInstance()
                            .getReference("Appointments")
                            .child(userPhone)
                            .child(item.getDoctor().getDoctorId());

                    dbRef.child("appointment_date").setValue(newDateStr)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Appointment date updated", Toast.LENGTH_SHORT).show();
                                    // Updating local list and refresh RecyclerView
                                    item.setAppointmentDate(newDateStr);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context, "Failed to update date", Toast.LENGTH_SHORT).show();
                                }
                            });

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }


}
