package com.acadify;
import com.sun.net.httpserver.HttpExchange;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
public class SessionUtil {
    private static final ConcurrentHashMap<String, SessionData> sessions = new ConcurrentHashMap<>();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long SESSION_TIMEOUT_MS = 24 * 60 * 60 * 1000;
    private static final long CLEANUP_INTERVAL_MS = 60 * 60 * 1000;
    private static long lastCleanup = System.currentTimeMillis();
    private static class SessionData {
        final String userId;
        final String role;
        final long expiresAt;
        SessionData(String userId, String role, long expiresAt) {
            this.userId = userId;
            this.role = role;
            this.expiresAt = expiresAt;
        }
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
    public static String createSession(String userId, String role) {
        String token = generateSecureToken();
        long expiresAt = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
        sessions.put(token, new SessionData(userId, role, expiresAt));
        cleanupExpiredSessions();
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
            return null;
        }
        return new String[]{session.userId, session.role};
    }
    public static void invalidate(String token) {
        if (token != null) {
            sessions.remove(token);
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
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
        System.out.println("[SessionUtil] Cleaned up expired sessions. Active sessions: " + sessions.size());
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
        return newToken;
    }
}
