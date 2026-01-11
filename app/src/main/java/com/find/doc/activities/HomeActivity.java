package com.find.doc.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.find.doc.R;
import com.find.doc.adapter.CategoryAdapter;
import com.find.doc.adapter.DoctorAdapter;
import com.find.doc.adapter.SearchDoctorAdapter;
import com.find.doc.model.CategoryEnum;
import com.find.doc.model.Doctor;
import com.find.doc.model.Location;
import com.find.doc.util.DoctorRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private DoctorAdapter doctorAdapter;
    private RecyclerView categoryRv, searchForRecycleView;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    private TextView navHeaderName;
    private TextView navHeaderPhone;
    private ImageView navHeaderImage;

    private boolean backPressedOnce = false;
    private MaterialButtonToggleGroup langToggle;
    private TextInputEditText searchEditText;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final long SEARCH_DELAY = 300;
    private Runnable searchRunnable;
    private SearchDoctorAdapter searchAdapter;
    private MaterialButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupDrawer();
        setupBottomNavigation();

        searchForRecycleView = findViewById(R.id.searchRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);

        searchAdapter = new SearchDoctorAdapter();
        searchForRecycleView.setAdapter(searchAdapter);
        searchForRecycleView.setLayoutManager(new LinearLayoutManager(this));
        searchForRecycleView.setVisibility(View.GONE);

        // Fetching all doctors for searching purpose
        fetchAllDoctors();
        setupSearchBar();

        View rootLayout = findViewById(R.id.scroll_view);
        rootLayout.setOnTouchListener((v, event) -> {
            if (searchEditText.hasFocus()) {
                searchEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
            return false;
        });

        fab = findViewById(R.id.fab_id);
        fab.setOnClickListener(view -> {
            fab.animate().rotationBy(360).setDuration(300).start();
            handleUploadNavigation();
        });


        langToggle = findViewById(R.id.langToggle);
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");

        if (lang.equals("en")) {
            langToggle.check(R.id.btnEng);
        } else {
            langToggle.check(R.id.btnBan);
        }

        langToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnEng) {
                    setLocale("en");
                } else if (checkedId == R.id.btnBan) {
                    setLocale("bn");
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (doctorAdapter != null) doctorAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchEditText.clearFocus();
        populateCategoryAndDoctorRv();
    }

    // Drawer Setup
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menu_icon);

        View headerView = navigationView.getHeaderView(0);
        navHeaderName = headerView.findViewById(R.id.nav_header_name);
        navHeaderPhone = headerView.findViewById(R.id.nav_header_phone_no);
        navHeaderImage = headerView.findViewById(R.id.nav_profile_image_id);

        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        String lastPhone = prefs.getString("phoneNo", "");
        updateNavHeaderUserInfo(lastPhone);

        Menu menu = navigationView.getMenu();
        MenuItem loginLogoutItem = menu.findItem(R.id.nav_logout);
        if (loginLogoutItem != null) {
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            loginLogoutItem.setTitle(isLoggedIn ? "Logout" : "Login");
            loginLogoutItem.setIcon(isLoggedIn ? R.drawable.back_arrow : R.drawable.forward_arrow);
        }

        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.nav_logout) {
            handleLoginLogout();
        }
        else if(id == R.id.all_categories_id){
            Intent intent = new Intent(this, CategoryListActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.bmi_cal_id){
            Intent intent = new Intent(this, BmiActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.contact_us_id){
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(InfoActivity.EXTRA_TYPE, InfoActivity.TYPE_CONTACT);
            startActivity(intent);

        }
        else if(id == R.id.clinic_id){
            Intent intent = new Intent(this, ClinicActivity.class);
            startActivity(intent);

        }
        else{
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(InfoActivity.EXTRA_TYPE, InfoActivity.TYPE_ABOUT);
            startActivity(intent);

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLoginLogout() {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean("isLoggedIn", false);
        if (loggedIn) {
            showLogoutDialog();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);

        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setIcon(R.drawable.alert_icon)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    userLogout();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    // Bottom Navigation
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.profile_activity:
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
                    break;
                case R.id.add:
                    handleUploadNavigation();
                    overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
                    break;

                case R.id.location:
                    startActivity(new Intent(this, LocationSelectActivity.class));
                    break;

                case R.id.home:
                    break;

                case R.id.all_doctor:
                    Intent intent = new Intent(this, DoctorListActivity.class);
                    intent.putExtra("sending", "all_doctor");
                    startActivity(intent);
                    overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
                    break;
            }
            return true;
        });
    }

    // RecyclerViews
    private void populateCategoryAndDoctorRv() {
        setupCategoryRecyclerView();
        setupDoctorRecyclerView();
        doctorAdapter.startListening();
    }

    private void setupCategoryRecyclerView() {
        categoryRv = findViewById(R.id.recycler_view_category);
        List<CategoryEnum> allCategories = Arrays.asList(CategoryEnum.values());
        categoryAdapter = new CategoryAdapter(allCategories, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoryRv.setLayoutManager(layoutManager);
        categoryRv.setAdapter(categoryAdapter);
    }


    private void setupDoctorRecyclerView() {
        recyclerView = findViewById(R.id.rv_2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query defaultQuery = FirebaseDatabase.getInstance()
                .getReference()
                .child("Doctor")
                .orderByChild("popularity")
                .limitToLast(10);

        FirebaseRecyclerOptions<Doctor> options =
                new FirebaseRecyclerOptions.Builder<Doctor>()
                        .setQuery(defaultQuery, Doctor.class)
                        .build();

        doctorAdapter = new DoctorAdapter(options, "home");
        recyclerView.setAdapter(doctorAdapter);
        doctorAdapter.startListening();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedLocation = prefs.getString("saved_location", null);

        if (!TextUtils.isEmpty(savedLocation)) {
            Location selectedLocation = null;
            try {
                selectedLocation = Location.valueOf(savedLocation);
            } catch (IllegalArgumentException e) {
                selectedLocation = null;
            }

            if (selectedLocation != null) {
                String locationName = selectedLocation.getDisplayName();

                DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference().child("Doctor");
                Query locationQuery = doctorRef.orderByChild("zone").equalTo(locationName).limitToLast(10);

                locationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Doctor> doctorList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Doctor doctor = ds.getValue(Doctor.class);
                            if (doctor != null) {
                                doctor.setDoctorId(ds.getKey());
                                doctorList.add(doctor);
                            }

                        }

                        int missingCount = 10 - doctorList.size();

                        if (missingCount > 0) {
                            Query fallbackQuery = doctorRef.orderByChild("popularity").limitToLast(10);
                            fallbackQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot fallbackSnapshot) {
                                    for (DataSnapshot ds : fallbackSnapshot.getChildren()) {
                                        Doctor doctor = ds.getValue(Doctor.class);
                                        if (doctor != null && !doctorList.contains(doctor)) {
                                            doctor.setDoctorId(ds.getKey());
                                            doctorList.add(doctor);
                                        }
                                        if (doctorList.size() >= 10) break;
                                    }

                                    doctorAdapter.updateWithCustomList(doctorList);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                        } else {
                            doctorAdapter.updateWithCustomList(doctorList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }
    }




    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "en");
        updateLocale(language);
    }

    private void updateLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void setLocale(String langCode) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", langCode);
        editor.apply();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }


    // Getting User Info
    private void updateNavHeaderUserInfo(String phoneNumber) {
        navHeaderPhone.setText(phoneNumber);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(phoneNumber);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    if (name != null) {
                        navHeaderName.setText(name);
                    }
                    String profileUrl = snapshot.child("userProfile").getValue(String.class);
                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(HomeActivity.this)
                                .load(profileUrl)
                                .placeholder(R.drawable.user_icon)
                                .error(R.drawable.user_icon)
                                .into(navHeaderImage);
                    } else {
                        navHeaderImage.setImageResource(R.drawable.user_icon);
                    }
                } else {
                    navHeaderName.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // User Logout
    private void userLogout() {
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String lastPhone = preferences.getString("phoneNo", "");
        editor.putString("lastPhone", lastPhone);

        editor.remove("isLoggedIn");
        editor.remove("phoneNo");

        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Upload Navigation
    protected void handleUploadNavigation() {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            new AlertDialog.Builder(this)
                    .setTitle("Login Required")
                    .setMessage("You need to log in to upload reports or prescriptions. Do you want to log in now?")
                    .setPositiveButton("Yes", (dialog, which) ->
                            startActivity(new Intent(this, LoginActivity.class)))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        } else {
            Intent addIntent = new Intent(this, DoctorListActivity.class);
            addIntent.putExtra("from", "fab");
            addIntent.putExtra("sending", "add");
            startActivity(addIntent);
        }
    }

    // Fetching all the doctors
    private void fetchAllDoctors() {
        DatabaseReference doctorRef = FirebaseDatabase.getInstance().getReference("Doctor");
        doctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Doctor> list = new ArrayList<>();
                for (DataSnapshot docSnap : snapshot.getChildren()) {
                    Doctor doctor = docSnap.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.setDoctorId(docSnap.getKey());
                        list.add(doctor);
                    }
                }
                DoctorRepository.getInstance().setDoctorList(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Search bar implementation
    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (searchRunnable != null) mainHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    if (query.isEmpty()) {
                        searchForRecycleView.setVisibility(View.GONE);
                    } else {
                        searchForRecycleView.setVisibility(View.VISIBLE);

                        executor.execute(() -> {
                            List<Doctor> allDoctors = DoctorRepository.getInstance().getDoctorList();
                            List<Doctor> filtered = new ArrayList<>();
                            for (Doctor doctor : allDoctors) {
                                if ((doctor.getName() != null && doctor.getName().toLowerCase().contains(query.toLowerCase())) ||
                                        (doctor.getCategory() != null && doctor.getCategory().toLowerCase().contains(query.toLowerCase())) ||
                                        (doctor.getHospital() != null && doctor.getHospital().toLowerCase().contains(query.toLowerCase()))) {

                                    filtered.add(doctor);

                                    if (filtered.size() == 3) break;
                                }
                            }

                            mainHandler.post(() -> searchAdapter.submitList(filtered));
                        });

                    }
                };

                mainHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            finishAffinity();
            System.exit(0);
            return;
        }
        this.backPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        searchEditText.clearFocus();
        new Handler().postDelayed(() -> backPressedOnce = false, 2000);
    }


}
