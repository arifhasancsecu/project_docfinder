package com.titukumar.doctor.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.titukumar.doctor.R;

public class BmiActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtHeightFeet, edtHeightInches, edtWeight;
    private MaterialButton btnCalculate;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        edtName = findViewById(R.id.edt_name);
        edtHeightFeet = findViewById(R.id.edt_height_feet);
        edtHeightInches = findViewById(R.id.edt_height_inches);
        edtWeight = findViewById(R.id.edt_weight);
        btnCalculate = findViewById(R.id.btn_calculate);
        tvResult = findViewById(R.id.tv_result);

        btnCalculate.setOnClickListener(v -> calculateBmi());
    }

    private void calculateBmi() {
        String name = edtName.getText().toString().trim();
        String feetStr = edtHeightFeet.getText().toString().trim();
        String inchStr = edtHeightInches.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Enter your name");
            return;
        }
        if (TextUtils.isEmpty(feetStr)) {
            edtHeightFeet.setError("Enter feet");
            return;
        }
        if (TextUtils.isEmpty(inchStr)) {
            edtHeightInches.setError("Enter inches");
            return;
        }
        if (TextUtils.isEmpty(weightStr)) {
            edtWeight.setError("Enter your weight");
            return;
        }

        try {
            float feet = Float.parseFloat(feetStr);
            float inches = Float.parseFloat(inchStr);
            float weightKg = Float.parseFloat(weightStr);

            float totalInches = (feet * 12) + inches;
            float heightMeters = (float) (totalInches * 0.0254);

            float bmi = weightKg / (heightMeters * heightMeters);

            String bmiCategory;
            if (bmi < 18.5) {
                bmiCategory = "Underweight";
            } else if (bmi < 24.9) {
                bmiCategory = "Normal weight";
            } else if (bmi < 29.9) {
                bmiCategory = "Overweight";
            } else {
                bmiCategory = "Obese";
            }

            String result = name + ", your BMI is " + String.format("%.2f", bmi) + " (" + bmiCategory + ")";
            tvResult.setText(result);
        } catch (NumberFormatException e) {
            tvResult.setText("Please enter valid numbers for height and weight");
        }
    }


}
