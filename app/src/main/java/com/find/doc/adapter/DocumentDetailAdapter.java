package com.find.doc.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.find.doc.R;
import com.find.doc.model.DocumentModel;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocumentDetailAdapter extends RecyclerView.Adapter<DocumentDetailAdapter.ViewHolder> {
    private final List<DocumentModel> documentList;
    private final List<DocumentModel> allDocuments;
    private final Context context;

    public DocumentDetailAdapter(Context context, List<DocumentModel> documentList) {
        this.context = context;
        this.documentList = documentList;
        this.allDocuments = new ArrayList<>(documentList);
    }

    public void updateData(List<DocumentModel> newList) {
        allDocuments.clear();
        allDocuments.addAll(newList);

        documentList.clear();
        documentList.addAll(newList);

        notifyDataSetChanged();
    }

    public void filterByDate(String query) {
        documentList.clear();
        if (query.isEmpty()) {
            documentList.addAll(allDocuments);
        } else {
            for (DocumentModel doc : allDocuments) {
                String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(new Date(Long.parseLong(doc.getTimestamp())));

                if (formattedDate.toLowerCase().contains(query.toLowerCase())) {
                    documentList.add(doc);
                }
            }
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doc_item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentModel doc = documentList.get(position);

        String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(Long.parseLong(doc.getTimestamp())));
        holder.dateText.setText(formattedDate);

        holder.docType.setText(doc.getType());

        String fileUrl = doc.getFileUrl();
        String fileType = doc.getFileType();
        holder.documentImage.setVisibility(View.VISIBLE);
        if (fileType != null && fileUrl != null) {
            if (fileType.contains("image")) {
                Glide.with(context)
                        .load(fileUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(holder.documentImage);
            } else if (fileType.contains("pdf")) {
                holder.documentImage.setImageResource(R.drawable.ic_pdf);
            } else {
                holder.documentImage.setVisibility(View.GONE);
            }
        } else {
            holder.documentImage.setVisibility(View.GONE);
        }



        holder.documentImage.setOnClickListener(v -> {
            if (fileType != null && fileType.contains("pdf")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show();
                }
            }
            else if (fileUrl != null && fileType != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fileUrl), fileType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getString(R.string.no_app_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.threeDotBtn.setOnClickListener(v -> {
            showOptionsBottomSheet(doc, position);
        });
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, docType;
        ImageView documentImage, threeDotBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            docType = itemView.findViewById(R.id.docTypeId);
            documentImage = itemView.findViewById(R.id.documentImage);
            threeDotBtn = itemView.findViewById(R.id.threeDotButton);

        }
    }

    // Open bottom sheet
    private void showOptionsBottomSheet(DocumentModel doc, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context)
                .inflate(R.layout.layout_edit_delete, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView btnDownload = sheetView.findViewById(R.id.btn_download);
        TextView btnShare   = sheetView.findViewById(R.id.btn_share);
        TextView btnDelete  = sheetView.findViewById(R.id.btn_delete);

        btnDownload.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            downloadFile(context, doc);
        });

        btnShare.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            shareDocumentFile(context, doc.getFileUrl());
        });

        btnDelete.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showDeleteConfirmation(doc, position);
        });

        bottomSheetDialog.show();
    }

    // To share documents
    private void shareDocumentFile(Context context, String fileUrl) {
        try {
            // Naming file based on URL extension
            String fileName = "shared_doc" + getFileExtensionFromUrl(fileUrl);
            File localFile = new File(context.getCacheDir(), fileName);

            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
            storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                Uri uri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".provider",
                        localFile);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType(getMimeType(fileUrl)); // Correct type
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(Intent.createChooser(shareIntent, "Share Document"));

            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to download file for sharing", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileExtensionFromUrl(String url) {
        try {
            url = url.split("\\?")[0];
            int i = url.lastIndexOf('.');
            if (i > 0) {
                return url.substring(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ".pdf";
    }


    private String getMimeType(String url) {
        String type = "*/*";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            extension = extension.toLowerCase(Locale.ROOT);
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (type == null) type = "*/*";
        }
        return type;
    }



    // Deleting documents(report or prescription) from firebase
    private void showDeleteConfirmation(DocumentModel doc, int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setIcon(R.drawable.alert_icon)
                .setMessage(context.getString(R.string.delete_confirmation_msg))
                .setPositiveButton(context.getString(R.string.delete), (dialog, which) -> {

                    String fileUrl = doc.getFileUrl();
                    String type = doc.getType();
                    String userPhone = doc.getUserPhone();
                    String docId = doc.getDocId();
                    String timeStamp = doc.getTimestamp();

                    if (fileUrl == null || type == null || userPhone == null || docId == null || timeStamp == null) {
                        Toast.makeText(context, context.getString(R.string.invalid_document_data), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dialog.dismiss();

                    FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                FirebaseDatabase.getInstance().getReference("Documents")
                                        .child(userPhone)
                                        .child(docId)
                                        .child(type)
                                        .child("doc_" + timeStamp)
                                        .removeValue()
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(context, context.getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();

                                            if (position >= 0 && position < documentList.size()) {
                                                documentList.remove(position);
                                                notifyItemRemoved(position);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, context.getString(R.string.failed_to_delete_database), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, context.getString(R.string.failed_to_delete_storage), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }


    // Downloading documents from apps
    private void downloadFile(Context context, DocumentModel doc) {
        if (doc != null && doc.getFileUrl() != null) {
            String fileUrl = doc.getFileUrl();
            String fileType = doc.getFileType();
            String extension = "";

            if (fileType != null) {
                if (fileType.contains("pdf")) {
                    extension = ".pdf";
                } else if (fileType.contains("jpeg")) {
                    extension = ".jpeg";
                } else if (fileType.contains("png")) {
                    extension = ".png";
                } else if (fileType.contains("jpg")) {
                    extension = ".jpg";
                } else if (fileType.contains("doc")) {
                    extension = ".doc";
                } else if (fileType.contains("docx")) {
                    extension = ".docx";
                } else {
                    extension = "";
                }
            }

            String fileName = "download_" + System.currentTimeMillis() + extension;


            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
            request.setTitle("Downloading file");
            request.setDescription("Please wait...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
                Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Download manager not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File not available", Toast.LENGTH_SHORT).show();
        }
    }

}
