package com.passwordmanager.ui;

import com.passwordmanager.model.Credential;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.passwordmanager.util.InputHelper;
import com.passwordmanager.util.PasswordMasker;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConsoleUI {
    private final AuthService authService;
    private final CredentialService credentialService;
    private final InputHelper inputHelper;

    public ConsoleUI(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
        this.inputHelper = new InputHelper();
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMainMenu();
            int choice = inputHelper.readInt("Choose an option: ", 1, 3);

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> {
                    running = false;
                    System.out.println("Thank you for using Secure Password Manager System.");
                }
                default -> System.out.println("Invalid option selected.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n=== Secure Password Manager System ===");
        System.out.println("1. Register User");
        System.out.println("2. Login");
        System.out.println("3. Exit");
    }

    private void registerUser() {
        try {
            String username = inputHelper.readNonEmptyLine("Enter username: ");
            String password = inputHelper.readNonEmptyLine("Enter password: ");
            int roleChoice = inputHelper.readInt("Choose role (1 = Standard User, 2 = Admin): ", 1, 2);
            String role = roleChoice == 2 ? "ADMIN" : "STANDARD";

            boolean registered = authService.registerUser(username, password, role);
            if (registered) {
                System.out.println("User registered successfully.");
            } else {
                System.out.println("User registration failed.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void loginUser() {
        try {
            String username = inputHelper.readNonEmptyLine("Enter username: ");
            String password = inputHelper.readNonEmptyLine("Enter password: ");

            Optional<User> userOptional = authService.login(username, password);
            if (userOptional.isEmpty()) {
                System.out.println("Invalid username or password.");
                return;
            }

            User currentUser = userOptional.get();
            System.out.println("Login successful. Welcome, " + currentUser.getUsername() + " (" + currentUser.getDisplayRole() + ").");
            showUserMenu(currentUser);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void showUserMenu(User currentUser) {
        boolean loggedIn = true;

        while (loggedIn) {
            printUserMenu(currentUser);
            int maxOption = currentUser.canManageUsers() ? 8 : 7;
            int choice = inputHelper.readInt("Choose an option: ", 1, maxOption);

            switch (choice) {
                case 1 -> addCredential(currentUser);
                case 2 -> viewCredentials(currentUser);
                case 3 -> updateCredential(currentUser);
                case 4 -> deleteCredential(currentUser);
                case 5 -> searchCredentials(currentUser);
                case 6 -> viewSiteSummary(currentUser);
                case 7 -> {
                    if (currentUser.canManageUsers()) {
                        viewRegisteredUsers();
                    } else {
                        loggedIn = false;
                        System.out.println("Logged out successfully.");
                    }
                }
                case 8 -> {
                    if (currentUser.canManageUsers()) {
                        loggedIn = false;
                        System.out.println("Logged out successfully.");
                    }
                }
                default -> System.out.println("Invalid option selected.");
            }
        }
    }

    private void printUserMenu(User currentUser) {
        System.out.println("\n=== User Dashboard ===");
        System.out.println("1. Add Credential");
        System.out.println("2. View Credentials");
        System.out.println("3. Update Credential");
        System.out.println("4. Delete Credential");
        System.out.println("5. Search Credential");
        System.out.println("6. View Site Summary");

        if (currentUser.canManageUsers()) {
            System.out.println("7. View Registered Users");
            System.out.println("8. Logout");
        } else {
            System.out.println("7. Logout");
        }
    }

    private void addCredential(User currentUser) {
        try {
            String siteName = inputHelper.readNonEmptyLine("Enter site name: ");
            String siteUsername = inputHelper.readNonEmptyLine("Enter account username/email: ");
            String password = inputHelper.readNonEmptyLine("Enter password: ");
            String notes = inputHelper.readOptionalLine("Enter notes (optional): ");

            boolean saved = credentialService.addCredential(
                    currentUser.getUserId(),
                    siteName,
                    siteUsername,
                    password,
                    notes
            );

            System.out.println(saved ? "Credential added successfully." : "Credential could not be added.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void viewCredentials(User currentUser) {
        List<Credential> credentials = credentialService.getCredentialsForUser(currentUser.getUserId());
        displayCredentials(credentials);
    }

    private void updateCredential(User currentUser) {
        try {
            int credentialId = inputHelper.readPositiveInt("Enter credential ID to update: ");
            Optional<Credential> existingCredentialOptional = credentialService.getCredentialById(credentialId, currentUser.getUserId());

            if (existingCredentialOptional.isEmpty()) {
                System.out.println("Credential not found.");
                return;
            }

            Credential existingCredential = existingCredentialOptional.get();
            String siteName = inputHelper.readOptionalLine("Enter site name [" + existingCredential.getSiteName() + "]: ");
            String siteUsername = inputHelper.readOptionalLine("Enter site username [" + existingCredential.getSiteUsername() + "]: ");
            String password = inputHelper.readOptionalLine("Enter password [hidden]: ");
            String notes = inputHelper.readOptionalLine("Enter notes [" + existingCredential.getNotes() + "]: ");

            boolean updated = credentialService.updateCredential(
                    credentialId,
                    currentUser.getUserId(),
                    siteName.isBlank() ? existingCredential.getSiteName() : siteName,
                    siteUsername.isBlank() ? existingCredential.getSiteUsername() : siteUsername,
                    password.isBlank() ? existingCredential.getPassword() : password,
                    notes.isBlank() ? existingCredential.getNotes() : notes
            );

            System.out.println(updated ? "Credential updated successfully." : "Credential update failed.");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void deleteCredential(User currentUser) {
        int credentialId = inputHelper.readPositiveInt("Enter credential ID to delete: ");
        String confirmation = inputHelper.readNonEmptyLine("Type YES to confirm deletion: ");

        if (!"YES".equalsIgnoreCase(confirmation)) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = credentialService.deleteCredential(credentialId, currentUser.getUserId());
        System.out.println(deleted ? "Credential deleted successfully." : "Credential not found or deletion failed.");
    }

    private void searchCredentials(User currentUser) {
        try {
            String keyword = inputHelper.readNonEmptyLine("Enter site name or username to search: ");
            List<Credential> searchResults = credentialService.searchCredentials(currentUser.getUserId(), keyword);
            displayCredentials(searchResults);
        } catch (IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void viewSiteSummary(User currentUser) {
        Map<String, Integer> summary = credentialService.buildSiteSummary(currentUser.getUserId());

        if (summary.isEmpty()) {
            System.out.println("No credentials available to summarize.");
            return;
        }

        System.out.println("\n=== Site Summary ===");
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue() + " account(s)");
        }
    }

    private void viewRegisteredUsers() {
        List<User> users = authService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No registered users found.");
            return;
        }

        System.out.println("\n=== Registered Users ===");
        for (User user : users) {
            System.out.println("ID: " + user.getUserId() + " | Username: " + user.getUsername() + " | Role: " + user.getDisplayRole());
        }
    }

    private void displayCredentials(List<Credential> credentials) {
        if (credentials.isEmpty()) {
            System.out.println("No credentials found.");
            return;
        }

        System.out.println("\n=== Saved Credentials ===");
        for (Credential credential : credentials) {
            System.out.println("ID: " + credential.getCredentialId());
            System.out.println("Site Name: " + credential.getSiteName());
            System.out.println("Account Username: " + credential.getSiteUsername());
            System.out.println("Password: " + PasswordMasker.mask(credential.getPassword()));
            System.out.println("Notes: " + (credential.getNotes().isBlank() ? "-" : credential.getNotes()));
            System.out.println("-----------------------------------");
        }
    }
}
