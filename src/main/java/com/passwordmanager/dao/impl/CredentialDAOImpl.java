package com.passwordmanager.dao.impl;

import com.passwordmanager.config.DatabaseConfig;
import com.passwordmanager.dao.CredentialDAO;
import com.passwordmanager.model.Credential;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CredentialDAOImpl implements CredentialDAO {
    @Override
    public boolean addCredential(Credential credential) {
        String sql = """
                INSERT INTO credentials (user_id, site_name, site_username, encrypted_password, notes)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credential.getUserId());
            statement.setString(2, credential.getSiteName());
            statement.setString(3, credential.getSiteUsername());
            statement.setString(4, credential.getPassword());
            statement.setString(5, credential.getNotes());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }
    }

    @Override
    public List<Credential> findCredentialsByUserId(int userId) {
        String sql = """
                SELECT cred_id, user_id, site_name, site_username, encrypted_password, notes
                FROM credentials
                WHERE user_id = ?
                ORDER BY site_name, site_username
                """;
        List<Credential> credentials = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    credentials.add(mapCredential(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return credentials;
    }

    @Override
    public Optional<Credential> findCredentialById(int credentialId, int userId) {
        String sql = """
                SELECT cred_id, user_id, site_name, site_username, encrypted_password, notes
                FROM credentials
                WHERE cred_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCredential(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return Optional.empty();
    }

    @Override
    public boolean updateCredential(Credential credential) {
        String sql = """
                UPDATE credentials
                SET site_name = ?, site_username = ?, encrypted_password = ?, notes = ?
                WHERE cred_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, credential.getSiteName());
            statement.setString(2, credential.getSiteUsername());
            statement.setString(3, credential.getPassword());
            statement.setString(4, credential.getNotes());
            statement.setInt(5, credential.getCredentialId());
            statement.setInt(6, credential.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }
    }

    @Override
    public boolean deleteCredential(int credentialId, int userId) {
        String sql = "DELETE FROM credentials WHERE cred_id = ? AND user_id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, credentialId);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }
    }

    @Override
    public List<Credential> searchCredentials(int userId, String keyword) {
        String sql = """
                SELECT cred_id, user_id, site_name, site_username, encrypted_password, notes
                FROM credentials
                WHERE user_id = ?
                  AND (LOWER(site_name) LIKE LOWER(?) OR LOWER(site_username) LIKE LOWER(?))
                ORDER BY site_name, site_username
                """;
        List<Credential> credentials = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String formattedKeyword = "%" + keyword + "%";
            statement.setInt(1, userId);
            statement.setString(2, formattedKeyword);
            statement.setString(3, formattedKeyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    credentials.add(mapCredential(resultSet));
                }
            }
        } catch (SQLException exception) {
            System.out.println("Database error occurred");
            throw new IllegalStateException("Database error occurred.", exception);
        }

        return credentials;
    }

    private Credential mapCredential(ResultSet resultSet) throws SQLException {
        return new Credential(
                resultSet.getInt("cred_id"),
                resultSet.getInt("user_id"),
                resultSet.getString("site_name"),
                resultSet.getString("site_username"),
                resultSet.getString("encrypted_password"),
                resultSet.getString("notes")
        );
    }
}
