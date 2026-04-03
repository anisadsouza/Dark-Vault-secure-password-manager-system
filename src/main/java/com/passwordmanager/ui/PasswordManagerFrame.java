package com.passwordmanager.ui;

import com.passwordmanager.model.Credential;
import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.passwordmanager.util.PasswordMasker;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PasswordManagerFrame extends JFrame {
    private static final String AUTH_CARD = "AUTH";
    private static final String DASHBOARD_CARD = "DASHBOARD";
    private static final String TABLE_CARD = "TABLE";
    private static final String EMPTY_CARD = "EMPTY";

    private static final Color APP_BACKGROUND = new Color(11, 16, 30);
    private static final Color APP_PANEL = new Color(20, 28, 50);
    private static final Color APP_PANEL_SOFT = new Color(29, 39, 67);
    private static final Color APP_PANEL_LIGHT = new Color(242, 246, 252);
    private static final Color TEXT_PRIMARY = new Color(14, 24, 46);
    private static final Color TEXT_MUTED = new Color(107, 120, 145);
    private static final Color TEXT_ON_DARK = new Color(244, 247, 255);
    private static final Color EMERALD = new Color(18, 181, 144);
    private static final Color CYAN = new Color(56, 189, 248);
    private static final Color CORAL = new Color(255, 123, 84);
    private static final Color GOLD = new Color(255, 193, 94);
    private static final Color TABLE_HEADER = new Color(23, 35, 63);
    private static final Color TABLE_ROW = new Color(251, 252, 255);
    private static final Color TABLE_ALT_ROW = new Color(242, 246, 252);

    private final AuthService authService;
    private final CredentialService credentialService;
    private final CardLayout rootCardLayout;
    private final CardLayout contentCardLayout;
    private final JPanel rootPanel;
    private final JPanel contentPanel;
    private final DefaultTableModel credentialTableModel;
    private final JTable credentialTable;
    private final JLabel welcomeLabel;
    private final JButton viewUsersButton;
    private final JTextField searchField;
    private final JLabel totalCredentialsValueLabel;
    private final JLabel totalSitesValueLabel;
    private final JLabel roleValueLabel;
    private final JLabel tableTitleLabel;
    private final JLabel emptyStateTitleLabel;
    private final JLabel emptyStateDescriptionLabel;
    private final JLabel statusLabel;

    private User currentUser;
    private List<Credential> currentCredentials;

    public PasswordManagerFrame(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
        this.rootCardLayout = new CardLayout();
        this.contentCardLayout = new CardLayout();
        this.rootPanel = new JPanel(rootCardLayout);
        this.contentPanel = new JPanel(contentCardLayout);
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
        this.viewUsersButton = new SoftButton("Registered Users", true);
        this.searchField = new JTextField(18);
        this.totalCredentialsValueLabel = new JLabel("0");
        this.totalSitesValueLabel = new JLabel("0");
        this.roleValueLabel = new JLabel("-");
        this.tableTitleLabel = new JLabel("Saved Credentials");
        this.emptyStateTitleLabel = new JLabel("No credentials yet");
        this.emptyStateDescriptionLabel = new JLabel("Add your first credential to start building your secure vault.");
        this.statusLabel = new JLabel("Ready");
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
        setSize(1260, 800);
        setMinimumSize(new Dimension(1040, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JPanel buildAuthPanel() {
        GradientPanel background = new GradientPanel(new Color(8, 14, 30), new Color(17, 64, 90));
        background.setLayout(new BorderLayout());
        background.setBorder(new EmptyBorder(34, 34, 34, 34));

        RoundedPanel shell = new RoundedPanel(36, new Color(246, 249, 255, 245));
        shell.setLayout(new GridLayout(1, 2, 28, 0));
        shell.setBorder(new EmptyBorder(28, 28, 28, 28));

        GradientPanel heroPanel = new GradientPanel(new Color(13, 27, 61), new Color(10, 124, 120));
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel appEyebrow = new JLabel("SECURE VAULT");
        appEyebrow.setFont(uiFont(Font.BOLD, 13));
        appEyebrow.setForeground(new Color(255, 215, 145));
        appEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html><div style='line-height:1.05;'>Store Secrets.<br/>Control Access.<br/>Own Your Digital Life.</div></html>");
        title.setFont(uiFont(Font.BOLD, 34));
        title.setForeground(TEXT_ON_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>This password manager gives you a bold desktop experience with secure storage, account search, role-based access, and encrypted credentials backed by JDBC.</html>");
        subtitle.setFont(uiFont(Font.PLAIN, 16));
        subtitle.setForeground(new Color(214, 229, 247));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel featureCard = new RoundedPanel(28, new Color(255, 255, 255, 34));
        featureCard.setLayout(new BoxLayout(featureCard, BoxLayout.Y_AXIS));
        featureCard.setBorder(new EmptyBorder(22, 22, 22, 22));
        featureCard.add(buildFeatureLine("Encrypted credential storage with masked list view"));
        featureCard.add(Box.createVerticalStrut(10));
        featureCard.add(buildFeatureLine("Add, update, delete, search, and summarize accounts"));
        featureCard.add(Box.createVerticalStrut(10));
        featureCard.add(buildFeatureLine("Admin access to registered users for presentation demos"));

        JPanel miniStats = new JPanel(new GridLayout(1, 3, 12, 0));
        miniStats.setOpaque(false);
        miniStats.add(buildMiniMetric("JDBC", "SQLite"));
        miniStats.add(buildMiniMetric("OOP", "Layered"));
        miniStats.add(buildMiniMetric("UI", "Desktop"));

        heroPanel.add(appEyebrow);
        heroPanel.add(Box.createVerticalStrut(18));
        heroPanel.add(title);
        heroPanel.add(Box.createVerticalStrut(18));
        heroPanel.add(subtitle);
        heroPanel.add(Box.createVerticalStrut(24));
        heroPanel.add(featureCard);
        heroPanel.add(Box.createVerticalGlue());
        heroPanel.add(miniStats);

        RoundedPanel authCard = new RoundedPanel(32, Color.WHITE);
        authCard.setLayout(new BorderLayout(0, 16));
        authCard.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel authTitle = new JLabel("Welcome Back");
        authTitle.setFont(uiFont(Font.BOLD, 28));
        authTitle.setForeground(TEXT_PRIMARY);

        JLabel authSubtitle = new JLabel("Log in or create a new account to enter your secure vault.");
        authSubtitle.setFont(uiFont(Font.PLAIN, 14));
        authSubtitle.setForeground(TEXT_MUTED);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(authTitle);
        heading.add(Box.createVerticalStrut(8));
        heading.add(authSubtitle);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(uiFont(Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Login", buildLoginPanel());
        tabbedPane.addTab("Register", buildRegisterPanel());

        authCard.add(heading, BorderLayout.NORTH);
        authCard.add(tabbedPane, BorderLayout.CENTER);

        shell.add(heroPanel);
        shell.add(authCard);
        background.add(shell, BorderLayout.CENTER);
        return background;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = buildFormPanel();
        JTextField usernameField = buildTextField();
        JPasswordField passwordField = buildPasswordField();
        JButton loginButton = new AccentButton("Enter Vault", EMERALD, new Color(12, 144, 112));

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
                usernameField.setText("");
                passwordField.setText("");
                showDashboard();
            } catch (IllegalArgumentException exception) {
                showError(exception.getMessage());
            }
        });

        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(createHintLabel("Tip: use an admin account if you want to present the user list feature."));
        return wrapFormContainer(panel);
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = buildFormPanel();
        JTextField usernameField = buildTextField();
        JPasswordField passwordField = buildPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STANDARD", "ADMIN"});
        roleBox.setFont(uiFont(Font.PLAIN, 14));
        roleBox.setBackground(Color.WHITE);
        JButton registerButton = new AccentButton("Create Account", CORAL, new Color(235, 97, 58));

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
        panel.add(Box.createVerticalStrut(10));
        panel.add(createHintLabel("Keep passwords at least 6 characters so the validation passes."));
        return wrapFormContainer(panel);
    }

    private JPanel buildDashboardPanel() {
        GradientPanel background = new GradientPanel(new Color(8, 13, 27), new Color(14, 50, 72));
        background.setLayout(new BorderLayout(24, 0));
        background.setBorder(new EmptyBorder(24, 24, 24, 24));

        RoundedPanel sidebar = new RoundedPanel(30, new Color(15, 22, 41, 228));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28, 24, 28, 24));

        JLabel brand = new JLabel("Vault");
        brand.setFont(uiFont(Font.BOLD, 30));
        brand.setForeground(TEXT_ON_DARK);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("<html><div style='line-height:1.4;'>A polished secure workspace for credentials, quick actions, and clean presentation demos.</div></html>");
        brandSub.setFont(uiFont(Font.PLAIN, 13));
        brandSub.setForeground(new Color(180, 195, 221));
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel sideInfo = new RoundedPanel(24, new Color(255, 255, 255, 20));
        sideInfo.setLayout(new BoxLayout(sideInfo, BoxLayout.Y_AXIS));
        sideInfo.setBorder(new EmptyBorder(18, 18, 18, 18));
        sideInfo.add(buildSidebarMetric("Credentials", totalCredentialsValueLabel));
        sideInfo.add(Box.createVerticalStrut(12));
        sideInfo.add(buildSidebarMetric("Sites", totalSitesValueLabel));
        sideInfo.add(Box.createVerticalStrut(12));
        sideInfo.add(buildSidebarMetric("Role", roleValueLabel));

        JButton addButton = new AccentButton("Add Credential", EMERALD, new Color(12, 144, 112));
        JButton editButton = new SoftButton("Edit Selected", true);
        JButton deleteButton = new SoftButton("Delete Selected", true);
        JButton revealButton = new SoftButton("Reveal Password", true);
        JButton summaryButton = new SoftButton("Site Summary", true);
        JButton refreshButton = new SoftButton("Refresh List", true);
        JButton logoutButton = new AccentButton("Logout", CORAL, new Color(235, 97, 58));

        addButton.addActionListener(event -> openCredentialDialog(null));
        editButton.addActionListener(event -> editSelectedCredential());
        deleteButton.addActionListener(event -> deleteSelectedCredential());
        revealButton.addActionListener(event -> revealSelectedPassword());
        summaryButton.addActionListener(event -> showSiteSummary());
        refreshButton.addActionListener(event -> refreshCredentialTable());
        logoutButton.addActionListener(event -> logout());
        viewUsersButton.addActionListener(event -> showRegisteredUsers());

        styleSidebarButton(viewUsersButton);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(26));
        sidebar.add(sideInfo);
        sidebar.add(Box.createVerticalStrut(24));
        sidebar.add(addButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(editButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(deleteButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(revealButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(summaryButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(refreshButton);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(viewUsersButton);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutButton);

        GradientPanel heroCard = new GradientPanel(new Color(18, 30, 58), new Color(18, 109, 102));
        heroCard.setLayout(new BorderLayout(18, 0));
        heroCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));

        welcomeLabel.setFont(uiFont(Font.BOLD, 30));
        welcomeLabel.setForeground(TEXT_ON_DARK);

        JLabel heroSub = new JLabel("<html>Search accounts instantly, manage encrypted credentials, and present your project with a more premium desktop look.</html>");
        heroSub.setFont(uiFont(Font.PLAIN, 14));
        heroSub.setForeground(new Color(212, 228, 245));
        heroSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroText.add(welcomeLabel);
        heroText.add(Box.createVerticalStrut(10));
        heroText.add(heroSub);

        JPanel statusPill = new RoundedPanel(22, new Color(255, 255, 255, 28));
        statusPill.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        statusLabel.setFont(uiFont(Font.BOLD, 13));
        statusLabel.setForeground(TEXT_ON_DARK);
        statusPill.add(statusLabel);

        heroCard.add(heroText, BorderLayout.CENTER);
        heroCard.add(statusPill, BorderLayout.EAST);

        RoundedPanel toolbarCard = new RoundedPanel(28, new Color(250, 252, 255, 240));
        toolbarCard.setLayout(new BorderLayout(16, 0));
        toolbarCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel toolbarLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbarLeft.setOpaque(false);
        JButton searchButton = new SoftButton("Search", false);
        JButton clearSearchButton = new SoftButton("Clear", false);
        searchButton.addActionListener(event -> searchCredentials());
        clearSearchButton.addActionListener(event -> clearSearch());
        searchField.addActionListener(event -> searchCredentials());
        styleSearchField(searchField);

        toolbarLeft.add(new JLabel("Find Credentials"));
        toolbarLeft.add(searchField);
        toolbarLeft.add(searchButton);
        toolbarLeft.add(clearSearchButton);

        JPanel toolbarRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarRight.setOpaque(false);
        toolbarRight.add(buildBadge("Encrypted", GOLD));
        toolbarRight.add(buildBadge("Desktop UI", CYAN));
        toolbarRight.add(buildBadge("JDBC Ready", EMERALD));

        toolbarCard.add(toolbarLeft, BorderLayout.WEST);
        toolbarCard.add(toolbarRight, BorderLayout.EAST);

        credentialTable.setRowHeight(34);
        credentialTable.setFont(uiFont(Font.PLAIN, 13));
        credentialTable.setForeground(TEXT_PRIMARY);
        credentialTable.setGridColor(new Color(227, 233, 244));
        credentialTable.setShowHorizontalLines(true);
        credentialTable.setShowVerticalLines(false);
        credentialTable.setSelectionBackground(new Color(215, 242, 235));
        credentialTable.setSelectionForeground(TEXT_PRIMARY);
        credentialTable.setIntercellSpacing(new Dimension(0, 1));
        credentialTable.setDefaultRenderer(Object.class, new VaultTableRenderer());
        credentialTable.getTableHeader().setFont(uiFont(Font.BOLD, 13));
        credentialTable.getTableHeader().setBackground(TABLE_HEADER);
        credentialTable.getTableHeader().setForeground(TEXT_ON_DARK);
        credentialTable.getTableHeader().setPreferredSize(new Dimension(0, 42));
        credentialTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(credentialTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(30, new Color(250, 252, 255, 246));
        tableCard.setLayout(new BorderLayout(0, 14));
        tableCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        tableTitleLabel.setFont(uiFont(Font.BOLD, 24));
        tableTitleLabel.setForeground(TEXT_PRIMARY);

        JLabel tableSubtitle = new JLabel("Your saved accounts are encrypted in storage and masked on screen.");
        tableSubtitle.setFont(uiFont(Font.PLAIN, 13));
        tableSubtitle.setForeground(TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.add(tableTitleLabel);
        headerText.add(Box.createVerticalStrut(6));
        headerText.add(tableSubtitle);

        tableHeader.add(headerText, BorderLayout.WEST);

        contentPanel.setOpaque(false);
        contentPanel.add(scrollPane, TABLE_CARD);
        contentPanel.add(buildEmptyStatePanel(), EMPTY_CARD);

        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(contentPanel, BorderLayout.CENTER);

        JPanel topStack = new JPanel(new BorderLayout(0, 18));
        topStack.setOpaque(false);
        topStack.add(heroCard, BorderLayout.NORTH);
        topStack.add(toolbarCard, BorderLayout.SOUTH);

        JPanel contentBackground = new JPanel(new BorderLayout(0, 18));
        contentBackground.setOpaque(false);
        contentBackground.add(topStack, BorderLayout.NORTH);
        contentBackground.add(tableCard, BorderLayout.CENTER);

        background.add(sidebar, BorderLayout.WEST);
        background.add(contentBackground, BorderLayout.CENTER);
        return background;
    }

    private JPanel buildEmptyStatePanel() {
        RoundedPanel panel = new RoundedPanel(26, new Color(255, 255, 255));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(52, 52, 52, 52));

        JLabel icon = new JLabel("VAULT");
        icon.setFont(uiFont(Font.BOLD, 16));
        icon.setForeground(CORAL);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateTitleLabel.setFont(uiFont(Font.BOLD, 28));
        emptyStateTitleLabel.setForeground(TEXT_PRIMARY);
        emptyStateTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyStateDescriptionLabel.setFont(uiFont(Font.PLAIN, 15));
        emptyStateDescriptionLabel.setForeground(TEXT_MUTED);
        emptyStateDescriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addFirstCredentialButton = new AccentButton("Add First Credential", EMERALD, new Color(12, 144, 112));
        addFirstCredentialButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFirstCredentialButton.addActionListener(event -> openCredentialDialog(null));

        panel.add(Box.createVerticalGlue());
        panel.add(icon);
        panel.add(Box.createVerticalStrut(16));
        panel.add(emptyStateTitleLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(emptyStateDescriptionLabel);
        panel.add(Box.createVerticalStrut(26));
        panel.add(addFirstCredentialButton);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel createFieldPanel(String labelText, Component component) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 8));
        fieldPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(uiFont(Font.BOLD, 14));
        label.setForeground(TEXT_PRIMARY);
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);
        return fieldPanel;
    }

    private JPanel wrapFormContainer(JPanel formPanel) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(18, 6, 6, 6));
        container.add(formPanel, BorderLayout.NORTH);
        return container;
    }

    private JTextField buildTextField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    private JPasswordField buildPasswordField() {
        JPasswordField field = new JPasswordField();
        styleInput(field);
        return field;
    }

    private void styleInput(JComponent component) {
        component.setFont(uiFont(Font.PLAIN, 14));
        component.setBackground(new Color(248, 250, 255));
        component.setForeground(TEXT_PRIMARY);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 223, 239), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
    }

    private void styleSearchField(JTextField field) {
        field.setFont(uiFont(Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(213, 221, 235), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        field.setPreferredSize(new Dimension(240, 42));
    }

    private Component buildFeatureLine(String text) {
        JLabel label = new JLabel("• " + text);
        label.setFont(uiFont(Font.PLAIN, 14));
        label.setForeground(new Color(237, 244, 255));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel buildMiniMetric(String title, String value) {
        RoundedPanel panel = new RoundedPanel(20, new Color(255, 255, 255, 24));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(uiFont(Font.PLAIN, 12));
        titleLabel.setForeground(new Color(189, 208, 230));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(uiFont(Font.BOLD, 18));
        valueLabel.setForeground(TEXT_ON_DARK);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(valueLabel);
        return panel;
    }

    private JPanel buildSidebarMetric(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(uiFont(Font.PLAIN, 12));
        titleLabel.setForeground(new Color(166, 184, 212));

        valueLabel.setFont(uiFont(Font.BOLD, 22));
        valueLabel.setForeground(TEXT_ON_DARK);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel buildBadge(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        label.setForeground(TEXT_PRIMARY);
        label.setFont(uiFont(Font.BOLD, 12));
        label.setBorder(new EmptyBorder(9, 12, 9, 12));
        return label;
    }

    private JLabel createHintLabel(String text) {
        JLabel hint = new JLabel(text);
        hint.setFont(uiFont(Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        return hint;
    }

    private void styleSidebarButton(JButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
    }

    private void showAuthScreen() {
        currentUser = null;
        rootCardLayout.show(rootPanel, AUTH_CARD);
    }

    private void showDashboard() {
        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        roleValueLabel.setText(currentUser.getDisplayRole());
        viewUsersButton.setVisible(currentUser.canManageUsers());
        clearSearch();
        refreshCredentialTable();
        setStatus("Signed in successfully.");
        rootCardLayout.show(rootPanel, DASHBOARD_CARD);
    }

    private void refreshCredentialTable() {
        if (currentUser == null) {
            return;
        }

        currentCredentials = credentialService.getCredentialsForUser(currentUser.getUserId());
        populateTable(currentCredentials, "No credentials yet", "Start by adding your first account to the vault.");
    }

    private void populateTable(List<Credential> credentials, String emptyTitle, String emptyDescription) {
        credentialTableModel.setRowCount(0);
        totalCredentialsValueLabel.setText(String.valueOf(credentials.size()));
        totalSitesValueLabel.setText(String.valueOf(countUniqueSites(credentials)));

        for (Credential credential : credentials) {
            credentialTableModel.addRow(new Object[]{
                    credential.getCredentialId(),
                    credential.getSiteName(),
                    credential.getSiteUsername(),
                    PasswordMasker.mask(credential.getPassword()),
                    credential.getNotes().isBlank() ? "-" : credential.getNotes()
            });
        }

        tableTitleLabel.setText("Saved Credentials");

        if (credentials.isEmpty()) {
            emptyStateTitleLabel.setText(emptyTitle);
            emptyStateDescriptionLabel.setText(emptyDescription);
            contentCardLayout.show(contentPanel, EMPTY_CARD);
            return;
        }

        contentCardLayout.show(contentPanel, TABLE_CARD);
    }

    private void searchCredentials() {
        if (currentUser == null) {
            return;
        }

        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshCredentialTable();
            setStatus("Showing all saved credentials.");
            return;
        }

        try {
            currentCredentials = credentialService.searchCredentials(currentUser.getUserId(), keyword);
            populateTable(
                    currentCredentials,
                    "Nothing matched your search",
                    "Try another site name or account username."
            );
            setStatus("Showing " + currentCredentials.size() + " search result(s).");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void clearSearch() {
        searchField.setText("");
        refreshCredentialTable();
        setStatus("Showing all saved credentials.");
    }

    private void openCredentialDialog(Credential existingCredential) {
        JTextField siteField = buildTextField();
        JTextField usernameField = buildTextField();
        JPasswordField passwordField = buildPasswordField();
        JTextArea notesArea = new JTextArea(4, 24);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(uiFont(Font.PLAIN, 14));
        notesArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (existingCredential != null) {
            siteField.setText(existingCredential.getSiteName());
            usernameField.setText(existingCredential.getSiteUsername());
            passwordField.setText(existingCredential.getPassword());
            notesArea.setText(existingCredential.getNotes());
        }

        RoundedPanel form = new RoundedPanel(26, new Color(248, 250, 255));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.add(createFieldPanel("Site Name", siteField));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldPanel("Account Username", usernameField));
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldPanel("Password", passwordField));
        if (existingCredential != null) {
            form.add(Box.createVerticalStrut(8));
            form.add(createHintLabel("Leave the password empty if you want to keep the current one."));
        }
        form.add(Box.createVerticalStrut(12));
        form.add(createFieldPanel("Notes", new JScrollPane(notesArea)));

        String title = existingCredential == null ? "Add Credential" : "Edit Credential";
        int choice = JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String passwordValue = new String(passwordField.getPassword());
            if (existingCredential != null && passwordValue.isBlank()) {
                passwordValue = existingCredential.getPassword();
            }

            boolean success;
            if (existingCredential == null) {
                success = credentialService.addCredential(
                        currentUser.getUserId(),
                        siteField.getText(),
                        usernameField.getText(),
                        passwordValue,
                        notesArea.getText()
                );
            } else {
                success = credentialService.updateCredential(
                        existingCredential.getCredentialId(),
                        currentUser.getUserId(),
                        siteField.getText(),
                        usernameField.getText(),
                        passwordValue,
                        notesArea.getText()
                );
            }

            if (success) {
                refreshCredentialTable();
                setStatus(existingCredential == null ? "Credential added successfully." : "Credential updated successfully.");
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
            setStatus("Credential deleted successfully.");
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
        setStatus("Password revealed for " + selectedCredential.getSiteName() + ".");
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

        JTextArea textArea = new JTextArea(builder.toString(), 10, 32);
        textArea.setEditable(false);
        textArea.setFont(uiFont(Font.PLAIN, 14));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Site Summary", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Site summary opened.");
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
        textArea.setFont(uiFont(Font.PLAIN, 14));
        textArea.setBorder(new EmptyBorder(16, 16, 16, 16));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Registered Users", JOptionPane.INFORMATION_MESSAGE);
        setStatus("Registered users opened.");
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
        totalCredentialsValueLabel.setText("0");
        totalSitesValueLabel.setText("0");
        roleValueLabel.setText("-");
        setStatus("Signed out.");
        showAuthScreen();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        setStatus(message);
    }

    private int countUniqueSites(List<Credential> credentials) {
        Set<String> uniqueSites = new HashSet<>();

        for (Credential credential : credentials) {
            uniqueSites.add(credential.getSiteName());
        }

        return uniqueSites.size();
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private Font uiFont(int style, int size) {
        return new Font("Avenir Next", style, size);
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private static final class GradientPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;

        private GradientPanel(Color startColor, Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Paint gradient = new LinearGradientPaint(
                    new Point(0, 0),
                    new Point(getWidth(), getHeight()),
                    new float[]{0f, 1f},
                    new Color[]{startColor, endColor}
            );
            graphics2D.setPaint(gradient);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            graphics2D.setColor(new Color(255, 255, 255, 18));
            graphics2D.fill(new Ellipse2D.Double(getWidth() - 260, 40, 220, 220));
            graphics2D.setColor(new Color(255, 255, 255, 12));
            graphics2D.fill(new Ellipse2D.Double(-80, getHeight() - 210, 240, 240));
            graphics2D.dispose();

            super.paintComponent(graphics);
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        private RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(new Color(0, 0, 0, 24));
            graphics2D.fillRoundRect(4, 8, getWidth() - 8, getHeight() - 8, radius, radius);
            graphics2D.setColor(backgroundColor);
            graphics2D.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 8, getHeight() - 8, radius, radius));
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AccentButton extends JButton {
        private final Color fillColor;
        private final Color hoverColor;
        private boolean hover;

        private AccentButton(String title, Color fillColor, Color hoverColor) {
            super(title);
            this.fillColor = fillColor;
            this.hoverColor = hoverColor;
            setFont(new Font("Avenir Next", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(12, 18, 12, 18));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            addHoverEffect();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(hover ? hoverColor : fillColor);
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            graphics2D.setColor(new Color(255, 255, 255, 30));
            graphics2D.setStroke(new BasicStroke(1.2f));
            graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }

        private void addHoverEffect() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hover = false;
                    repaint();
                }
            });
        }
    }

    private static final class SoftButton extends AccentButton {
        private SoftButton(String title, boolean darkSurface) {
            super(
                    title,
                    darkSurface ? new Color(255, 255, 255, 24) : new Color(17, 31, 58, 18),
                    darkSurface ? new Color(255, 255, 255, 40) : new Color(17, 31, 58, 30)
            );
            setForeground(darkSurface ? TEXT_ON_DARK : TEXT_PRIMARY);
        }
    }

    private static final class VaultTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(new EmptyBorder(0, 12, 0, 12));
            label.setFont(new Font("Avenir Next", Font.PLAIN, 13));

            if (isSelected) {
                label.setBackground(new Color(220, 245, 238));
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setBackground(row % 2 == 0 ? TABLE_ROW : TABLE_ALT_ROW);
                label.setForeground(TEXT_PRIMARY);
            }

            return label;
        }
    }
}
