package com.find.doc.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.find.doc.R;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private EditText d1, d2, d3, d4, d5, d6;
    private MaterialButton btnVerify;
    private TextView tvTimer, tvResend, tvPhone;

    private String phoneNo;
    private String verificationId;
    private FirebaseAuth firebaseAuth;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        initViews();
        setupOtpInputs();

        phoneNo = getIntent().getStringExtra("phoneNo");
        verificationId = getIntent().getStringExtra("verificationId");

        firebaseAuth = FirebaseAuth.getInstance();

        tvPhone.setText(maskPhone(phoneNo));
        startTimer();

        btnVerify.setOnClickListener(v -> verifyOtp());
        tvResend.setOnClickListener(v -> resendOtp());
    }

    private void initViews() {
        d1 = findViewById(R.id.otp_digit1);
        d2 = findViewById(R.id.otp_digit2);
        d3 = findViewById(R.id.otp_digit3);
        d4 = findViewById(R.id.otp_digit4);
        d5 = findViewById(R.id.otp_digit5);
        d6 = findViewById(R.id.otp_digit6);

        btnVerify = findViewById(R.id.btn_verify_proceed);
        tvTimer = findViewById(R.id.tv_timer);
        tvResend = findViewById(R.id.tv_resend_otp);
        tvPhone = findViewById(R.id.otp_phone_no_id);
    }

    private void setupOtpInputs() {
        setupEditText(d1, null, d2);
        setupEditText(d2, d1, d3);
        setupEditText(d3, d2, d4);
        setupEditText(d4, d3, d5);
        setupEditText(d5, d4, d6);
        setupEditText(d6, d5, null);
    }

    private void setupEditText(EditText current, EditText previous, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) next.requestFocus();
                else if (s.length() == 0 && previous != null) previous.requestFocus();
            }
        });
    }

    private void verifyOtp() {

        String otp = d1.getText().toString() +
                d2.getText().toString() +
                d3.getText().toString() +
                d4.getText().toString() +
                d5.getText().toString() +
                d6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Enter complete OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseDatabase.getInstance()
                                .getReference("Users")
                                .child(phoneNo)
                                .child("verified")
                                .setValue(true);

                        saveLoginSession(phoneNo);

                        startActivity(new Intent(this, HomeActivity.class));
                        finishAffinity();

                    } else {
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resendOtp() {

        startTimer();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+88" + phoneNo,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OtpActivity.this,
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = verId;
                        Toast.makeText(OtpActivity.this,
                                "OTP resent", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void startTimer() {
        tvResend.setEnabled(false);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("You can resend OTP");
                tvResend.setEnabled(true);
            }
        }.start();
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

    private String maskPhone(String phone) {
        if (phone != null && phone.length() >= 11) {
            return "+88 " + phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
