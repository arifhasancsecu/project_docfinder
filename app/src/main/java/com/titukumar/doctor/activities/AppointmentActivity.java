package com.titukumar.doctor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.titukumar.doctor.R;
import com.titukumar.doctor.adapter.AppointmentAdapter;
import com.titukumar.doctor.model.Doctor;
import com.titukumar.doctor.model.DoctorAppointment;
import com.titukumar.doctor.util.AppointmentReminderReceiver;
import com.titukumar.doctor.util.NotificationUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AppointmentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;
    private TextView tvAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NotificationUtils.createNotificationChannel(this);
        recyclerView = findViewById(R.id.rvAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyLayout = findViewById(R.id.empty_layout);
        tvAppointment = findViewById(R.id.tvAppointments);
        loadAppointments();

    }


    private void loadAppointments() {
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        String phoneNo = preferences.getString("phoneNo", null);
        if (phoneNo == null) return;

        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                .getReference("Appointments")
                .child(phoneNo);
        DatabaseReference doctorsRef = FirebaseDatabase.getInstance()
                .getReference("Doctor");

        List<DoctorAppointment> doctorAppointments = new ArrayList<>();
        AppointmentAdapter adapter = new AppointmentAdapter(this, doctorAppointments, phoneNo);
        recyclerView.setAdapter(adapter);

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorAppointments.clear();

                // Collecting all unique doctor IDs
                Set<String> doctorIds = new HashSet<>();
                Map<String, String> appointmentDates = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String docId = ds.getKey();
                    String date = ds.child("appointment_date").getValue(String.class);
                    if (docId != null) {
                        doctorIds.add(docId);
                        appointmentDates.put(docId, date != null ? date : "N/A");
                    }
                }

                if (doctorIds.isEmpty()) {
                    tvAppointment.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                Map<String, Doctor> fetchedDoctors = new HashMap<>();
                int[] remaining = {doctorIds.size()};

                for (String docId : doctorIds) {
                    doctorsRef.child(docId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot doctorSnap) {
                            Doctor doctor = doctorSnap.getValue(Doctor.class);
                            if (doctor != null) {
                                doctor.setDoctorId(docId);
                                fetchedDoctors.put(docId, doctor);
                            }
                            remaining[0]--;

                            if (remaining[0] == 0) {
                                doctorAppointments.clear();
                                for (String id : appointmentDates.keySet()) {
                                    Doctor doc = fetchedDoctors.get(id);
                                    if (doc != null) {
                                        String apptDate = appointmentDates.get(id);
                                        doctorAppointments.add(new DoctorAppointment(doc, apptDate));
                                        scheduleAppointmentNotification(AppointmentActivity.this, doc, apptDate);
                                    }
                                }

                                Collections.sort(doctorAppointments, (a, b) -> {
                                    if (a.getAppointmentDate() == null) return 1;
                                    if (b.getAppointmentDate() == null) return -1;
                                    return a.getAppointmentDate().compareTo(b.getAppointmentDate());
                                });

                                adapter.notifyDataSetChanged();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AppointmentActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleAppointmentNotification(Context context,
                                                 Doctor doctor,
                                                 String appointmentDateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(appointmentDateStr);
            if (date == null) return;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            long triggerTime = cal.getTimeInMillis();
            if (triggerTime <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                triggerTime = cal.getTimeInMillis();
            }

            Intent intent = new Intent(context, AppointmentReminderReceiver.class);
            intent.putExtra("doctorName", "Test Doctor");
            intent.putExtra("appointmentDate", "Today");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    12345,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}