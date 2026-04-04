package com.passwordmanager.web;

import com.passwordmanager.model.User;
import com.passwordmanager.service.AuthService;
import com.passwordmanager.service.CredentialService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

public class PasswordManagerWebServer {
    private static final String SESSION_COOKIE = "PM_SESSION";
    private final AuthService authService;
    private final CredentialService credentialService;
    private final SessionManager sessionManager;
    private HttpServer server;

    public PasswordManagerWebServer(AuthService authService, CredentialService credentialService) {
        this.authService = authService;
        this.credentialService = credentialService;
        this.sessionManager = new SessionManager();
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleIndex);
            server.createContext("/assets/styles.css", this::handleStyles);
            server.createContext("/assets/app.js", this::handleScript);
            server.createContext("/api/register", this::handleRegister);
            server.createContext("/api/login", this::handleLogin);
            server.createContext("/api/logout", this::handleLogout);
            server.createContext("/api/session", this::handleSession);
            server.createContext("/api/credentials", this::handleCredentials);
            server.createContext("/api/summary", this::handleSummary);
            server.createContext("/api/users", this::handleUsers);
            server.createContext("/api/health", this::handleHealth);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to start web server on port " + port, exception);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            WebUtils.sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
            return;
        }

        WebUtils.sendResource(exchange, "/static/index.html", "text/html; charset=UTF-8");
    }

    private void handleStyles(HttpExchange exchange) throws IOException {
        WebUtils.sendResource(exchange, "/static/styles.css", "text/css; charset=UTF-8");
    }

    private void handleScript(HttpExchange exchange) throws IOException {
        WebUtils.sendResource(exchange, "/static/app.js", "application/javascript; charset=UTF-8");
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            WebUtils.sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
            return;
        }

        try {
            Map<String, String> formData = WebUtils.parseFormData(WebUtils.readBody(exchange));
            boolean registered = authService.registerUser(
                    formData.getOrDefault("username", ""),
                    formData.getOrDefault("password", ""),
                    "STANDARD"
            );

            if (registered) {
                WebUtils.sendJson(exchange, 200, "{\"success\":true,\"message\":\"User registered successfully.\"}");
                return;
            }

            WebUtils.sendJson(exchange, 400, "{\"success\":false,\"error\":\"Registration failed.\"}");
        } catch (IllegalArgumentException exception) {
            WebUtils.sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + WebUtils.escapeJson(exception.getMessage()) + "\"}");
        } catch (IllegalStateException exception) {
            sendServerError(exchange);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            WebUtils.sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
            return;
        }

        try {
            Map<String, String> formData = WebUtils.parseFormData(WebUtils.readBody(exchange));
            Optional<User> userOptional = authService.login(
                    formData.getOrDefault("username", ""),
                    formData.getOrDefault("password", "")
            );

            if (userOptional.isEmpty()) {
                WebUtils.sendJson(exchange, 401, "{\"success\":false,\"error\":\"Invalid username or password.\"}");
                return;
            }

            User user = userOptional.get();
            String sessionId = sessionManager.createSession(user);
            WebUtils.setSessionCookie(exchange, SESSION_COOKIE, sessionId);
            WebUtils.sendJson(exchange, 200, buildSessionJson(user));
        } catch (IllegalArgumentException exception) {
            WebUtils.sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + WebUtils.escapeJson(exception.getMessage()) + "\"}");
        } catch (IllegalStateException exception) {
            sendServerError(exchange);
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String sessionId = WebUtils.getCookie(exchange, SESSION_COOKIE);
        sessionManager.destroySession(sessionId);
        WebUtils.clearSessionCookie(exchange, SESSION_COOKIE);
        WebUtils.sendJson(exchange, 200, "{\"success\":true}");
    }

    private void handleSession(HttpExchange exchange) throws IOException {
        Optional<User> userOptional = currentUser(exchange);
        if (userOptional.isEmpty()) {
            WebUtils.sendJson(exchange, 200, "{\"authenticated\":false}");
            return;
        }

        WebUtils.sendJson(exchange, 200, buildSessionJson(userOptional.get()));
    }

    private void handleCredentials(HttpExchange exchange) throws IOException {
        Optional<User> userOptional = currentUser(exchange);
        if (userOptional.isEmpty()) {
            WebUtils.sendJson(exchange, 401, "{\"success\":false,\"error\":\"Please log in first.\"}");
            return;
        }

        User user = userOptional.get();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                String search = queryValue(exchange.getRequestURI(), "search");
                List<com.passwordmanager.model.Credential> credentials;
                if (search.isBlank()) {
                    credentials = credentialService.getCredentialsForUser(user.getUserId());
                } else {
                    credentials = credentialService.searchCredentials(user.getUserId(), search);
                }

                WebUtils.sendJson(exchange, 200, buildCredentialsJson(credentials));
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> formData = WebUtils.parseFormData(WebUtils.readBody(exchange));
                boolean saved = credentialService.addCredential(
                        user.getUserId(),
                        formData.getOrDefault("siteName", ""),
                        formData.getOrDefault("siteUsername", ""),
                        formData.getOrDefault("password", ""),
                        formData.getOrDefault("notes", "")
                );
                WebUtils.sendJson(exchange, saved ? 200 : 400, "{\"success\":" + saved + "}");
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                Map<String, String> formData = WebUtils.parseFormData(WebUtils.readBody(exchange));
                boolean updated = credentialService.updateCredential(
                        Integer.parseInt(formData.getOrDefault("credentialId", "0")),
                        user.getUserId(),
                        formData.getOrDefault("siteName", ""),
                        formData.getOrDefault("siteUsername", ""),
                        formData.getOrDefault("password", ""),
                        formData.getOrDefault("notes", "")
                );
                WebUtils.sendJson(exchange, updated ? 200 : 400, "{\"success\":" + updated + "}");
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                String credentialIdValue = queryValue(exchange.getRequestURI(), "id");
                boolean deleted = credentialService.deleteCredential(Integer.parseInt(credentialIdValue), user.getUserId());
                WebUtils.sendJson(exchange, deleted ? 200 : 400, "{\"success\":" + deleted + "}");
                return;
            }

            WebUtils.sendJson(exchange, 405, "{\"error\":\"Method not allowed.\"}");
        } catch (IllegalArgumentException exception) {
            WebUtils.sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + WebUtils.escapeJson(exception.getMessage()) + "\"}");
        } catch (IllegalStateException exception) {
            sendServerError(exchange);
        }
    }

    private void handleSummary(HttpExchange exchange) throws IOException {
        Optional<User> userOptional = currentUser(exchange);
        if (userOptional.isEmpty()) {
            WebUtils.sendJson(exchange, 401, "{\"success\":false,\"error\":\"Please log in first.\"}");
            return;
        }

        try {
            Map<String, Integer> summary = credentialService.buildSiteSummary(userOptional.get().getUserId());
            StringBuilder json = new StringBuilder();
            json.append("{\"items\":[");

            boolean first = true;
            for (Map.Entry<String, Integer> entry : summary.entrySet()) {
                if (!first) {
                    json.append(',');
                }
                first = false;
                json.append("{")
                        .append("\"siteName\":\"").append(WebUtils.escapeJson(entry.getKey())).append("\",")
                        .append("\"count\":").append(entry.getValue())
                        .append("}");
            }

            json.append("]}");
            WebUtils.sendJson(exchange, 200, json.toString());
        } catch (IllegalStateException exception) {
            sendServerError(exchange);
        }
    }

    private void handleUsers(HttpExchange exchange) throws IOException {
        Optional<User> userOptional = currentUser(exchange);
        if (userOptional.isEmpty()) {
            WebUtils.sendJson(exchange, 401, "{\"success\":false,\"error\":\"Please log in first.\"}");
            return;
        }

        User user = userOptional.get();
        if (!user.canManageUsers()) {
            WebUtils.sendJson(exchange, 403, "{\"success\":false,\"error\":\"Admin access required.\"}");
            return;
        }

        List<User> users = authService.getAllUsers();
        StringBuilder json = new StringBuilder();
        json.append("{\"items\":[");

        for (int index = 0; index < users.size(); index++) {
            User item = users.get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{")
                    .append("\"userId\":").append(item.getUserId()).append(",")
                    .append("\"username\":\"").append(WebUtils.escapeJson(item.getUsername())).append("\",")
                    .append("\"displayRole\":\"").append(WebUtils.escapeJson(item.getDisplayRole())).append("\"")
                    .append("}");
        }

        json.append("]}");
        WebUtils.sendJson(exchange, 200, json.toString());
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        WebUtils.sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"Web server is running.\"}");
    }

    private Optional<User> currentUser(HttpExchange exchange) {
        String sessionId = WebUtils.getCookie(exchange, SESSION_COOKIE);
        return sessionManager.getUser(sessionId);
    }

    private String buildCredentialsJson(List<com.passwordmanager.model.Credential> credentials) {
        StringBuilder json = new StringBuilder();
        json.append("{\"items\":[");

        for (int index = 0; index < credentials.size(); index++) {
            com.passwordmanager.model.Credential credential = credentials.get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{")
                    .append("\"credentialId\":").append(credential.getCredentialId()).append(",")
                    .append("\"siteName\":\"").append(WebUtils.escapeJson(credential.getSiteName())).append("\",")
                    .append("\"siteUsername\":\"").append(WebUtils.escapeJson(credential.getSiteUsername())).append("\",")
                    .append("\"password\":\"").append(WebUtils.escapeJson(credential.getPassword())).append("\",")
                    .append("\"notes\":\"").append(WebUtils.escapeJson(credential.getNotes())).append("\"")
                    .append("}");
        }

        json.append("]}");
        return json.toString();
    }

    private String buildSessionJson(User user) {
        return "{"
                + "\"authenticated\":true,"
                + "\"userId\":" + user.getUserId() + ","
                + "\"username\":\"" + WebUtils.escapeJson(user.getUsername()) + "\","
                + "\"role\":\"" + WebUtils.escapeJson(user.getRole()) + "\","
                + "\"displayRole\":\"" + WebUtils.escapeJson(user.getDisplayRole()) + "\","
                + "\"canManageUsers\":" + user.canManageUsers()
                + "}";
    }

    private String queryValue(URI uri, String key) {
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            return "";
        }

        return WebUtils.parseFormData(query).getOrDefault(key, "");
    }

    private void sendServerError(HttpExchange exchange) throws IOException {
        WebUtils.sendJson(exchange, 500, "{\"success\":false,\"error\":\"Database error occurred.\"}");
    }
}
