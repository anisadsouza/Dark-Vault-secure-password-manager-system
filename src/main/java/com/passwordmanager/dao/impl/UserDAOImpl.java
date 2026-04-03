package com.passwordmanager.dao.impl;

import com.passwordmanager.config.DatabaseConfig;
import com.passwordmanager.dao.UserDAO;
import com.passwordmanager.model.AdminUser;
import com.passwordmanager.model.StandardUser;
import com.passwordmanager.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save user: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id, username, password_hash, role FROM users WHERE username = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapUser(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch user: " + exception.getMessage(), exception);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT user_id, username, password_hash, role FROM users ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to fetch users: " + exception.getMessage(), exception);
        }

        return users;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int userId = resultSet.getInt("user_id");
        String username = resultSet.getString("username");
        String passwordHash = resultSet.getString("password_hash");
        String role = resultSet.getString("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new AdminUser(userId, username, passwordHash);
        }

        return new StandardUser(userId, username, passwordHash);
    }
}
