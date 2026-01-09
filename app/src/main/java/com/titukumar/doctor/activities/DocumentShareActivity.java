package com.titukumar.doctor.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.titukumar.doctor.BaseActivity;
import com.titukumar.doctor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DocumentShareActivity extends BaseActivity {

    private static final String DB_DOCUMENTS = "Documents";
    private static final String TYPE_PRESCRIPTION = "Prescriptions";
    private static final String TYPE_REPORT = "Report";

    private MaterialButton btnTakePhoto, btnChooseFile, btnSave;
    private RadioGroup rgDocumentType;
    private EditText editFileName;
    private TextView edtTxtDoctypeError;
    private ProgressBar progressBar;

    private String selectedDocType = null;
    private Uri selectedFileUri = null;
    private String userPhone;
    private String docId;
    private final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(DB_DOCUMENTS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_share);
        setupBottomNavigation(0);


        SharedPreferences preferences = getSharedPreferences("login_session", MODE_PRIVATE);
        userPhone = preferences.getString("phoneNo", "");
        docId = getIntent().getStringExtra("doctorId");

        // Initialize UI
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnChooseFile = findViewById(R.id.btn_choose_file);
        btnSave = findViewById(R.id.btn_save);
        rgDocumentType = findViewById(R.id.rg_document_type);
        editFileName = findViewById(R.id.edit_file_name);
        progressBar = findViewById(R.id.progress_bar_doc);
        edtTxtDoctypeError = findViewById(R.id.txt_error_doctype);
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, "Upload Prescription or Report", Snackbar.LENGTH_SHORT).show();


        rgDocumentType.setOnCheckedChangeListener((group, checkedId) -> {
            edtTxtDoctypeError.setText("");
            if (checkedId == R.id.rb_prescription) {
                selectedDocType = TYPE_PRESCRIPTION;
            } else if (checkedId == R.id.rb_report) {
                selectedDocType = TYPE_REPORT;
            }
        });


        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnChooseFile.setOnClickListener(v -> openFilePicker());

        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                btnSave.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                uploadFileToFirebase(selectedFileUri);
            }
        });

    }

    private boolean validateInputs() {
        boolean isValid = true;
        if (selectedDocType == null) {
            edtTxtDoctypeError.setText("Please Select a Document Type");
            isValid = false;
        } else {
            edtTxtDoctypeError.setText("");
        }

        if (selectedFileUri == null) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please Select a File", Snackbar.LENGTH_SHORT).show();
            isValid = false;
        } else {
            editFileName.setError(null);
        }

        if (!isInternetAvailable()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }


    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");

            selectedFileUri = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (selectedFileUri == null) {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedFileUri);
            takePictureLauncher.launch(intent);

        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }


    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && selectedFileUri != null) {
                    editFileName.setText("Image captured successfully");
                    editFileName.setError(null);
                    Toast.makeText(this, "Photo ready to upload. Tap Save.", Toast.LENGTH_SHORT).show();
                }
            });

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFileLauncher.launch(Intent.createChooser(intent, "Choose a file"));
    }

    private final ActivityResultLauncher<Intent> chooseFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) return;

                    Uri fileUri = data.getData();
                    selectedFileUri = fileUri;

                    String originalName = getFileNameFromUri(fileUri);
                    if (originalName.contains(".")) {
                        String nameOnly = originalName.substring(0, originalName.lastIndexOf('.'));
                        editFileName.setText(nameOnly);
                    } else {
                        editFileName.setText(originalName);
                    }
                    editFileName.setError(null);
                }
            });

    private void uploadFileToFirebase(Uri fileUri) {
        if (userPhone == null || userPhone.isEmpty()) {
            userPhone = "UnknownUser";
        }

        long timestamp = System.currentTimeMillis();
        String userInputName = editFileName.getText().toString().trim();

        if (userInputName.isEmpty()) {
            userInputName = "doc_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        }

        // Keep original extension
        String extension = getFileExtension(fileUri);
        String finalFileName = userInputName + (extension != null ? "." + extension : "");

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(DB_DOCUMENTS + "/" + selectedDocType + "/" + userPhone + "/" + finalFileName);

        UploadTask uploadTask = storageRef.putFile(fileUri);
        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            String mimeType = getMimeType(fileUri);
                            saveFileMetadataToDatabase(downloadUri.toString(), timestamp, userPhone, mimeType);
                            Snackbar.make(findViewById(android.R.id.content),
                                            "Uploaded successfully",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("See Document", v -> {
                                        Intent intent = new Intent(DocumentShareActivity.this, ProfileActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                            resetUIAfterUpload();
                        }).addOnFailureListener(e -> {
                            Snackbar.make(findViewById(android.R.id.content),
                                            "Failed to get download URL: " + e.getMessage(),
                                            Snackbar.LENGTH_LONG)
                                    .show();
                            resetUIAfterUpload();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetUIAfterUpload();
                });
    }

    private void saveFileMetadataToDatabase(String downloadUrl, long timestamp, String userPhone, String mimeType) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileUrl", downloadUrl);
        data.put("timestamp", timestamp);
        data.put("fileType", mimeType);
        data.put("doctorId", docId);
        databaseRef.child(userPhone).child(docId).child(selectedDocType).child("doc_" + timestamp).setValue(data);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "Unknown";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        } else if ("file".equals(uri.getScheme())) {
            result = new java.io.File(uri.getPath()).getName();
        }
        return result;
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    private String getFileExtension(Uri uri) {
        String extension = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        }
        return extension != null ? extension : "dat";
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    private void resetUIAfterUpload() {
        selectedFileUri = null;
        editFileName.setText("");
        progressBar.setVisibility(View.GONE);
        btnSave.setVisibility(View.VISIBLE);

        selectedDocType = null;
        rgDocumentType.clearCheck();
    }

    @Override
    public void onBackPressed() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Please wait until upload finishes", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}
