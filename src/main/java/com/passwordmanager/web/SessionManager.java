package com.passwordmanager.web;

import com.passwordmanager.model.User;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        return sessionId;
    }

    public Optional<User> getUser(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void destroySession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        sessions.remove(sessionId);
    }
}
