package com.titukumar.doctor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.titukumar.doctor.R;

public class InfoActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "extra_type";
    public static final String TYPE_ABOUT = "about";
    public static final String TYPE_CONTACT = "contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView tvContent = findViewById(R.id.tvContent);
        LinearLayout contactLayout = findViewById(R.id.contactLayout);

        String type = getIntent().getStringExtra(EXTRA_TYPE);

        if (InfoActivity.TYPE_ABOUT.equals(type)) {
            tvContent.setText(getString(R.string.about_content));
            contactLayout.setVisibility(View.GONE);
        } else if (InfoActivity.TYPE_CONTACT.equals(type)) {
            tvContent.setVisibility(View.GONE);
            contactLayout.setVisibility(View.VISIBLE);
        }
    }
}