package com.find.doc.activities;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.find.doc.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{


    private EditText edtPhoneNumber;
    private TextView goToSignupButton;
    private Button loginButton;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPhoneNumber = findViewById(R.id.user_signin_mobile_id);
        goToSignupButton = findViewById(R.id.goto_signup_id);
        goToSignupButton.setOnClickListener(this);
        loginButton = findViewById(R.id.signin_button_id);
        loginButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        String passedPhone = getIntent().getStringExtra("phoneNumber");

        if (passedPhone != null && !passedPhone.isEmpty()) {
            edtPhoneNumber.setText(passedPhone);
        } else {
            SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
            String lastPhone = preferences.getString("lastPhone", "");
            edtPhoneNumber.setText(lastPhone);
        }


    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.goto_signup_id:
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                break;

            case R.id.signin_button_id:
                loginUser();
                break;

        }
    }

    private void loginUser() {
        String phoneNo = edtPhoneNumber.getText().toString().trim();

        if (phoneNo.isEmpty()) {
            edtPhoneNumber.setError("Phone number required");
            edtPhoneNumber.requestFocus();
            return;
        }

        if (phoneNo.length() != 11) {
            edtPhoneNumber.setError("Enter valid 11-digit phone number");
            edtPhoneNumber.requestFocus();
            return;
        }


        progressDialog.setMessage("Checking credentials...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        databaseReference.child(phoneNo)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {

                            saveLoginSession(phoneNo);
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            edtPhoneNumber.setError("Phone number not registered");
                            edtPhoneNumber.requestFocus();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveLoginSession(String phoneNo) {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putBoolean("everLoggedIn", true)
                .putString("phoneNo", phoneNo)
                .putString("lastPhone", phoneNo)
                .apply();
    }



    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String phoneNo = prefs.getString("phoneNo", null);

        if (isLoggedIn && phoneNo != null && !phoneNo.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

}