package com.passwordmanager;

import com.passwordmanager.config.DatabaseInitializer;
import com.passwordmanager.dao.CredentialDAO;
import com.passwordmanager.dao.DocumentDAO;
import com.passwordmanager.dao.UserDAO;
import com.passwordmanager.dao.impl.CredentialDAOImpl;
import com.passwordmanager.dao.impl.DocumentDAOImpl;
import com.passwordmanager.dao.impl.UserDAOImpl;
import com.passwordmanager.service.AESEncryptionService;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.passwordmanager.service.DocumentService;
import com.passwordmanager.service.EncryptionService;
import com.passwordmanager.web.PasswordManagerWebServer;

public class MainApp {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();

        UserDAO userDAO = new UserDAOImpl();
        CredentialDAO credentialDAO = new CredentialDAOImpl();
        DocumentDAO documentDAO = new DocumentDAOImpl();
        EncryptionService encryptionService = new AESEncryptionService();

        AuthService authService = new AuthService(userDAO);
        CredentialService credentialService = new CredentialService(credentialDAO, encryptionService);
        DocumentService documentService = new DocumentService(documentDAO);

        PasswordManagerWebServer webServer = new PasswordManagerWebServer(authService, credentialService, documentService);
        int port = startWebServer(webServer, args);

        System.out.println("Dark Vault is running at http://localhost:" + port);
        System.out.println("Open that URL in Chrome to use the browser interface.");
    }

    private static int resolvePort(String[] args) {
        for (String arg : args) {
            if (arg != null && arg.startsWith("--port=")) {
                return Integer.parseInt(arg.substring("--port=".length()));
            }
        }

        return 8080;
    }

    private static int startWebServer(PasswordManagerWebServer webServer, String[] args) {
        int preferredPort = resolvePort(args);

        if (hasExplicitPort(args)) {
            webServer.start(preferredPort);
            return preferredPort;
        }

        int[] fallbackPorts = {preferredPort, 8081, 8082, 8083};
        for (int port : fallbackPorts) {
            try {
                webServer.start(port);
                return port;
            } catch (IllegalStateException exception) {
                if (!exception.getMessage().contains("Unable to start web server on port")) {
                    throw exception;
                }
            }
        }

        throw new IllegalStateException("Unable to start web server on ports 8080-8083.");
    }

    private static boolean hasExplicitPort(String[] args) {
        for (String arg : args) {
            if (arg != null && arg.startsWith("--port=")) {
                return true;
            }
        }

        return false;
    }
}
