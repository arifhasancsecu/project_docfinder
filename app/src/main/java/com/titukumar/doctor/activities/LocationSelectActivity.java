package com.titukumar.doctor.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;
import com.titukumar.doctor.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationSelectActivity extends AppCompatActivity {

    private static final int LOCATION_REQ = 101;

    private ArrayAdapter<String> adapter;
    private final ArrayList<String> locationNames = new ArrayList<>();
    private ListView listView;
    private TextInputEditText searchEditText;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_select);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.location_list);
        searchEditText = findViewById(R.id.searchEditText);

        String[] cities = getResources().getStringArray(R.array.location_array);
        for (String city : cities) {
            if (!city.equalsIgnoreCase("Select Location")) {
                locationNames.add(city.trim());
            }
        }

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                locationNames
        );
        listView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String displayName = adapter.getItem(position);
            if (displayName != null && displayName.startsWith("📍 ")) {
                displayName = displayName.replace("📍 ", "");
            }
            com.titukumar.doctor.model.Location selected =
                    com.titukumar.doctor.model.Location.fromDisplayName(displayName);

            if (selected == null) {
                Toast.makeText(this, "Invalid location selected", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putString("saved_location", selected.name()).apply();

            Intent intent = new Intent(this, DoctorListActivity.class);
            intent.putExtra("selected_location_enum", selected.name());
            intent.putExtra("locationScreen", "LocationActivity");
            startActivity(intent);
            finish();
        });

        // Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Auto detect district
        detectAndSetLocation();
    }


    private void detectAndSetLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ
            );
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) return;

                    try {
                        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1
                        );

                        if (addresses == null || addresses.isEmpty()) return;

                        Address address = addresses.get(0);
                        String district = address.getSubAdminArea();
                        if (district != null) {
                            district = district.replace(" District", "").trim();

                            district = district.replace(" ", "");

                            if (district.equalsIgnoreCase("MoulviBazar")) {
                                district = "Moulvibazar";
                            }

                            // Move to top
                            locationNames.remove(district);
                            locationNames.add(0, "📍 " + district);
                            adapter.notifyDataSetChanged();

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to detect location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location detection failed", Toast.LENGTH_SHORT).show()
                );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectAndSetLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
