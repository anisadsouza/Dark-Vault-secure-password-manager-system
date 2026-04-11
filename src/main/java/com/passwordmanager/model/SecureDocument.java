package com.passwordmanager.model;

public class SecureDocument {
    private int documentId;
    private int userId;
    private String title;
    private String originalFileName;
    private String storedFilePath;
    private String mimeType;
    private String category;
    private String notes;
    private long fileSizeBytes;
    private String dateAdded;

    public SecureDocument() {
    }

    public SecureDocument(int userId, String title, String originalFileName, String storedFilePath,
                          String mimeType, String category, String notes, long fileSizeBytes) {
        this.userId = userId;
        this.title = title;
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.mimeType = mimeType;
        this.category = category;
        this.notes = notes;
        this.fileSizeBytes = fileSizeBytes;
    }

    public SecureDocument(int documentId, int userId, String title, String originalFileName, String storedFilePath,
                          String mimeType, String category, String notes, long fileSizeBytes, String dateAdded) {
        this(userId, title, originalFileName, storedFilePath, mimeType, category, notes, fileSizeBytes);
        this.documentId = documentId;
        this.dateAdded = dateAdded;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFilePath() {
        return storedFilePath;
    }

    public void setStoredFilePath(String storedFilePath) {
        this.storedFilePath = storedFilePath;
    }

    public String getMimeType() {
        return mimeType == null ? "" : mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType == null ? "" : mimeType;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public void setCategory(String category) {
        this.category = category == null ? "" : category;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getDateAdded() {
        return dateAdded == null ? "" : dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded == null ? "" : dateAdded;
    }
}
