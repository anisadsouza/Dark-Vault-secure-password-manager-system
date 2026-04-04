package com.passwordmanager.model;

public class StandardUser extends User {
    public StandardUser() {
        setRole("STANDARD");
    }

    public StandardUser(int userId, String username, String passwordHash) {
        super(userId, username, passwordHash, "STANDARD");
    }

    @Override
    public String getDisplayRole() {
        return "Standard User";
    }
}
