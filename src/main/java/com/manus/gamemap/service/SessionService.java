package com.manus.gamemap.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

    private static class SessionInfo {
        UUID userId;
        long lastAccessTime;

        SessionInfo(UUID userId) {
            this.userId = userId;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    // Map sessionId -> SessionInfo
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    public String createSession(UUID userId) {
        String sessionId = UUID.randomUUID().toString();
        activeSessions.put(sessionId, new SessionInfo(userId));
        return sessionId;
    }

    public boolean isValidSession(String sessionId) {
        if (sessionId == null || !activeSessions.containsKey(sessionId)) {
            return false;
        }

        SessionInfo info = activeSessions.get(sessionId);
        long currentTime = System.currentTimeMillis();

        if (currentTime - info.lastAccessTime > SESSION_TIMEOUT_MS) {
            // Session expired
            activeSessions.remove(sessionId);
            return false;
        }

        // Update last access time (sliding expiration)
        info.lastAccessTime = currentTime;
        return true;
    }

    public UUID getUserIdFromSession(String sessionId) {
        SessionInfo info = activeSessions.get(sessionId);
        return info != null ? info.userId : null;
    }

    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            activeSessions.remove(sessionId);
        }
    }
}
