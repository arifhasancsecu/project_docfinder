package com.find.doc.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.find.doc.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileUpdateActivity extends AppCompatActivity {
    private TextInputEditText edtName, edtAge, edtPhone;
    private ImageView imgProfile;
    private MaterialButton btnUpdateProfile;
    private DatabaseReference userRef;
    private String phoneNo;
    private Uri imageUri;
    private ProgressBar progressBar;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
        edtName = findViewById(R.id.edt_name);
        edtAge = findViewById(R.id.edt_age);
        edtPhone = findViewById(R.id.edt_phone);
        imgProfile = findViewById(R.id.img_profile);
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        progressBar = findViewById(R.id.progress_bar);

        // Retrieving phone number from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        phoneNo = preferences.getString("phoneNo", null);

        if (phoneNo != null && !phoneNo.isEmpty()) {
            edtPhone.setText(phoneNo);
            userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(phoneNo);

            fetchUserProfile();
        } else {
            Toast.makeText(this, "No phone number found in session", Toast.LENGTH_SHORT).show();
        }

        imgProfile.setOnClickListener(v -> pickImage());
        btnUpdateProfile.setOnClickListener(view -> updateProfile());

    }


    private void fetchUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name  = snapshot.child("username").getValue(String.class);
                    String age = snapshot.child("age").getValue(String.class);
                    String profileUrl = snapshot.child("userProfile").getValue(String.class);

                    edtName.setText(name != null ? name : "");
                    edtAge.setText(age != null ? age : "");

                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        Glide.with(ProfileUpdateActivity.this)
                                .load(profileUrl)
                                .placeholder(R.drawable.user_icon)
                                .into(imgProfile);
                    }
                } else {
                    Toast.makeText(ProfileUpdateActivity.this,
                            "Profile data not found for this number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileUpdateActivity.this,
                        "Failed to fetch profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            if (imageUri != null) {
                try (Cursor cursor = getContentResolver().query(imageUri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        if (sizeIndex != -1) {
                            long fileSize = cursor.getLong(sizeIndex); // in bytes
                            if (fileSize > 3 * 1024 * 1024) { // 3 MB
                                Toast.makeText(this, "Please choose an image smaller than 3 MB", Toast.LENGTH_SHORT).show();
                                imageUri = null;
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to read image size", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                    return;
                }

                imgProfile.setImageURI(imageUri);
            }

        }
    }



    private void updateProfile() {
        if (isUpdating) return;
        isUpdating = true;

        String name  = edtName.getText().toString().trim();
        String age   = edtAge.getText().toString().trim();

        if (name.isEmpty() || age.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdateProfile.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            try {
                byte[] data = getCompressedImage(imageUri);
                StorageReference storageRef = FirebaseStorage.getInstance()
                        .getReference("Profile/" + phoneNo + ".jpg");

                storageRef.putBytes(data)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return storageRef.getDownloadUrl();
                        })
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveProfileData(name, age, imageUrl);
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            btnUpdateProfile.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                btnUpdateProfile.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveProfileData(name, age, null);
        }

    }

    private void saveProfileData(String name, String age, String imageUrl) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", name);
        map.put("age", age);
        if (imageUrl != null) map.put("userProfile", imageUrl);

        userRef.updateChildren(map)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdateProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdateProfile.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                });
    }

    private byte[] getCompressedImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

        int maxWidth = 800;
        int maxHeight = 800;
        float ratio = Math.min(
                (float) maxWidth / bitmap.getWidth(),
                (float) maxHeight / bitmap.getHeight()
        );

        int width = Math.round(bitmap.getWidth() * ratio);
        int height = Math.round(bitmap.getHeight() * ratio);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        return baos.toByteArray();
    }


}
