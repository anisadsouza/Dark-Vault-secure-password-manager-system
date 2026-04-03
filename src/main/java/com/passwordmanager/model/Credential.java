package com.passwordmanager.model;

public class Credential {
    private int credentialId;
    private int userId;
    private String siteName;
    private String siteUsername;
    private String password;
    private String notes;

    public Credential() {
    }

    public Credential(int userId, String siteName, String siteUsername, String password, String notes) {
        this.userId = userId;
        this.siteName = siteName;
        this.siteUsername = siteUsername;
        this.password = password;
        this.notes = notes;
    }

    public Credential(int credentialId, int userId, String siteName, String siteUsername, String password, String notes) {
        this.credentialId = credentialId;
        this.userId = userId;
        this.siteName = siteName;
        this.siteUsername = siteUsername;
        this.password = password;
        this.notes = notes;
    }

    public int getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(int credentialId) {
        this.credentialId = credentialId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteUsername() {
        return siteUsername;
    }

    public void setSiteUsername(String siteUsername) {
        this.siteUsername = siteUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNotes() {
        return notes == null ? "" : notes;
    }

    public void setNotes(String notes) {
        this.notes = notes == null ? "" : notes;
    }
}
