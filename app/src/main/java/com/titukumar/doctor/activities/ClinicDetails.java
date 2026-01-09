package com.titukumar.doctor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.titukumar.doctor.R;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ClinicDetails extends AppCompatActivity {
    private TextView tvClinicName, tvLocation, tvHotline;

    private String hospitalName;
    private String hospitalLocation;
    private ImageView callIcon;
    private ArrayList<String> hotlines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clinic_details);

        tvClinicName = findViewById(R.id.tvHospitalnameId);
        tvLocation = findViewById(R.id.tvLocationId);
        tvHotline = findViewById(R.id.tvHotlineId);
        callIcon  = findViewById(R.id.icCallId);

        hospitalName = getIntent().getStringExtra("hospital_name");
        hospitalLocation = getIntent().getStringExtra("location");
        hotlines = getIntent().getStringArrayListExtra("hotlines");

        if (hotlines != null && !hotlines.isEmpty()) {
            String numbers = TextUtils.join(", ", hotlines);
            tvHotline.setText(numbers);
            callIcon.setVisibility(View.VISIBLE);
            callIcon.setOnClickListener(view -> showHotlineDialog());
            tvHotline.setOnClickListener(view -> showHotlineDialog());

        } else {
            tvHotline.setText("N/A");
            callIcon.setVisibility(View.GONE);
        }

        if (hospitalName != null && !hospitalName.isEmpty()) {
            tvClinicName.setText(hospitalName);
        } else {
            tvClinicName.setText("N/A");
        }
        if (hospitalLocation != null && !hospitalLocation.isEmpty()) {
            tvLocation.setText("Location: "+hospitalLocation);
        } else {
            tvLocation.setText("N/A");
        }

        callIcon.setOnClickListener(view -> showHotlineDialog());
        tvHotline.setOnClickListener(view -> showHotlineDialog());

    }
    private void showHotlineDialog() {
        if (hotlines == null || hotlines.isEmpty()) {
            Toast.makeText(this, "No hotline available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] hotlineArray = hotlines.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                hotlineArray
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);

                tv.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("Select a Hotline")
                .setAdapter(adapter, (dialog, which) -> callFunction(hotlineArray[which]))
                .show();
    }

    private void callFunction(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}