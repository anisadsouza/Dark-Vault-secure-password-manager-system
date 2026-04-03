package com.passwordmanager.ui;

import com.passwordmanager.model.Credential;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.passwordmanager.util.PasswordMasker;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class PasswordManagerFrame extends JFrame {
    private static final String AUTH_CARD = "AUTH";
    private static final String DASHBOARD_CARD = "DASHBOARD";

    private final AuthService authService;
    private final CredentialService credentialService;
    private final CardLayout cardLayout;
    private final JPanel rootPanel;
    private final DefaultTableModel credentialTableModel;
    private final JTable credentialTable;
    private final JLabel welcomeLabel;
    private final JButton viewUsersButton;
    private final JTextField searchField;

    private User currentUser;
    private List<Credential> currentCredentials;

    public PasswordManagerFrame(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
        this.cardLayout = new CardLayout();
        this.rootPanel = new JPanel(cardLayout);
        this.credentialTableModel = new DefaultTableModel(
                new Object[]{"ID", "Site", "Username", "Password", "Notes"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.credentialTable = new JTable(credentialTableModel);
        this.welcomeLabel = new JLabel("Welcome");
        this.viewUsersButton = new JButton("Registered Users");
        this.searchField = new JTextField(18);
        this.currentCredentials = List.of();

        configureFrame();
        rootPanel.add(buildAuthPanel(), AUTH_CARD);
        rootPanel.add(buildDashboardPanel(), DASHBOARD_CARD);
        setContentPane(rootPanel);
        showAuthScreen();
    }

    public static void launch(AuthService authService, CredentialService credentialService) {
        SwingUtilities.invokeLater(() -> {
            installSystemLookAndFeel();
            PasswordManagerFrame frame = new PasswordManagerFrame(authService, credentialService);
            frame.setVisible(true);
        });
    }

    private void configureFrame() {
        setTitle("Secure Password Manager System");
        setSize(1080, 720);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel buildAuthPanel() {
        JPanel wrapper = new JPanel(new GridLayout(1, 2, 28, 0));
        wrapper.setBorder(BorderFactory.createEmptyBorder(36, 36, 36, 36));
        wrapper.setBackground(new Color(244, 247, 252));

        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBackground(new Color(20, 31, 54));
        heroPanel.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        JLabel title = new JLabel("Secure Password Manager");
        title.setFont(new Font("Avenir Next", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>Store website credentials securely with encryption, JDBC, and clean object-oriented design.</html>");
        subtitle.setFont(new Font("Avenir Next", Font.PLAIN, 16));
        subtitle.setForeground(new Color(222, 229, 244));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel features = new JLabel("""
                <html>
                <div style='font-size:14px; line-height:1.6;'>
                • Register and log in as standard or admin user<br/>
                • Add, search, update, and delete credentials<br/>
                • Passwords stay masked in the interface<br/>
                • Admins can view all registered users
                </div>
                </html>
                """);
        features.setForeground(new Color(187, 201, 230));
        features.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroPanel.add(title);
        heroPanel.add(Box.createVerticalStrut(18));
        heroPanel.add(subtitle);
        heroPanel.add(Box.createVerticalStrut(28));
        heroPanel.add(features);
        heroPanel.add(Box.createVerticalGlue());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Avenir Next", Font.BOLD, 14));
        tabbedPane.addTab("Login", buildLoginPanel());
        tabbedPane.addTab("Register", buildRegisterPanel());

        wrapper.add(heroPanel);
        wrapper.add(tabbedPane);
        return wrapper;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = buildFormPanel();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = buildPrimaryButton("Login");

        panel.add(createFieldPanel("Username", usernameField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldPanel("Password", passwordField));
        panel.add(Box.createVerticalStrut(20));

        loginButton.addActionListener(event -> {
            try {
                Optional<User> userOptional = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
                if (userOptional.isEmpty()) {
                    showError("Invalid username or password.");
                    return;
                }

                currentUser = userOptional.get();
                showDashboard();
                usernameField.setText("");
                passwordField.setText("");
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });

        panel.add(loginButton);
        return wrapCenteredPanel(panel);
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = buildFormPanel();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STANDARD", "ADMIN"});
        JButton registerButton = buildPrimaryButton("Create Account");

        panel.add(createFieldPanel("Username", usernameField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldPanel("Password", passwordField));
        panel.add(Box.createVerticalStrut(14));
        panel.add(createFieldPanel("Role", roleBox));
        panel.add(Box.createVerticalStrut(20));

        registerButton.addActionListener(event -> {
            try {
                boolean registered = authService.registerUser(
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        String.valueOf(roleBox.getSelectedItem())
                );

                if (registered) {
                    JOptionPane.showMessageDialog(this, "User registered successfully.");
                    usernameField.setText("");
                    passwordField.setText("");
                    roleBox.setSelectedIndex(0);
                }
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });

        panel.add(registerButton);
        return wrapCenteredPanel(panel);
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(new Color(245, 247, 250));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        welcomeLabel.setFont(new Font("Avenir Next", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(25, 36, 58));

        JButton logoutButton = buildSecondaryButton("Logout");
        logoutButton.addActionListener(event -> logout());

        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton addButton = buildPrimaryButton("Add");
        JButton editButton = buildSecondaryButton("Edit");
        JButton deleteButton = buildSecondaryButton("Delete");
        JButton revealButton = buildSecondaryButton("Reveal Password");
        JButton refreshButton = buildSecondaryButton("Refresh");
        JButton summaryButton = buildSecondaryButton("Site Summary");

        addButton.addActionListener(event -> openCredentialDialog(null));
        editButton.addActionListener(event -> editSelectedCredential());
        deleteButton.addActionListener(event -> deleteSelectedCredential());
        revealButton.addActionListener(event -> revealSelectedPassword());
        refreshButton.addActionListener(event -> refreshCredentialTable());
        summaryButton.addActionListener(event -> showSiteSummary());

        searchField.putClientProperty("JTextField.placeholderText", "Search by site or username");
        JButton searchButton = buildSecondaryButton("Search");
        searchButton.addActionListener(event -> searchCredentials());

        viewUsersButton.addActionListener(event -> showRegisteredUsers());

        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(revealButton);
        toolbar.add(refreshButton);
        toolbar.add(summaryButton);
        toolbar.add(searchField);
        toolbar.add(searchButton);
        toolbar.add(viewUsersButton);

        credentialTable.setRowHeight(28);
        credentialTable.getTableHeader().setFont(new Font("Avenir Next", Font.BOLD, 13));
        credentialTable.setFont(new Font("Avenir Next", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(credentialTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 227, 239)));

        JPanel topSection = new JPanel(new BorderLayout(0, 14));
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(toolbar, BorderLayout.SOUTH);

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 229, 238)),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));
        return panel;
    }

    private JPanel createFieldPanel(String labelText, Component component) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 8));
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Avenir Next", Font.BOLD, 14));
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel wrapCenteredPanel(JPanel formPanel) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(244, 247, 252));
        container.setBorder(BorderFactory.createEmptyBorder(48, 36, 48, 36));
        container.add(formPanel, BorderLayout.NORTH);
        return container;
    }

    private JButton buildPrimaryButton(String title) {
        JButton button = new JButton(title);
        button.setBackground(new Color(30, 122, 86));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private JButton buildSecondaryButton(String title) {
        JButton button = new JButton(title);
        button.setBackground(new Color(233, 238, 248));
        button.setForeground(new Color(32, 45, 74));
        button.setFocusPainted(false);
        return button;
    }

    private void showAuthScreen() {
        currentUser = null;
        cardLayout.show(rootPanel, AUTH_CARD);
    }

    private void showDashboard() {
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (" + currentUser.getDisplayRole() + ")");
        viewUsersButton.setVisible(currentUser.canManageUsers());
        refreshCredentialTable();
        cardLayout.show(rootPanel, DASHBOARD_CARD);
    }

    private void refreshCredentialTable() {
        if (currentUser == null) {
            return;
        }

        currentCredentials = credentialService.getCredentialsForUser(currentUser.getUserId());
        populateTable(currentCredentials);
    }

    private void populateTable(List<Credential> credentials) {
        credentialTableModel.setRowCount(0);

        for (Credential credential : credentials) {
            credentialTableModel.addRow(new Object[]{
                    credential.getCredentialId(),
                    credential.getSiteName(),
                    credential.getSiteUsername(),
                    PasswordMasker.mask(credential.getPassword()),
                    credential.getNotes().isBlank() ? "-" : credential.getNotes()
            });
        }
    }

    private void searchCredentials() {
        if (currentUser == null) {
            return;
        }

        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshCredentialTable();
            return;
        }

        try {
            currentCredentials = credentialService.searchCredentials(currentUser.getUserId(), keyword);
            populateTable(currentCredentials);
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void openCredentialDialog(Credential existingCredential) {
        JTextField siteField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        if (existingCredential != null) {
            siteField.setText(existingCredential.getSiteName());
            usernameField.setText(existingCredential.getSiteUsername());
            passwordField.setText(existingCredential.getPassword());
            notesArea.setText(existingCredential.getNotes());
        }

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.add(new JLabel("Site Name"));
        form.add(siteField);
        form.add(new JLabel("Account Username"));
        form.add(usernameField);
        form.add(new JLabel("Password"));
        form.add(passwordField);
        form.add(new JLabel("Notes"));
        form.add(new JScrollPane(notesArea));

        String title = existingCredential == null ? "Add Credential" : "Edit Credential";
        int choice = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            boolean success;
            if (existingCredential == null) {
                success = credentialService.addCredential(
                        currentUser.getUserId(),
                        siteField.getText(),
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        notesArea.getText()
                );
            } else {
                success = credentialService.updateCredential(
                        existingCredential.getCredentialId(),
                        currentUser.getUserId(),
                        siteField.getText(),
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        notesArea.getText()
                );
            }

            if (success) {
                refreshCredentialTable();
                JOptionPane.showMessageDialog(this, existingCredential == null ? "Credential added successfully." : "Credential updated successfully.");
            } else {
                showError("The credential could not be saved.");
            }
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void editSelectedCredential() {
        Credential selectedCredential = getSelectedCredential();
        if (selectedCredential == null) {
            showError("Please select a credential first.");
            return;
        }

        openCredentialDialog(selectedCredential);
    }

    private void deleteSelectedCredential() {
        Credential selectedCredential = getSelectedCredential();
        if (selectedCredential == null) {
            showError("Please select a credential first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected credential for " + selectedCredential.getSiteName() + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = credentialService.deleteCredential(selectedCredential.getCredentialId(), currentUser.getUserId());
        if (deleted) {
            refreshCredentialTable();
            JOptionPane.showMessageDialog(this, "Credential deleted successfully.");
        } else {
            showError("Credential deletion failed.");
        }
    }

    private void revealSelectedPassword() {
        Credential selectedCredential = getSelectedCredential();
        if (selectedCredential == null) {
            showError("Please select a credential first.");
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Password for " + selectedCredential.getSiteName() + ": " + selectedCredential.getPassword(),
                "Revealed Password",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showSiteSummary() {
        Map<String, Integer> summary = credentialService.buildSiteSummary(currentUser.getUserId());
        if (summary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No credentials available to summarize.");
            return;
        }

        StringBuilder builder = new StringBuilder("Site Summary\n\n");
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            builder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append(" account(s)\n");
        }

        JOptionPane.showMessageDialog(this, builder.toString());
    }

    private void showRegisteredUsers() {
        List<User> users = authService.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registered users found.");
            return;
        }

        StringBuilder builder = new StringBuilder("Registered Users\n\n");
        for (User user : users) {
            builder.append("ID: ")
                    .append(user.getUserId())
                    .append(" | Username: ")
                    .append(user.getUsername())
                    .append(" | Role: ")
                    .append(user.getDisplayRole())
                    .append('\n');
        }

        JTextArea textArea = new JTextArea(builder.toString(), 12, 36);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Registered Users", JOptionPane.INFORMATION_MESSAGE);
    }

    private Credential getSelectedCredential() {
        int selectedRow = credentialTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentCredentials.size()) {
            return null;
        }

        int credentialId = (int) credentialTableModel.getValueAt(selectedRow, 0);
        return currentCredentials.stream()
                .filter(credential -> credential.getCredentialId() == credentialId)
                .findFirst()
                .orElse(null);
    }

    private void logout() {
        currentCredentials = List.of();
        credentialTableModel.setRowCount(0);
        searchField.setText("");
        showAuthScreen();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
