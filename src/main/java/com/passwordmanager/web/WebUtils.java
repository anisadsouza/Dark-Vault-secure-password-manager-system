package com.passwordmanager.web;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WebUtils {
    private WebUtils() {
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseFormData(String rawFormData) {
        Map<String, String> values = new HashMap<>();
        if (rawFormData == null || rawFormData.isBlank()) {
            return values;
        }

        String[] pairs = rawFormData.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            values.put(key, value);
        }

        return values;
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    public static void sendResource(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        try (InputStream inputStream = WebUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                sendJson(exchange, 404, "{\"error\":\"Resource not found.\"}");
                return;
            }

            byte[] response = inputStream.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response);
            }
        }
    }

    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static String getCookie(HttpExchange exchange, String cookieName) {
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders == null) {
            return "";
        }

        for (String header : cookieHeaders) {
            String[] cookies = header.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && cookieName.equals(parts[0].trim())) {
                    return parts[1].trim();
                }
            }
        }

        return "";
    }

    public static void setSessionCookie(HttpExchange exchange, String cookieName, String cookieValue) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                cookieName + "=" + cookieValue + "; Path=/; HttpOnly; SameSite=Lax"
        );
    }

    public static void clearSessionCookie(HttpExchange exchange, String cookieName) {
        exchange.getResponseHeaders().add(
                "Set-Cookie",
                cookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
