package com.passwordmanager.dao;

import com.passwordmanager.model.SecureDocument;
import java.util.List;
import java.util.Optional;

public interface DocumentDAO {
    boolean addDocument(SecureDocument document);
    List<SecureDocument> findDocumentsByUserId(int userId);
    Optional<SecureDocument> findDocumentById(int documentId, int userId);
    boolean deleteDocument(int documentId, int userId);
    List<SecureDocument> searchDocuments(int userId, String keyword);
}
