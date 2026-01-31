package com.acadify;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
public class DatabaseConfig {
    private static final int POOL_SIZE = 20;
    private static final Deque<Connection> pool = new ArrayDeque<>();
    private static String jdbcUrl;
    private static String username;
    private static String password;
    public static void initialize() {
        jdbcUrl = requireEnv("DB_URL");
        username = requireEnv("DB_USER");
        password = requireEnv("DB_PASSWORD");
        try {
            Class.forName("org.postgresql.Driver");
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.push(createConnection());
            }
            System.out.println("[DatabaseConfig] Connection pool initialized with " + POOL_SIZE + " connections.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DatabaseConfig] PostgreSQL JDBC driver not found on classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("[DatabaseConfig] Failed to initialize connection pool.", e);
        }
    }
    public static synchronized Connection getConnection() throws SQLException {
        Connection conn = pool.poll();
        if (conn == null || conn.isClosed()) {
            conn = createConnection();
        }
        return conn;
    }
    public static synchronized void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) {
                conn.setAutoCommit(true);
                pool.push(conn);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] Failed to release connection cleanly.");
        }
    }
    private static Connection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
        conn.setAutoCommit(true);
        return conn;
    }
    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("[DatabaseConfig] Missing required environment variable: " + key);
        }
        return value;
    }
}
