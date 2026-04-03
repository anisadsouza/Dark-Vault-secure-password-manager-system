package com.passwordmanager.model;

public class AdminUser extends User {
    public AdminUser() {
        setRole("ADMIN");
    }

    public AdminUser(int userId, String username, String passwordHash) {
        super(userId, username, passwordHash, "ADMIN");
    }

    @Override
    public boolean canManageUsers() {
        return true;
    }

    @Override
    public String getDisplayRole() {
        return "Administrator";
    }
}
