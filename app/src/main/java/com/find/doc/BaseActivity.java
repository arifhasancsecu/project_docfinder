package com.find.doc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.find.doc.activities.DoctorListActivity;
import com.find.doc.activities.HomeActivity;
import com.find.doc.activities.LocationSelectActivity;
import com.find.doc.activities.LoginActivity;
import com.find.doc.activities.ProfileActivity;

public class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        if (selectedItemId != 0) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == selectedItemId) return true;

            switch (item.getItemId()) {
                case R.id.profile_activity:
                    openActivity(ProfileActivity.class, false, null, null);
                    break;
                case R.id.add:
                    checkLoginWithDialog("upload reports or prescriptions",
                            () -> openActivity(DoctorListActivity.class, true, "sending", "add"));
                    break;

                case R.id.location:
                    startActivity(new Intent(this, LocationSelectActivity.class));
                    break;

                case R.id.home:
                    openActivity(HomeActivity.class, false, null, null);

                    break;
                case R.id.all_doctor:
                    openActivity(DoctorListActivity.class, false, "sending", "all_doctor");
                    break;
            }
            return true;
        });
    }

    protected boolean checkLoginWithDialog(String actionName, Runnable onLoggedIn) {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Login Required")
                    .setMessage("Want to " + actionName + "? You have to Login")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
            return false;
        } else {
            if (onLoggedIn != null) onLoggedIn.run();
            return true;
        }
    }

    private void openActivity(Class<?> target, boolean fromFab, String extraKey, String extraValue) {
        Intent intent = new Intent(this, target);

        if (fromFab) intent.putExtra("from", "fab");

        if (extraKey != null && extraValue != null) {
            intent.putExtra(extraKey, extraValue);
        }

        ContextCompat.startActivity(this, intent, null);
        overridePendingTransition(0, 0);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }

}
