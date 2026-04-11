package com.passwordmanager.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private DatabaseInitializer() {
    }

    public static void initializeDatabase() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK (role = 'STANDARD')
                )
                """;

        String createCredentialsTable = """
                CREATE TABLE IF NOT EXISTS credentials (
                    cred_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    site_name TEXT NOT NULL,
                    site_username TEXT NOT NULL,
                    encrypted_password TEXT NOT NULL,
                    notes TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
                """;

        String createDocumentsTable = """
                CREATE TABLE IF NOT EXISTS secure_documents (
                    document_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    original_file_name TEXT NOT NULL,
                    stored_file_path TEXT NOT NULL,
                    mime_type TEXT,
                    category TEXT,
                    notes TEXT,
                    file_size_bytes INTEGER NOT NULL,
                    date_added TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute(createUsersTable);
            statement.execute(createCredentialsTable);
            statement.execute(createDocumentsTable);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize database: " + exception.getMessage(), exception);
        }
    }
}
