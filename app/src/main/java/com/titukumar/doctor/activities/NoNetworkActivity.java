package com.titukumar.doctor.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.titukumar.doctor.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class NoNetworkActivity extends AppCompatActivity {
    private Button retryBtn;
    private TextView exitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);
        retryBtn = findViewById(R.id.btnRetry);
        exitBtn = findViewById(R.id.btnExit);

        retryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(NoNetworkActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        exitBtn.setOnClickListener(v -> {
            finishAffinity();
        });
    }
}