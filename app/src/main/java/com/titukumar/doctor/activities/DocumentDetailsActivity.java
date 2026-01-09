package com.titukumar.doctor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import com.titukumar.doctor.BaseActivity;
import com.titukumar.doctor.R;
import com.titukumar.doctor.adapter.DocumentDetailAdapter;
import com.titukumar.doctor.model.DocumentModel;

import java.util.ArrayList;
import java.util.List;

public class DocumentDetailsActivity extends BaseActivity {

    private ImageView doctorImage;
    private TextView doctorName;

    private String userPhone, doctorId;
    private RecyclerView recyclerView;
    private DocumentDetailAdapter documentDetailAdapter;
    private List<DocumentModel> alldocumentList;
    private MaterialButton docShareBtn;

    private TextInputEditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_details);
        setupBottomNavigation(0);

        doctorName = findViewById(R.id.document_details_name);
        doctorImage = findViewById(R.id.document_details_img);
        recyclerView = findViewById(R.id.recycler_prescription);
        docShareBtn = findViewById(R.id.fab_docshare_details_id);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        alldocumentList = new ArrayList<>();
        documentDetailAdapter = new DocumentDetailAdapter(this, alldocumentList);
        recyclerView.setAdapter(documentDetailAdapter);

        loadDoctorDetailsFromIntent();
        loadAllDocuments();
        docShareBtn.setOnClickListener(v -> goDocShareActivity());


        searchEditText = findViewById(R.id.searchEditText);
        View rootLayout = findViewById(R.id.scrollViewId);
        rootLayout.setOnTouchListener((v, event) -> {
            if (searchEditText.hasFocus()) {
                searchEditText.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
            return false;
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                documentDetailAdapter.filterByDate(s.toString().trim());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });




    }

    private void loadDoctorDetailsFromIntent() {
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        userPhone = preferences.getString("phoneNo", null);
        Intent intent = getIntent();

        doctorId = intent.getStringExtra("doctorId");
        String docName = intent.getStringExtra("doctorName");
        String docImageUrl = intent.getStringExtra("doctorImage");

        if (docName != null) {
            doctorName.setText(docName);
        }

        if (docImageUrl != null && !docImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(docImageUrl)
                    .placeholder(R.drawable.doc_icon)
                    .into(doctorImage);
        } else {
            doctorImage.setImageResource(R.drawable.doc_icon);
        }
    }

    private void loadAllDocuments() {
        if (userPhone == null || doctorId == null) {
            Toast.makeText(this, "User or doctor info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference docRef = FirebaseDatabase.getInstance()
                .getReference("Documents")
                .child(userPhone);

        docRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<DocumentModel> filteredDocuments = new ArrayList<>();

                for (DataSnapshot docIdSnapshot : snapshot.getChildren()) {
                    // Prescriptions
                    DataSnapshot presSnap = docIdSnapshot.child("Prescriptions");
                    for (DataSnapshot docSnap : presSnap.getChildren()) {
                        String docDoctorId = docSnap.child("doctorId").getValue(String.class);
                        if (doctorId.equals(docDoctorId)) {
                            DocumentModel model = docSnap.getValue(DocumentModel.class);
                            if (model != null) {
                                model.setType("Prescriptions");
                                model.setDocId(doctorId);
                                model.setUserPhone(userPhone);
                                filteredDocuments.add(model);
                            }
                        }
                    }

                    // Reports
                    DataSnapshot repSnap = docIdSnapshot.child("Report");
                    for (DataSnapshot docSnap : repSnap.getChildren()) {
                        String docDoctorId = docSnap.child("doctorId").getValue(String.class);
                        if (doctorId.equals(docDoctorId)) {
                            DocumentModel model = docSnap.getValue(DocumentModel.class);
                            if (model != null) {
                                model.setType("Report");
                                model.setDocId(doctorId);
                                model.setUserPhone(userPhone);
                                filteredDocuments.add(model);
                            }
                        }
                    }
                }

                if (filteredDocuments.isEmpty()) {
                    Toast.makeText(DocumentDetailsActivity.this, "No documents found", Toast.LENGTH_SHORT).show();
                } else {
                    documentDetailAdapter.updateData(filteredDocuments);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DocumentDetailsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goDocShareActivity(){
        Intent intent = new Intent(DocumentDetailsActivity.this, DocumentShareActivity.class);
        intent.putExtra("doctorId", doctorId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchEditText.clearFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }
}
