package com.passwordmanager.service;

import com.passwordmanager.dao.UserDAO;
import com.passwordmanager.model.AdminUser;
import com.passwordmanager.model.StandardUser;
import com.passwordmanager.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean registerUser(String username, String password, String role) {
        validateRequired(username, "Username");
        validateRequired(password, "Password");

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }

        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists. Please choose another username.");
        }

        User user;
        String passwordHash = hashPassword(password);

        if ("ADMIN".equalsIgnoreCase(role)) {
            user = new AdminUser(0, username.trim(), passwordHash);
        } else {
            user = new StandardUser(0, username.trim(), passwordHash);
        }

        return userDAO.addUser(user);
    }

    public Optional<User> login(String username, String password) {
        validateRequired(username, "Username");
        validateRequired(password, "Password");

        Optional<User> userOptional = userDAO.findByUsername(username.trim());
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!hashPassword(password).equals(user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    public List<User> getAllUsers() {
        return userDAO.findAllUsers();
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash password", exception);
        }
    }
}
