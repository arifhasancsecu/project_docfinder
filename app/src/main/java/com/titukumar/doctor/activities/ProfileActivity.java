package com.titukumar.doctor.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.*;
import com.titukumar.doctor.BaseActivity;
import com.titukumar.doctor.R;
import com.titukumar.doctor.adapter.DocumentAdapter;
import com.titukumar.doctor.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {

    private TextView userName, btnSeeAllDocuments, emptyTextView ;
    private DatabaseReference userReference;
    private String phoneNo;
    private ImageView profileImage;
    private MaterialButton btnUpdateProfile, btnAppointment;
    RecyclerView recyclerView;
    DocumentAdapter documentAdapter;
    List<Doctor> documentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigation(R.id.profile_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emptyTextView = findViewById(R.id.emptyProfileMessage);
        if (!checkLoginWithDialog("Access Your Profile", null)) {
            emptyTextView.setVisibility(View.VISIBLE);
            return;
        }

        initViews();
        initFirebase();
        loadUserData();
        loadDocuments();
    }

    private void initViews() {
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnAppointment = findViewById(R.id.btn_appointment);
        userName = findViewById(R.id.profile_name_id);
        btnSeeAllDocuments = findViewById(R.id.seeAllDocuments);
        profileImage = findViewById(R.id.profile_image_id);
        recyclerView = findViewById(R.id.recyclerViewDocuments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        documentList = new ArrayList<>();
        documentAdapter = new DocumentAdapter(this, documentList);
        recyclerView.setAdapter(documentAdapter);

        btnUpdateProfile.setOnClickListener(view -> goToUpdateProfile());
        btnAppointment.setOnClickListener(view -> goToAppointmentSection());
    }

    private void initFirebase() {
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        phoneNo = preferences.getString("phoneNo", null);

        if (phoneNo == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userReference = FirebaseDatabase.getInstance().getReference("Users").child(phoneNo);
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (phoneNo != null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (name != null) {
                        userName.setText(name);
                    }

                    String profileUrl = snapshot.child("userProfile").getValue(String.class);
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(profileUrl)
                                .placeholder(R.drawable.user_icon)
                                .error(R.drawable.user_icon)
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.user_icon);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDoctorInfo(List<String> doctorIdList) {
        documentList.clear();

        if (doctorIdList.isEmpty()) {
            documentAdapter.notifyDataSetChanged();
            return;
        }

        final int total = doctorIdList.size();
        final int[] loadedCount = {0};

        for (String doctorId : doctorIdList) {
            DatabaseReference officialRef = FirebaseDatabase.getInstance()
                    .getReference("Doctor")
                    .child(doctorId);

            DatabaseReference userAddedRef = FirebaseDatabase.getInstance()
                    .getReference("Doctor")
                    .child("UserAddedDoctor")
                    .child(doctorId);

            officialRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot officialSnap) {
                    if (officialSnap.exists()) {
                        addDoctorFromSnapshot(officialSnap, doctorId);
                        incrementAndNotify();
                    } else {
                        userAddedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnap) {
                                if (userSnap.exists()) {
                                    addDoctorFromSnapshot(userSnap, doctorId);
                                }
                                incrementAndNotify();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                incrementAndNotify();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    incrementAndNotify();
                }

                private void incrementAndNotify() {
                    loadedCount[0]++;
                    if (loadedCount[0] == total) {
                        documentAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void addDoctorFromSnapshot(DataSnapshot snapshot, String doctorId) {
        String doctorName = snapshot.child("name").getValue(String.class);
        String doctorImage = snapshot.child("image").getValue(String.class);
        String doctorDegree = snapshot.child("degree").getValue(String.class);
        String doctorCategory = snapshot.child("category").getValue(String.class);

        Doctor model = new Doctor();
        model.setName(doctorName != null ? doctorName : "Unknown");
        model.setImage(doctorImage != null ? doctorImage : "");
        model.setDegree(doctorDegree != null ? doctorDegree : "");
        model.setCategory(doctorCategory != null ? doctorCategory : "");
        model.setDoctorId(doctorId);

        documentList.add(model);
    }

    private void loadDocuments() {
        if (phoneNo == null) return;

        DatabaseReference docRef = FirebaseDatabase.getInstance()
                .getReference("Documents")
                .child(phoneNo);

        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> doctorIdList = new ArrayList<>();

                for (DataSnapshot docIdSnapshot : snapshot.getChildren()) {
                    String doctorId = docIdSnapshot.getKey();
                    if (doctorId != null && !doctorId.isEmpty()) {
                        doctorIdList.add(doctorId);
                    }
                }

                btnSeeAllDocuments.setVisibility(doctorIdList.size() > 5 ? View.VISIBLE : View.GONE);
                btnSeeAllDocuments.setText(doctorIdList.size() > 5 ? "See more" : "");
                btnSeeAllDocuments.setOnClickListener(v -> {
                    btnSeeAllDocuments.setVisibility(View.GONE);
                    loadAllDoctorIds();
                });

                List<String> limitedDoctorIds = doctorIdList.subList(0, Math.min(5, doctorIdList.size()));
                loadDoctorInfo(limitedDoctorIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load documents", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllDoctorIds() {
        if (phoneNo == null) return;

        DatabaseReference docRef = FirebaseDatabase.getInstance()
                .getReference("Documents")
                .child(phoneNo);

        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> doctorIdList = new ArrayList<>();

                for (DataSnapshot docIdSnapshot : snapshot.getChildren()) {
                    String doctorId = docIdSnapshot.getKey();
                    if (doctorId != null && !doctorId.isEmpty()) {
                        doctorIdList.add(doctorId);
                    }
                }

                loadDoctorInfo(doctorIdList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load all doctor IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToUpdateProfile(){
        Intent intent = new Intent(this, ProfileUpdateActivity.class);
        startActivity(intent);
    }

    private void goToAppointmentSection(){
        Intent intent = new Intent(this, AppointmentActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


}
