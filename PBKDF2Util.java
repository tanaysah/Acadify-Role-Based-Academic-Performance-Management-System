package com.acadify;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;
public class PBKDF2Util {
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);
            return "$pbkdf2$" + ITERATIONS + "$" + saltB64 + "$" + hashB64;
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            if (storedHash.length() == 64 && !storedHash.contains("$")) {
                System.err.println("[SECURITY WARNING] Legacy SHA-256 hash detected. User should reset password.");
                return false;
            }
            String[] parts = storedHash.split("\\$");
            if (parts.length != 5 || !parts[1].equals("pbkdf2")) {
                return false;
            }
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[4]);
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, KEY_LENGTH);
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            System.err.println("[PBKDF2Util] Password verification error: " + e.getMessage());
            return false;
        }
    }
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
