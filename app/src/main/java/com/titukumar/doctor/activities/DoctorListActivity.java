package com.titukumar.doctor.activities;

import static com.titukumar.doctor.model.Location.getLocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.titukumar.doctor.BaseActivity;
import com.titukumar.doctor.R;
import com.titukumar.doctor.adapter.DoctorAdapter;
import com.titukumar.doctor.model.Doctor;
import com.titukumar.doctor.model.Location;
import com.titukumar.doctor.util.LocationUtil;

import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends BaseActivity {

    private RecyclerView doctorListRv;
    private DoctorAdapter doctorAdapter;
    private TextInputEditText searchDoc;
    private String sourceScreen = "home";
    private LinearLayout emptyLayout;
    private Button btnAddDoctor;
    private String categoryName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        String from = getIntent().getStringExtra("sending");
        if ("add".equals(from)) {
            setupBottomNavigation(R.id.add);
            Snackbar.make(findViewById(android.R.id.content),
                            "Select the Doctor to Upload Prescription or Report",
                            Snackbar.LENGTH_LONG)
                    .show();
        } else {
            setupBottomNavigation(R.id.all_doctor);
        }

        if (getIntent().getExtras() != null) {
            sourceScreen = getIntent().getStringExtra("from");
            Object categoryObj = getIntent().getExtras().get("category");
            categoryName = categoryObj == null ? null : categoryObj.toString();
        }
        if (sourceScreen == null) {
            sourceScreen = "home";
        }

        doctorListRv = findViewById(R.id.recycler_view_doctor);
        doctorListRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        searchDoc = findViewById(R.id.search_doc_id);
        emptyLayout = findViewById(R.id.empty_layout);
        btnAddDoctor = findViewById(R.id.btn_add_doctor);

        btnAddDoctor.setVisibility("add".equals(from) ? View.VISIBLE : View.GONE);
        btnAddDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorListActivity.this, AddDoctorActivity.class);
            startActivity(intent);
        });


        populateDoctorList(categoryName);

        searchDoc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (categoryName != null && !categoryName.isEmpty()) {
                    searchDoctorsByCategory(charSequence.toString().trim());
                }else{
                    filterDoctorsLocally(charSequence.toString().trim());
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void populateDoctorList(String categoryName) {
        String locationScreen = getIntent().getStringExtra("locationScreen");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Doctor");

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedLocation = prefs.getString("saved_location", null);

        Location selectedLocation = null;
        if (savedLocation != null && !savedLocation.isEmpty()) {
            try {
                selectedLocation = Location.valueOf(savedLocation);
            } catch (IllegalArgumentException e) {
                selectedLocation = null;
            }
        }

        Query query;

        if ("LocationActivity".equals(locationScreen)) {
            if (selectedLocation == null) {
                if (categoryName == null || categoryName.isEmpty()) {
                    query = reference;
                } else {
                    query = reference.orderByChild("category")
                            .equalTo(categoryName);
                }
            } else {
                if (categoryName == null || categoryName.isEmpty()) {
                    query = reference.orderByChild("zone")
                            .equalTo(selectedLocation.getDisplayName());
                } else {
                    query = reference.orderByChild("zone_category")
                            .equalTo(selectedLocation.getDisplayName() + "_" + categoryName);
                }
            }
        } else {
            if (categoryName != null && !categoryName.isEmpty()) {
                if (selectedLocation != null) {
                    query = reference.orderByChild("zone_category")
                            .equalTo(selectedLocation.getDisplayName() + "_" + categoryName);
                } else {
                    query = reference.orderByChild("category")
                            .equalTo(categoryName);
                }
            } else {
                query = reference;
            }
        }


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null && task.getResult().hasChildren()) {
                    emptyLayout.setVisibility(View.GONE);
                    doctorListRv.setVisibility(View.VISIBLE);
                } else {
                    emptyLayout.setVisibility(View.VISIBLE);
                    doctorListRv.setVisibility(View.GONE);
                }
            } else {
                emptyLayout.setVisibility(View.VISIBLE);
                doctorListRv.setVisibility(View.GONE);
            }
        });

        FirebaseRecyclerOptions<Doctor> doctorList =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(query, Doctor.class)
                        .build();

        doctorAdapter = new DoctorAdapter(doctorList, sourceScreen);
        doctorListRv.setAdapter(doctorAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (doctorAdapter != null) {
            doctorAdapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doctorAdapter != null) {
            doctorAdapter.stopListening();
        }
    }

    private void filterDoctorsLocally(String keyword) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Doctor");
        Location location = getLocation(LocationUtil.locationPosition);
        if (location == null) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Query query = reference.orderByChild("zone")
                .equalTo(location.getDisplayName());

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Doctor> filteredList = new ArrayList<>();
                String lowerKeyword = keyword == null ? "" : keyword.toLowerCase();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Doctor doctor = snapshot.getValue(Doctor.class);
                    if (doctor != null && doctor.getName().toLowerCase().contains(lowerKeyword)) {
                        filteredList.add(doctor);
                    }
                }

                if (filteredList.isEmpty()) {
                    emptyLayout.setVisibility(View.VISIBLE);
                    doctorListRv.setVisibility(View.GONE);
                } else {
                    emptyLayout.setVisibility(View.GONE);
                    doctorListRv.setVisibility(View.VISIBLE);
                }

                doctorAdapter.updateWithCustomList(filteredList);
            }
        });

    }



    //Search only doctors that belong to the currently selected category
    private void searchDoctorsByCategory(String keyword) {
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this,
                    "No category selected for category search.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Doctor");
        Query query = reference.orderByChild("category").equalTo(categoryName);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                Toast.makeText(this, "Search failed.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Doctor> filteredList = new ArrayList<>();
            String lowerKeyword = keyword == null ? "" : keyword.toLowerCase();

            for (DataSnapshot snap : task.getResult().getChildren()) {
                Doctor doctor = snap.getValue(Doctor.class);
                if (doctor == null) continue;

                if (!lowerKeyword.isEmpty()) {
                    if (doctor.getName() == null ||
                            !doctor.getName().toLowerCase().contains(lowerKeyword)) {
                        continue;
                    }
                }

                filteredList.add(doctor);
            }

            if (filteredList.isEmpty()) {
                emptyLayout.setVisibility(View.VISIBLE);
                doctorListRv.setVisibility(View.GONE);
            } else {
                emptyLayout.setVisibility(View.GONE);
                doctorListRv.setVisibility(View.VISIBLE);
            }
            if (doctorAdapter != null) {
                doctorAdapter.updateWithCustomList(filteredList);
            }
        });
    }


}