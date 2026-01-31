package com.acadify;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;
public class AuthController {
    public static void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        if (path.equals("/auth/login") && method.equals("POST")) {
            handleLogin(exchange);
        } else if (path.equals("/auth/register") && method.equals("POST")) {
            handleRegister(exchange);
        } else {
            ResponseUtil.sendError(exchange, 404, "Endpoint not found");
        }
    }
    private static void handleLogin(HttpExchange exchange) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password");
        String email = fields[0];
        String password = fields[1];
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            ResponseUtil.sendError(exchange, 400, "Email and password are required");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, role, password FROM users WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String role = rs.getString("role");
                String storedHash = rs.getString("password");
                rs.close();
                ps.close();
                if (PBKDF2Util.verifyPassword(password, storedHash)) {
                    logActivity(conn, userId, "LOGIN", "users", userId);
                    String token = SessionUtil.createSession(String.valueOf(userId), role);
                    ResponseUtil.sendJson(exchange, 200,
                            "{\"user_id\":" + userId + ",\"role\":\"" + role + "\",\"token\":\"" + token + "\"}");
                } else {
                    ResponseUtil.sendError(exchange, 401, "Invalid email or password");
                }
            } else {
                rs.close();
                ps.close();
                ResponseUtil.sendError(exchange, 401, "Invalid email or password");
            }
        } catch (SQLException e) {
            System.err.println("[AuthController] Login error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Authentication failed");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleRegister(HttpExchange exchange) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password", "role", "name");
        String email = fields[0];
        String password = fields[1];
        String role = fields[2];
        String name = fields[3];
        if (email == null || email.isBlank() || password == null || password.isBlank()
                || role == null || role.isBlank() || name == null || name.isBlank()) {
            ResponseUtil.sendError(exchange, 400, "email, password, role, and name are required");
            return;
        }
        role = role.toUpperCase();
        if (!role.equals("STUDENT") && !role.equals("TEACHER") && !role.equals("ADMIN")) {
            ResponseUtil.sendError(exchange, 400, "Invalid role. Must be STUDENT, TEACHER, or ADMIN");
            return;
        }
        String hashedPassword = PBKDF2Util.hashPassword(password);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT INTO users (email, password, role) VALUES (?, ?, ?) RETURNING user_id",
                    Statement.RETURN_GENERATED_KEYS);
            insertUser.setString(1, email);
            insertUser.setString(2, hashedPassword);
            insertUser.setString(3, role);
            insertUser.executeUpdate();
            ResultSet keys = insertUser.getGeneratedKeys();
            if (!keys.next()) {
                conn.rollback();
                ResponseUtil.sendError(exchange, 500, "Registration failed");
                return;
            }
            int userId = keys.getInt("user_id");
            keys.close();
            insertUser.close();
            insertRoleProfile(conn, role, userId, name);
            logActivity(conn, userId, "REGISTERED", "users", userId);
            conn.commit();
            conn.setAutoCommit(true);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Registration successful\",\"user_id\":" + userId + ",\"role\":\"" + role + "\"}");
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendError(exchange, 409, "Email already registered");
            } else {
                System.err.println("[AuthController] Register error: " + e.getMessage());
                ResponseUtil.sendError(exchange, 500, "Registration failed");
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void insertRoleProfile(Connection conn, String role, int userId, String name) throws SQLException {
        switch (role) {
            case "STUDENT": {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO students (user_id, name, age, roll_number, stream) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setInt(3, 18);
                ps.setString(4, "STU-" + userId);
                ps.setString(5, "General");
                ps.executeUpdate();
                ps.close();
                break;
            }
            case "TEACHER": {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO teachers (user_id, name, department, designation) VALUES (?, ?, ?, ?)");
                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setString(3, "General");
                ps.setString(4, "Lecturer");
                ps.executeUpdate();
                ps.close();
                break;
            }
            case "ADMIN": {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO admins (user_id, name) VALUES (?, ?)");
                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.executeUpdate();
                ps.close();
                break;
            }
        }
    }
    static void logActivity(Connection conn, int userId, String action, String entityType, int entityId) {
        try {
            CallableStatement cs = conn.prepareCall("{ CALL log_activity(?, ?, ?, ?) }");
            cs.setInt(1, userId);
            cs.setString(2, action);
            cs.setString(3, entityType);
            cs.setInt(4, entityId);
            cs.execute();
            cs.close();
        } catch (SQLException e) {
            System.err.println("[AuthController] Activity log failed: " + e.getMessage());
        }
    }
}
