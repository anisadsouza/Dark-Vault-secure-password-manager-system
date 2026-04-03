package com.passwordmanager;

import com.passwordmanager.config.DatabaseInitializer;
import com.passwordmanager.dao.CredentialDAO;
import com.passwordmanager.dao.UserDAO;
import com.passwordmanager.dao.impl.CredentialDAOImpl;
import com.passwordmanager.dao.impl.UserDAOImpl;
import com.passwordmanager.service.AESEncryptionService;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.passwordmanager.service.EncryptionService;
import com.passwordmanager.ui.ConsoleUI;
import com.passwordmanager.web.PasswordManagerWebServer;

public class MainApp {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        UserDAO userDAO = new UserDAOImpl();
        CredentialDAO credentialDAO = new CredentialDAOImpl();
        EncryptionService encryptionService = new AESEncryptionService();

        AuthService authService = new AuthService(userDAO);
        CredentialService credentialService = new CredentialService(credentialDAO, encryptionService);

        if (args.length > 0 && "--console".equalsIgnoreCase(args[0])) {
            ConsoleUI consoleUI = new ConsoleUI(authService, credentialService);
            consoleUI.start();
            return;
        }

        PasswordManagerWebServer webServer = new PasswordManagerWebServer(authService, credentialService);
        int port = 8080;
        webServer.start(port);

        System.out.println("Dark Vault is running at http://localhost:" + port);
        System.out.println("Open that URL in Chrome to use the browser interface.");
    }
}
