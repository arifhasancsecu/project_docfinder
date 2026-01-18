package com.find.doc.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.find.doc.R;
import com.find.doc.model.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView alreadyHaveAccount;
    private EditText edtUserName, edtUserPhoneNo, edtUserAge;
    private RadioGroup userGender;
    private Button signupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        edtUserName = findViewById(R.id.user_name_id);
        edtUserPhoneNo = findViewById(R.id.user_mobile_id);
        edtUserAge = findViewById(R.id.user_age_id);
        userGender = findViewById(R.id.gender_group_id);
        signupButton = findViewById(R.id.signup_button_id);
        alreadyHaveAccount = findViewById(R.id.already_account_id);

        alreadyHaveAccount.setOnClickListener(this);
        signupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.already_account_id) {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);

        } else if (id == R.id.signup_button_id) {
            registerUser();
        }
    }


    private String getSelectedGender() {
        int selectedId = userGender.getCheckedRadioButtonId();

        if (selectedId == -1) {
            return "";
        } else {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        }
    }


    private void registerUser() {
        String username = edtUserName.getText().toString().trim();
        String phoneNo = edtUserPhoneNo.getText().toString().trim();
        String age = edtUserAge.getText().toString().trim();
        String userProfile = "";
        String gender = getSelectedGender();

        if (isEmpty(edtUserName, "Username required")) return;
        if (isEmpty(edtUserAge, "Age required")) return;
        if (!isGenderSelected()) return;
        if (isEmpty(edtUserPhoneNo, "Phone number required")) return;

        if (!age.matches("\\d+")) {
            edtUserAge.setError("Enter a valid number");
            edtUserAge.requestFocus();
            return;
        }

        if (phoneNo.length() != 11 || !phoneNo.startsWith("01")) {
            edtUserPhoneNo.setError("Enter a valid 11-digit Bangladeshi phone number starting with 01");
            edtUserPhoneNo.requestFocus();
            return;
        }


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        checkIfUserExists(phoneNo, () -> {
            String timestamp = String.valueOf(System.currentTimeMillis());
            User user = new User(username, age, gender, phoneNo, timestamp, timestamp, userProfile);
            user.setVerified(false);
            String safePhone = encodeKey(phoneNo);

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(safePhone)
                    .setValue(user)
                    .addOnCompleteListener(task -> {
                        if (!isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            intent.putExtra("phoneNumber", phoneNo);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
                        }
                    });
        }, progressDialog);

    }

    private boolean isEmpty(EditText editText, String errorMessage) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setError(errorMessage);
            editText.requestFocus();
            return true;
        }
        return false;
    }

    private boolean isGenderSelected() {
        int selectedId = userGender.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String encodeKey(String key) {
        return key.replace(".", ",")
                .replace("#", ",")
                .replace("$", ",")
                .replace("[", ",")
                .replace("]", ",");
    }


    private void checkIfUserExists(String phoneNo, Runnable onNotExists, ProgressDialog progressDialog) {
        String safePhone = encodeKey(phoneNo);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(safePhone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            if (!isFinishing() && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            edtUserPhoneNo.setError("Phone number already registered");
                            edtUserPhoneNo.requestFocus();
                        } else {
                            onNotExists.run();
                        }
                    } else {
                        if (!isFinishing() && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(this, "Failed to check user", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
