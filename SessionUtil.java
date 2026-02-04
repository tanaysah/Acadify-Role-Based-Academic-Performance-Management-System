package com.acadify;

import com.sun.net.httpserver.HttpExchange;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class SessionUtil {
    private static final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;
    private static final long MAX_SESSION_LIFETIME_MS = 24 * 60 * 60 * 1000;
    private static final long CLEANUP_INTERVAL_MS = 60 * 60 * 1000;
    private static long lastCleanup = System.currentTimeMillis();

    private static class SessionData {
        final String userId;
        final String role;
        final long createdAt;
        long lastAccessedAt;
        final long absoluteExpiresAt;

        SessionData(String userId, String role) {
            this.userId = userId;
            this.role = role;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = this.createdAt;
            this.absoluteExpiresAt = this.createdAt + MAX_SESSION_LIFETIME_MS;
        }

        boolean isExpired() {
            long now = System.currentTimeMillis();
            boolean inactivityExpired = (now - lastAccessedAt) > SESSION_TIMEOUT_MS;
            boolean lifetimeExpired = now > absoluteExpiresAt;
            return inactivityExpired || lifetimeExpired;
        }

        void updateLastAccessed() {
            this.lastAccessedAt = System.currentTimeMillis();
        }
        
        long getRemainingInactivityTime() {
            long now = System.currentTimeMillis();
            long timeSinceLastAccess = now - lastAccessedAt;
            long remaining = SESSION_TIMEOUT_MS - timeSinceLastAccess;
            return Math.max(0, remaining / 1000);
        }
    }

    public static String createSession(String userId, String role) {
        String token = generateSecureToken();
        sessions.put(token, new SessionData(userId, role));
        cleanupExpiredSessions();
        System.out.println("[SessionUtil] Session created for user " + userId + 
                         " (Role: " + role + ") - Inactivity timeout: " + 
                         (SESSION_TIMEOUT_MS / 60000) + " minutes");
        return token;
    }

    public static String[] extractAndValidate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        SessionData session = sessions.get(token);

        if (session == null) {
            return null;
        }

        if (session.isExpired()) {
            sessions.remove(token);
            System.out.println("[SessionUtil] Session expired and removed for user " + 
                             session.userId + " (inactive for " + 
                             ((System.currentTimeMillis() - session.lastAccessedAt) / 60000) + 
                             " minutes)");
            return null;
        }

        session.updateLastAccessed();
        
        return new String[]{session.userId, session.role};
    }

    public static void invalidate(String token) {
        if (token != null) {
            SessionData removed = sessions.remove(token);
            if (removed != null) {
                System.out.println("[SessionUtil] Session invalidated for user " + removed.userId);
            }
        }
    }

    private static String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private static void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_INTERVAL_MS) {
            return;
        }
        lastCleanup = now;
        
        int beforeCount = sessions.size();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterCount = sessions.size();
        int removed = beforeCount - afterCount;
        
        if (removed > 0) {
            System.out.println("[SessionUtil] Cleaned up " + removed + 
                             " expired sessions. Active sessions: " + afterCount);
        }
    }

    public static int getActiveSessionCount() {
        cleanupExpiredSessions();
        return sessions.size();
    }

    public static String rotateSession(String oldToken) {
        SessionData oldSession = sessions.get(oldToken);
        if (oldSession == null || oldSession.isExpired()) {
            return null;
        }

        String newToken = createSession(oldSession.userId, oldSession.role);
        sessions.remove(oldToken);
        
        System.out.println("[SessionUtil] Session rotated for user " + oldSession.userId);
        return newToken;
    }

    public static String getSessionInfo(String token) {
        SessionData session = sessions.get(token);
        if (session == null) {
            return null;
        }
        
        long now = System.currentTimeMillis();
        long age = (now - session.createdAt) / 1000;
        long inactiveSince = (now - session.lastAccessedAt) / 1000;
        long remainingInactivity = session.getRemainingInactivityTime();
        
        return String.format("Session Info - User: %s, Role: %s, Age: %ds, " +
                           "Inactive for: %ds, Remaining: %ds", 
                           session.userId, session.role, age, inactiveSince, remainingInactivity);
    }

    public static int forceCleanup() {
        int beforeCount = sessions.size();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterCount = sessions.size();
        int removed = beforeCount - afterCount;
        
        System.out.println("[SessionUtil] Force cleanup removed " + removed + 
                         " expired sessions. Active sessions: " + afterCount);
        return removed;
    }

    public static boolean isSessionValid(String token) {
        SessionData session = sessions.get(token);
        return session != null && !session.isExpired();
    }
}
