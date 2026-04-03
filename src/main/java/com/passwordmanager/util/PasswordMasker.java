package com.passwordmanager.util;

public final class PasswordMasker {
    private PasswordMasker() {
    }

    public static String mask(String password) {
        if (password == null || password.isBlank()) {
            return "";
        }

        return "*".repeat(Math.max(8, password.length()));
    }
}
