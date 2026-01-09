package com.titukumar.doctor.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.titukumar.doctor.R;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddDoctorActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtSpeciality;
    private MaterialButton btnProceed;
    private ProgressBar progressBar;

    private DatabaseReference doctorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);

        edtName = findViewById(R.id.add_doc_name_id);
        edtSpeciality = findViewById(R.id.add_doc_speciality_id);
        btnProceed = findViewById(R.id.btn_proceed_add_doc_id);
        progressBar = findViewById(R.id.add_doc_progressBar);

        doctorsRef = FirebaseDatabase.getInstance()
                .getReference("Doctor")
                .child("UserAddedDoctor");

        btnProceed.setOnClickListener(v -> saveDoctor());
    }

    private void saveDoctor() {
        String name = Objects.requireNonNull(edtName.getText(), "Name input is null").toString().trim();
        String speciality = Objects.requireNonNull(edtSpeciality.getText(), "Speciality input is null").toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Doctor name required");
            edtName.requestFocus();
            return;
        }
        if (name.length() < 3) {
            edtName.setError("Name must be at least 3 characters");
            edtName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(speciality)) {
            edtSpeciality.setError("Speciality required");
            edtSpeciality.requestFocus();
            return;
        }
        if (speciality.length() < 3) {
            edtSpeciality.setError("Speciality must be at least 3 characters");
            edtSpeciality.requestFocus();
            return;
        }

        btnProceed.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String docId = doctorsRef.push().getKey();
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("name", name);
        doctorData.put("category", speciality);
        doctorData.put("created_at", System.currentTimeMillis());
        doctorData.put("updated_at", System.currentTimeMillis());

        if (docId != null) {
            doctorsRef.child(docId).setValue(doctorData)
                    .addOnSuccessListener(unused -> {
                        if (!isFinishing()) {
                            edtName.setText("");
                            edtSpeciality.setText("");
                            resetUI();

                            Intent intent = new Intent(AddDoctorActivity.this, DocumentShareActivity.class);
                            intent.putExtra("doctorId", docId);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isFinishing()) {
                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            resetUI();
                        }
                    });
        } else {
            resetUI();
        }
    }

    private void resetUI() {
        progressBar.setVisibility(View.GONE);
        btnProceed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
        String speciality = edtSpeciality.getText() != null ? edtSpeciality.getText().toString().trim() : "";

        if (!name.isEmpty() || !speciality.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard changes?")
                    .setIcon(R.drawable.alert_icon)
                    .setMessage("You have unsaved changes. Do you really want to go back?")
                    .setPositiveButton("Yes", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }


}
