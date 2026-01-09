package com.titukumar.doctor.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.titukumar.doctor.BaseActivity;
import com.titukumar.doctor.R;
import com.titukumar.doctor.model.Appointment;
import com.titukumar.doctor.model.Doctor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailsActivity extends BaseActivity {

    private TextView nameTv;
    private TextView hospitalNameTv;
    private TextView chamberTv;
    private TextView serialNumberTv;
    private TextView feeTv;
    private TextView docDegree;
    private TextView appointmentMsg;
    private ImageView docImage;
    private ImageView btncall;
    private MaterialButton btnAppointmentConfirm;
    private String docId;
    private Doctor doctor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setupBottomNavigation(0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTv = findViewById(R.id.tv_doctor_name);
        hospitalNameTv = findViewById(R.id.tv_hospital_name);
        chamberTv = findViewById(R.id.tv_chamber);
        serialNumberTv = findViewById(R.id.tv_serial);
        feeTv = findViewById(R.id.tv_visiting_fee);
        docDegree = findViewById(R.id.tv_doc_degree);
        docImage = findViewById(R.id.doc_image_id);
        btncall = findViewById(R.id.btncall);
        btnAppointmentConfirm = findViewById(R.id.btn_appointment_confirm);
        appointmentMsg = findViewById(R.id.tvAppointmentMsg);
        docId = getIntent().getStringExtra("doctorId");

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");
        if (doctor != null) {
            populateViews(doctor);
        } else {
            Toast.makeText(this, "Doctor data not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        serialNumberTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });


        btncall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        btnAppointmentConfirm.setOnClickListener(v -> appointmentConfirmation());
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void makePhoneCall() {
        Object serialObj = doctor.getSerialNumber();
        List<String> hotlines = new ArrayList<>();

        if (serialObj != null) {
            if (serialObj instanceof List) {
                for (Object obj : (List<?>) serialObj) {
                    if (obj != null) {
                        String num = obj.toString().trim();
                        if (!num.isEmpty()) {
                            hotlines.add(num);
                        }
                    }
                }
            } else if (serialObj instanceof String) {
                String serialStr = ((String) serialObj).trim();
                if (!serialStr.isEmpty()) {
                    String[] numbers = serialStr.split("[,\\s]+");
                    for (String num : numbers) {
                        num = num.trim();
                        if (!num.isEmpty()) {
                            hotlines.add(num);
                        }
                    }
                }
            }
        }

        if (hotlines.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
        // For single number, direct call
        else if (hotlines.size() == 1) {
            callFunction(hotlines.get(0));
        }
        // Otherwise, showing dialog to select
        else {
            showHotlineDialog(hotlines);
        }
    }



    private void showHotlineDialog(List<String> hotlines) {
        if (hotlines == null || hotlines.isEmpty()) {
            Toast.makeText(this, "No number available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] hotlineArray = hotlines.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                hotlineArray
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(ContextCompat.getColor(DetailsActivity.this, android.R.color.black));
                tv.setPadding(32, 24, 32, 24);
                tv.setTextSize(16);
                return view;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Select a number")
                .setAdapter(adapter, (dialog, which) -> {
                    String selectedNumber = hotlineArray[which];
                    callFunction(selectedNumber);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void callFunction(String number) {
        if (number == null || number.isEmpty()) {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intentCall = new Intent(Intent.ACTION_CALL);
        intentCall.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestCallPermission();
        } else {
            startActivity(intentCall);
        }
    }

    private void requestCallPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CALL_PHONE},
                1001
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission denied to make phone calls", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void populateViews(Doctor doctor) {
        String defaultUrl = "https://firebasestorage.googleapis.com/v0/b/doctor-f506d.appspot.com/o/doc.png?alt=media&token=36b2a113-8094-40c0-ad0e-e6b496840f96";
        String doctorUrl = doctor.getImage();
        if (doctorUrl != null && doctorUrl.equals(defaultUrl)) {
            Glide.with(docImage)
                    .load(R.drawable.doc_icon)
                    .circleCrop()
                    .into(docImage);
        } else {
            Glide.with(docImage)
                    .load(doctorUrl)
                    .placeholder(R.drawable.doc_icon)
                    .circleCrop()
                    .error(R.drawable.doc_icon)
                    .into(docImage);
        }

        nameTv.setText(doctor.getName());
        docDegree.setText(doctor.getDegree());
        hospitalNameTv.setText(doctor.getHospital());
        chamberTv.setText("Location: " + doctor.getLocation());

        // Both single and multiple serial numbers
        String displaySerial = "";
        try {
            Object serialObj = doctor.getSerialNumber();

            if (serialObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> serialList = (List<String>) serialObj;
                displaySerial = TextUtils.join(", ", serialList);
            } else if (serialObj instanceof String) {
                displaySerial = (String) serialObj;
            } else if (serialObj != null) {
                displaySerial = serialObj.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            displaySerial = "N/A";
        }

        serialNumberTv.setText(displaySerial);
        feeTv.setText("Visiting Fee: " + doctor.getVisitingFee());
    }

    private void appointmentConfirmation(){
        if (!checkLoginWithDialog("To Access Appointment", null)) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Appointment")
                .setMessage("Have you taken the Appointment?")
                .setIcon(R.drawable.ic_gray_appntment)
                .setPositiveButton("Yes", (dialog, which) -> {
                    showDatePicker();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String dateStr = sdf.format(selectedDate.getTime());

                    saveAppointmentToDatabase(docId, dateStr);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }


    private void saveAppointmentToDatabase(String docId, String dateString){
        if (docId == null || docId.trim().isEmpty()) {
            Toast.makeText(this, "Doctor ID missing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dateString == null || dateString.trim().isEmpty()) {
            Toast.makeText(this, "Date is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        String phoneNo = preferences.getString("phoneNo", null);

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Appointments")
                .child(phoneNo);

        long now = System.currentTimeMillis();
        Appointment appointment = new Appointment(dateString, now, now);

        dbRef.child(docId)
                .setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    btnAppointmentConfirm.setVisibility(View.GONE);
                    appointmentMsg.setText("Appointment booked on: " + dateString);
                    new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            showRatingDialog(docId);
                        }
                    }, 1000);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to book: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void showRatingDialog(String docId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

        new AlertDialog.Builder(this)
                .setTitle("Rate your Appointment")
                .setMessage("How was your experience with the doctor?")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    saveRatingToDatabase(docId, rating);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    private void saveRatingToDatabase(String docId, float rating){
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        String phoneNo = preferences.getString("phoneNo", null);
        if (phoneNo == null) return;

        DatabaseReference ratingRef = FirebaseDatabase.getInstance()
                .getReference("Ratings")
                .child(docId)
                .child(phoneNo);

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("timestamp", System.currentTimeMillis());

        ratingRef.setValue(ratingData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



}