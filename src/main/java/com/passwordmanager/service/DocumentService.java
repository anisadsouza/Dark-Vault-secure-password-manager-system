package com.passwordmanager.service;

import com.passwordmanager.dao.DocumentDAO;
import com.passwordmanager.model.SecureDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class DocumentService {
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(
            ".pdf", ".doc", ".docx", ".csv", ".txt",
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"
    );
    private static final List<String> SUPPORTED_MIME_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/csv",
            "application/csv",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml"
    );
    private final DocumentDAO documentDAO;
    private final Path storageRoot;

    public DocumentService(DocumentDAO documentDAO) {
        this(documentDAO, Paths.get("vault-files"));
    }

    public DocumentService(DocumentDAO documentDAO, Path storageRoot) {
        this.documentDAO = documentDAO;
        this.storageRoot = storageRoot.toAbsolutePath().normalize();
    }

    public boolean saveDocument(int userId, String title, String originalFileName, String mimeType,
                                String category, String notes, String base64FileData) {
        validateDocumentInput(title, originalFileName, mimeType, base64FileData);

        try {
            byte[] fileBytes = Base64.getDecoder().decode(stripDataUrlPrefix(base64FileData));
            if (fileBytes.length > MAX_FILE_SIZE_BYTES) {
                throw new IllegalArgumentException("File must be 10 MB or smaller.");
            }

            String cleanFileName = sanitizeFileName(originalFileName);
            Path userDirectory = storageRoot.resolve("user-" + userId).normalize();
            Files.createDirectories(userDirectory);

            String storedFileName = UUID.randomUUID() + extensionOf(cleanFileName);
            Path storedFile = userDirectory.resolve(storedFileName).normalize();
            if (!storedFile.startsWith(storageRoot)) {
                throw new IllegalArgumentException("Invalid file path.");
            }

            Files.write(storedFile, fileBytes);
            SecureDocument document = new SecureDocument(
                    userId,
                    title.trim(),
                    cleanFileName,
                    storageRoot.relativize(storedFile).toString(),
                    sanitizeOptional(mimeType),
                    sanitizeOptional(category),
                    sanitizeOptional(notes),
                    fileBytes.length
            );

            return documentDAO.addDocument(document);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save file.", exception);
        }
    }

    public List<SecureDocument> getDocumentsForUser(int userId) {
        return documentDAO.findDocumentsByUserId(userId);
    }

    public Optional<SecureDocument> getDocumentById(int documentId, int userId) {
        return documentDAO.findDocumentById(documentId, userId);
    }

    public List<SecureDocument> searchDocuments(int userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getDocumentsForUser(userId);
        }

        return documentDAO.searchDocuments(userId, keyword.trim());
    }

    public byte[] readDocumentBytes(SecureDocument document) {
        Path filePath = resolveStoredPath(document);
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read file.", exception);
        }
    }

    public boolean deleteDocument(int documentId, int userId) {
        Optional<SecureDocument> documentOptional = getDocumentById(documentId, userId);
        if (documentOptional.isEmpty()) {
            return false;
        }

        boolean deleted = documentDAO.deleteDocument(documentId, userId);
        if (deleted) {
            try {
                Files.deleteIfExists(resolveStoredPath(documentOptional.get()));
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to delete stored file.", exception);
            }
        }

        return deleted;
    }

    private void validateDocumentInput(String title, String originalFileName, String mimeType, String base64FileData) {
        validateRequired(title, "Document title");
        validateRequired(originalFileName, "File name");
        validateRequired(base64FileData, "File data");

        if (!isSafeText(title) || !isSafeText(originalFileName)) {
            throw new IllegalArgumentException("Invalid input.");
        }

        if (!isSupportedFile(originalFileName, mimeType)) {
            throw new IllegalArgumentException("Unsupported file type. Use PDF, Word, CSV, TXT, JPG, PNG, GIF, WebP, or SVG.");
        }
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private String sanitizeFileName(String fileName) {
        String cleanName = Paths.get(fileName).getFileName().toString();
        cleanName = cleanName.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        return cleanName.isEmpty() ? "uploaded-file" : cleanName;
    }

    private String sanitizeOptional(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if (!isSafeText(trimmed)) {
            throw new IllegalArgumentException("Invalid input.");
        }
        return trimmed;
    }

    private boolean isSafeText(String value) {
        return value == null || !value.contains("<") && !value.contains(">");
    }

    private String stripDataUrlPrefix(String base64FileData) {
        int commaIndex = base64FileData.indexOf(',');
        return commaIndex >= 0 ? base64FileData.substring(commaIndex + 1) : base64FileData;
    }

    private String extensionOf(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
    }

    private boolean hasSupportedExtension(String fileName) {
        String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lowerCaseFileName::endsWith);
    }

    private boolean hasSupportedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }

        return SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase(Locale.ROOT).trim());
    }

    private boolean isSupportedFile(String fileName, String mimeType) {
        return hasSupportedExtension(fileName) || hasSupportedMimeType(mimeType);
    }

    private Path resolveStoredPath(SecureDocument document) {
        Path filePath = storageRoot.resolve(document.getStoredFilePath()).normalize();
        if (!filePath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid file path.");
        }
        return filePath;
    }
}
