package com.acadify;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
public class MainApplication {
    private static final int PORT = 8080;
    public static void main(String[] args) {
        try {
            DatabaseConfig.initialize();
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(16));
            server.createContext("/auth/", MainApplication::routeAuth);
            server.createContext("/student/", MainApplication::routeStudent);
            server.createContext("/teacher/", MainApplication::routeTeacher);
            server.createContext("/admin/", MainApplication::routeAdmin);
            server.start();
            System.out.println("[Acadify] Server running on port " + PORT);
        } catch (IOException e) {
            System.err.println("[Acadify] FATAL: Failed to start server – " + e.getMessage());
            System.exit(1);
        }
    }
    private static void routeAuth(HttpExchange exchange) {
        try {
            AuthController.handle(exchange);
        } catch (Exception e) {
            System.err.println("[MainApplication] Auth error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
    private static void routeStudent(HttpExchange exchange) {
        try {
            int[] session = validateSession(exchange, "STUDENT");
            if (session == null) return;
            int userId = session[0];
            int studentId = EntityResolver.resolveStudentId(userId);
            if (studentId == -1) {
                ResponseUtil.sendError(exchange, 404, "Student profile not found");
                return;
            }
            StudentController.handle(exchange, userId, studentId);
        } catch (Exception e) {
            System.err.println("[MainApplication] Student route error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
    private static void routeTeacher(HttpExchange exchange) {
        try {
            int[] session = validateSession(exchange, "TEACHER");
            if (session == null) return;
            int userId = session[0];
            int teacherId = EntityResolver.resolveTeacherId(userId);
            if (teacherId == -1) {
                ResponseUtil.sendError(exchange, 404, "Teacher profile not found");
                return;
            }
            TeacherController.handle(exchange, userId, teacherId);
        } catch (Exception e) {
            System.err.println("[MainApplication] Teacher route error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
    private static void routeAdmin(HttpExchange exchange) {
        try {
            int[] session = validateSession(exchange, "ADMIN");
            if (session == null) return;
            AdminController.handle(exchange, session[0]);
        } catch (Exception e) {
            System.err.println("[MainApplication] Admin route error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Internal server error");
        }
    }
    private static int[] validateSession(HttpExchange exchange, String requiredRole) throws IOException {
        String[] session = SessionUtil.extractAndValidate(exchange);
        if (session == null) {
            ResponseUtil.sendError(exchange, 401, "Unauthorized: Invalid or expired session");
            return null;
        }
        String userRole = session[1];
        if (!requiredRole.equalsIgnoreCase(userRole)) {
            System.err.println("[SECURITY] Unauthorized access attempt: User with role '" +
                             userRole + "' tried to access '" + requiredRole + "' endpoint");
            ResponseUtil.sendError(exchange, 403, "Forbidden: Insufficient privileges");
            return null;
        }
        return new int[]{Integer.parseInt(session[0])};
    }
}
