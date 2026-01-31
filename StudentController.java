package com.acadify;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;
public class StudentController {
    public static void handle(HttpExchange exchange, int userId, int studentId) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        switch (path) {
            case "/student/report":
                if (method.equals("GET")) handlePerformanceReport(exchange, studentId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/semester-performance":
                if (method.equals("GET")) handleSemesterPerformance(exchange, studentId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/marks-trend":
                if (method.equals("GET")) handleSubjectMarksTrend(exchange, studentId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/weak-subjects":
                if (method.equals("GET")) handleWeakSubjects(exchange, exchange.getRequestURI().getQuery(), studentId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/doubt":
                if (method.equals("POST")) handleRaiseDoubt(exchange, studentId, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/doubts":
                if (method.equals("GET")) handleGetDoubts(exchange, studentId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/student/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            default:
                ResponseUtil.sendError(exchange, 404, "Endpoint not found");
        }
    }
    private static void handlePerformanceReport(HttpExchange exchange, int studentId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_student_performance_report(?)");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = "{" +
                        "\"student_id\":" + rs.getInt("student_id") + "," +
                        "\"student_name\":\"" + rs.getString("student_name") + "\"," +
                        "\"roll_number\":\"" + rs.getString("roll_number") + "\"," +
                        "\"stream\":\"" + rs.getString("stream") + "\"," +
                        "\"current_cgpa\":" + rs.getBigDecimal("current_cgpa") + "," +
                        "\"total_subjects\":" + rs.getInt("total_subjects") + "," +
                        "\"overall_average\":" + rs.getBigDecimal("overall_average") + "," +
                        "\"total_backlogs\":" + rs.getInt("total_backlogs") + "," +
                        "\"semesters_completed\":" + rs.getInt("semesters_completed") +
                        "}";
                rs.close();
                ps.close();
                ResponseUtil.sendJson(exchange, 200, json);
            } else {
                rs.close();
                ps.close();
                ResponseUtil.sendError(exchange, 404, "No performance data found");
            }
        } catch (SQLException e) {
            System.err.println("[StudentController] PerformanceReport error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve report");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleSemesterPerformance(HttpExchange exchange, int studentId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_semester_performance(?)");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"semester\":").append(rs.getInt("semester")).append(",");
                json.append("\"total_subjects\":").append(rs.getInt("total_subjects")).append(",");
                json.append("\"average_marks\":").append(rs.getBigDecimal("average_marks")).append(",");
                json.append("\"semester_gpa\":").append(rs.getBigDecimal("semester_gpa")).append(",");
                json.append("\"backlogs\":").append(rs.getInt("backlogs"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[StudentController] SemesterPerformance error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve semester performance");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleSubjectMarksTrend(HttpExchange exchange, int studentId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_subject_marks_trend(?)");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"subject_id\":").append(rs.getInt("subject_id")).append(",");
                json.append("\"subject_name\":\"").append(rs.getString("subject_name")).append("\",");
                json.append("\"semester\":").append(rs.getInt("semester")).append(",");
                json.append("\"marks_obtained\":").append(rs.getBigDecimal("marks_obtained"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[StudentController] MarksTrend error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve marks trend");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleWeakSubjects(HttpExchange exchange, String query, int studentId) throws IOException {
        double threshold = 50.0;
        if (query != null && query.contains("threshold=")) {
            try {
                String val = query.substring(query.indexOf("threshold=") + 10);
                if (val.contains("&")) val = val.substring(0, val.indexOf("&"));
                threshold = Double.parseDouble(val);
            } catch (NumberFormatException ignored) {}
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_weak_subjects(?, ?)");
            ps.setInt(1, studentId);
            ps.setDouble(2, threshold);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"subject_id\":").append(rs.getInt("subject_id")).append(",");
                json.append("\"subject_name\":\"").append(rs.getString("subject_name")).append("\",");
                json.append("\"average_marks\":").append(rs.getBigDecimal("average_marks")).append(",");
                json.append("\"times_below_threshold\":").append(rs.getInt("times_below_threshold")).append(",");
                json.append("\"latest_semester\":").append(rs.getInt("latest_semester")).append(",");
                json.append("\"latest_marks\":").append(rs.getBigDecimal("latest_marks"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[StudentController] WeakSubjects error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve weak subjects");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleRaiseDoubt(HttpExchange exchange, int studentId, int userId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "teacher_id", "question");
        String teacherIdStr = fields[0];
        String question = fields[1];
        if (question == null || question.isBlank()) {
            ResponseUtil.sendError(exchange, 400, "question is required");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps;
            if (teacherIdStr != null && !teacherIdStr.isBlank()) {
                int teacherId = Integer.parseInt(teacherIdStr);
                ps = conn.prepareStatement(
                        "INSERT INTO doubts (student_id, teacher_id, question) VALUES (?, ?, ?) RETURNING doubt_id");
                ps.setInt(1, studentId);
                ps.setInt(2, teacherId);
                ps.setString(3, question);
            } else {
                ps = conn.prepareStatement(
                        "INSERT INTO doubts (student_id, question) VALUES (?, ?) RETURNING doubt_id");
                ps.setInt(1, studentId);
                ps.setString(2, question);
            }
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int doubtId = keys.next() ? keys.getInt("doubt_id") : -1;
            keys.close();
            ps.close();
            AuthController.logActivity(conn, userId, "DOUBT_CREATED", "doubts", doubtId);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Doubt raised successfully\",\"doubt_id\":" + doubtId + "}");
        } catch (SQLException e) {
            System.err.println("[StudentController] RaiseDoubt error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to raise doubt");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleGetDoubts(HttpExchange exchange, int studentId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT doubt_id, teacher_id, question, answer, status, created_at " +
                    "FROM doubts WHERE student_id = ? ORDER BY created_at DESC");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"doubt_id\":").append(rs.getInt("doubt_id")).append(",");
                json.append("\"teacher_id\":").append(rs.getInt("teacher_id")).append(",");
                json.append("\"question\":\"").append(escapeJson(rs.getString("question"))).append("\",");
                json.append("\"answer\":").append(rs.getString("answer") == null ? "null" : "\"" + escapeJson(rs.getString("answer")) + "\"").append(",");
                json.append("\"status\":\"").append(rs.getString("status")).append("\",");
                json.append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"");
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[StudentController] GetDoubts error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve doubts");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleGetActivity(HttpExchange exchange, int userId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_user_activity(?)");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"log_id\":").append(rs.getInt("log_id")).append(",");
                json.append("\"action\":\"").append(rs.getString("action")).append("\",");
                json.append("\"entity_type\":\"").append(rs.getString("entity_type")).append("\",");
                json.append("\"entity_id\":").append(rs.getInt("entity_id")).append(",");
                json.append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"");
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[StudentController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve activity");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static String escapeJson(String input) {
        if (input == null) return "null";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
