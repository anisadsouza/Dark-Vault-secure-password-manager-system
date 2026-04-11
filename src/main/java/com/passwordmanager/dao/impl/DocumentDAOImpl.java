package com.passwordmanager.dao.impl;

import com.passwordmanager.config.DatabaseConfig;
import com.passwordmanager.dao.DocumentDAO;
import com.passwordmanager.model.SecureDocument;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentDAOImpl implements DocumentDAO {
    @Override
    public boolean addDocument(SecureDocument document) {
        String sql = """
                INSERT INTO secure_documents
                    (user_id, title, original_file_name, stored_file_path, mime_type, category, notes, file_size_bytes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, document.getUserId());
            statement.setString(2, document.getTitle());
            statement.setString(3, document.getOriginalFileName());
            statement.setString(4, document.getStoredFilePath());
            statement.setString(5, document.getMimeType());
            statement.setString(6, document.getCategory());
            statement.setString(7, document.getNotes());
            statement.setLong(8, document.getFileSizeBytes());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }
    }

    @Override
    public List<SecureDocument> findDocumentsByUserId(int userId) {
        String sql = """
                SELECT document_id, user_id, title, original_file_name, stored_file_path,
                       mime_type, category, notes, file_size_bytes, date_added
                FROM secure_documents
                WHERE user_id = ?
                ORDER BY date_added DESC, title
                """;
        List<SecureDocument> documents = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(mapDocument(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return documents;
    }

    @Override
    public Optional<SecureDocument> findDocumentById(int documentId, int userId) {
        String sql = """
                SELECT document_id, user_id, title, original_file_name, stored_file_path,
                       mime_type, category, notes, file_size_bytes, date_added
                FROM secure_documents
                WHERE document_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapDocument(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteDocument(int documentId, int userId) {
        String sql = "DELETE FROM secure_documents WHERE document_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, documentId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }
    }

    @Override
    public List<SecureDocument> searchDocuments(int userId, String keyword) {
        String sql = """
                SELECT document_id, user_id, title, original_file_name, stored_file_path,
                       mime_type, category, notes, file_size_bytes, date_added
                FROM secure_documents
                WHERE user_id = ?
                  AND (
                      LOWER(title) LIKE LOWER(?)
                      OR LOWER(original_file_name) LIKE LOWER(?)
                      OR LOWER(category) LIKE LOWER(?)
                  )
                ORDER BY date_added DESC, title
                """;
        List<SecureDocument> documents = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String formattedKeyword = "%" + keyword + "%";
            statement.setInt(1, userId);
            statement.setString(2, formattedKeyword);
            statement.setString(3, formattedKeyword);
            statement.setString(4, formattedKeyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    documents.add(mapDocument(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return documents;
    }

    private SecureDocument mapDocument(ResultSet resultSet) throws SQLException {
        return new SecureDocument(
                resultSet.getInt("document_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("title"),
                resultSet.getString("original_file_name"),
                resultSet.getString("stored_file_path"),
                resultSet.getString("mime_type"),
                resultSet.getString("category"),
                resultSet.getString("notes"),
                resultSet.getLong("file_size_bytes"),
                resultSet.getString("date_added")
        );
    }
}
