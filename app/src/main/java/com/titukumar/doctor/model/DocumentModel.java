package com.titukumar.doctor.model;

public class DocumentModel {
    private String fileUrl;
    private Object timestamp;
    private String fileType;
    private String type;
    private String userPhone;
    private String docId;

    public DocumentModel() {}

    public DocumentModel(String fileUrl, Object timestamp, String fileType) {
        this.fileUrl = fileUrl;
        this.timestamp = timestamp;
        this.fileType = fileType;
    }

    public String getFileUrl() { return fileUrl; }
    public String getTimestamp() { return String.valueOf(timestamp); }
    public String getFileType() { return fileType; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

}
