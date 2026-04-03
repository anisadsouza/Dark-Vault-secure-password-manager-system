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
import com.passwordmanager.ui.PasswordManagerFrame;
import java.awt.GraphicsEnvironment;

public class MainApp {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        UserDAO userDAO = new UserDAOImpl();
        CredentialDAO credentialDAO = new CredentialDAOImpl();
        EncryptionService encryptionService = new AESEncryptionService();

        AuthService authService = new AuthService(userDAO);
        CredentialService credentialService = new CredentialService(credentialDAO, encryptionService);
        if (GraphicsEnvironment.isHeadless()) {
            ConsoleUI consoleUI = new ConsoleUI(authService, credentialService);
            consoleUI.start();
            return;
        }

        PasswordManagerFrame.launch(authService, credentialService);
    }
}
