package com.passwordmanager.service;

import com.passwordmanager.dao.CredentialDAO;
import com.passwordmanager.model.Credential;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CredentialService {
    private final CredentialDAO credentialDAO;
    private final EncryptionService encryptionService;

    public CredentialService(CredentialDAO credentialDAO, EncryptionService encryptionService) {
        this.credentialDAO = credentialDAO;
        this.encryptionService = encryptionService;
    }

    public boolean addCredential(int userId, String siteName, String siteUsername, String password) {
        return addCredential(userId, siteName, siteUsername, password, "");
    }

    public boolean addCredential(int userId, String siteName, String siteUsername, String password, String notes) {
        validateCredentialFields(siteName, siteUsername, password);
        String encryptedPassword = encryptionService.encrypt(password);
        Credential credential = new Credential(userId, siteName.trim(), siteUsername.trim(), encryptedPassword, sanitizeNotes(notes));
        return credentialDAO.addCredential(credential);
    }

    public List<Credential> getCredentialsForUser(int userId) {
        return credentialDAO.findCredentialsByUserId(userId)
                .stream()
                .map(this::decryptCredential)
                .collect(Collectors.toList());
    }

    public Optional<Credential> getCredentialById(int credentialId, int userId) {
        return credentialDAO.findCredentialById(credentialId, userId).map(this::decryptCredential);
    }

    public boolean updateCredential(int credentialId, int userId, String siteName, String siteUsername, String password, String notes) {
        validateCredentialFields(siteName, siteUsername, password);
        String encryptedPassword = encryptionService.encrypt(password);
        Credential credential = new Credential(credentialId, userId, siteName.trim(), siteUsername.trim(), encryptedPassword, sanitizeNotes(notes));
        return credentialDAO.updateCredential(credential);
    }

    public boolean deleteCredential(int credentialId, int userId) {
        return credentialDAO.deleteCredential(credentialId, userId);
    }

    public List<Credential> searchCredentials(int userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty.");
        }

        return credentialDAO.searchCredentials(userId, keyword.trim())
                .stream()
                .map(this::decryptCredential)
                .collect(Collectors.toList());
    }

    public List<Credential> searchCredentials(int userId, String siteName, String siteUsername) {
        String combinedKeyword = (siteName == null ? "" : siteName.trim()) + " " + (siteUsername == null ? "" : siteUsername.trim());
        return searchCredentials(userId, combinedKeyword.trim());
    }

    public Map<String, Integer> buildSiteSummary(int userId) {
        Map<String, Integer> siteSummary = new HashMap<>();
        List<Credential> credentials = getCredentialsForUser(userId);

        for (Credential credential : credentials) {
            siteSummary.merge(credential.getSiteName(), 1, Integer::sum);
        }

        return siteSummary;
    }

    private Credential decryptCredential(Credential encryptedCredential) {
        return new Credential(
                encryptedCredential.getCredentialId(),
                encryptedCredential.getUserId(),
                encryptedCredential.getSiteName(),
                encryptedCredential.getSiteUsername(),
                encryptionService.decrypt(encryptedCredential.getPassword()),
                encryptedCredential.getNotes()
        );
    }

    private void validateCredentialFields(String siteName, String siteUsername, String password) {
        if (siteName == null || siteName.trim().isEmpty()) {
            throw new IllegalArgumentException("Site name cannot be empty");
        }
        if (siteUsername == null || siteUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Account username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (siteName.contains("<") || siteName.contains(">") || siteUsername.contains("<") || siteUsername.contains(">")) {
            throw new IllegalArgumentException("Invalid input");
        }
    }

    private String sanitizeNotes(String notes) {
        return notes == null ? "" : notes.trim();
    }
}
