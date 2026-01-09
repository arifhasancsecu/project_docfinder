package com.titukumar.doctor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.titukumar.doctor.R;
import com.titukumar.doctor.adapter.ClinicAdapter;
import com.titukumar.doctor.model.Clinic;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ClinicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ClinicAdapter adapter;
    private List<Clinic> clinicList;
    private DatabaseReference clinicRef;
    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchEditText = findViewById(R.id.searchEditTextClinic);

        clinicList = new ArrayList<>();
        adapter = new ClinicAdapter(clinicList);
        recyclerView.setAdapter(adapter);

        clinicRef = FirebaseDatabase.getInstance().getReference("Clinic");

        clinicRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Clinic> freshList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Clinic clinic = ds.getValue(Clinic.class);
                    if (clinic != null) freshList.add(clinic);
                }
                adapter.updateData(freshList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClinicActivity.this,
                        "Failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

    }
}
