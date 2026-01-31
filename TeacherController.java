package com.acadify;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;
public class TeacherController {
    public static void handle(HttpExchange exchange, int userId, int teacherId) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        switch (path) {
            case "/teacher/doubts":
                if (method.equals("GET")) handleGetPendingDoubts(exchange, teacherId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/teacher/doubt/answer":
                if (method.equals("POST")) handleAnswerDoubt(exchange, teacherId, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/teacher/marks":
                if (method.equals("POST")) handleAddMarks(exchange, teacherId, userId);
                else if (method.equals("PUT")) handleUpdateMarks(exchange, teacherId, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/teacher/class-performance":
                if (method.equals("GET")) handleClassPerformance(exchange, teacherId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/teacher/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            default:
                ResponseUtil.sendError(exchange, 404, "Endpoint not found");
        }
    }
    private static void handleGetPendingDoubts(HttpExchange exchange, int teacherId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_pending_doubts_by_teacher(?)");
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"doubt_id\":").append(rs.getInt("doubt_id")).append(",");
                json.append("\"student_name\":\"").append(rs.getString("student_name")).append("\",");
                json.append("\"roll_number\":\"").append(rs.getString("roll_number")).append("\",");
                json.append("\"question\":\"").append(escapeJson(rs.getString("question"))).append("\",");
                json.append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"");
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[TeacherController] GetPendingDoubts error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve doubts");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleAnswerDoubt(HttpExchange exchange, int teacherId, int userId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "doubt_id", "answer");
        String doubtIdStr = fields[0];
        String answer = fields[1];
        if (doubtIdStr == null || answer == null || answer.isBlank()) {
            ResponseUtil.sendError(exchange, 400, "doubt_id and answer are required");
            return;
        }
        int doubtId = Integer.parseInt(doubtIdStr);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement check = conn.prepareStatement(
                    "SELECT teacher_id FROM doubts WHERE doubt_id = ? AND status = 'PENDING'");
            check.setInt(1, doubtId);
            ResultSet checkRs = check.executeQuery();
            if (!checkRs.next()) {
                checkRs.close();
                check.close();
                ResponseUtil.sendError(exchange, 404, "Doubt not found or already answered");
                return;
            }
            int assignedTeacherId = checkRs.getInt("teacher_id");
            checkRs.close();
            check.close();
            if (assignedTeacherId != teacherId) {
                ResponseUtil.sendError(exchange, 403, "This doubt is not assigned to you");
                return;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE doubts SET answer = ?, status = 'ANSWERED' WHERE doubt_id = ?");
            ps.setString(1, answer);
            ps.setInt(2, doubtId);
            ps.executeUpdate();
            ps.close();
            AuthController.logActivity(conn, userId, "DOUBT_ANSWERED", "doubts", doubtId);
            ResponseUtil.sendJson(exchange, 200, "{\"message\":\"Doubt answered successfully\"}");
        } catch (SQLException e) {
            System.err.println("[TeacherController] AnswerDoubt error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to answer doubt");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleAddMarks(HttpExchange exchange, int teacherId, int userId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "student_id", "subject_id", "marks_obtained", "semester");
        String studentIdStr = fields[0];
        String subjectIdStr = fields[1];
        String marksStr = fields[2];
        String semesterStr = fields[3];
        if (studentIdStr == null || subjectIdStr == null || marksStr == null || semesterStr == null) {
            ResponseUtil.sendError(exchange, 400, "student_id, subject_id, marks_obtained, and semester are required");
            return;
        }
        int studentId, subjectId, semester;
        double marksObtained;
        try {
            studentId = Integer.parseInt(studentIdStr);
            subjectId = Integer.parseInt(subjectIdStr);
            marksObtained = Double.parseDouble(marksStr);
            semester = Integer.parseInt(semesterStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendError(exchange, 400, "student_id, subject_id, and semester must be integers; marks_obtained must be a number");
            return;
        }
        if (marksObtained < 0 || marksObtained > 100) {
            ResponseUtil.sendError(exchange, 400, "marks_obtained must be between 0 and 100");
            return;
        }
        if (semester <= 0) {
            ResponseUtil.sendError(exchange, 400, "semester must be a positive integer");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendError(exchange, 403, "You are not assigned to this subject");
                return;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO marks (student_id, subject_id, marks_obtained, semester) VALUES (?, ?, ?, ?) RETURNING mark_id");
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setDouble(3, marksObtained);
            ps.setInt(4, semester);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int markId = keys.next() ? keys.getInt("mark_id") : -1;
            keys.close();
            ps.close();
            AuthController.logActivity(conn, userId, "MARKS_ADDED", "marks", markId);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Marks added successfully\",\"mark_id\":" + markId + "}");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendError(exchange, 409, "Marks already exist for this student, subject, and semester");
            } else {
                System.err.println("[TeacherController] AddMarks error: " + e.getMessage());
                ResponseUtil.sendError(exchange, 500, "Failed to add marks");
            }
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleUpdateMarks(HttpExchange exchange, int teacherId, int userId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "mark_id", "marks_obtained");
        String markIdStr = fields[0];
        String marksStr = fields[1];
        if (markIdStr == null || marksStr == null) {
            ResponseUtil.sendError(exchange, 400, "mark_id and marks_obtained are required");
            return;
        }
        int markId;
        double marksObtained;
        try {
            markId = Integer.parseInt(markIdStr);
            marksObtained = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendError(exchange, 400, "mark_id must be an integer; marks_obtained must be a number");
            return;
        }
        if (marksObtained < 0 || marksObtained > 100) {
            ResponseUtil.sendError(exchange, 400, "marks_obtained must be between 0 and 100");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement check = conn.prepareStatement(
                    "SELECT m.subject_id FROM marks m WHERE m.mark_id = ?");
            check.setInt(1, markId);
            ResultSet checkRs = check.executeQuery();
            if (!checkRs.next()) {
                checkRs.close();
                check.close();
                ResponseUtil.sendError(exchange, 404, "Mark record not found");
                return;
            }
            int subjectId = checkRs.getInt("subject_id");
            checkRs.close();
            check.close();
            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendError(exchange, 403, "You are not assigned to this subject");
                return;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE marks SET marks_obtained = ? WHERE mark_id = ?");
            ps.setDouble(1, marksObtained);
            ps.setInt(2, markId);
            ps.executeUpdate();
            ps.close();
            AuthController.logActivity(conn, userId, "MARKS_UPDATED", "marks", markId);
            ResponseUtil.sendJson(exchange, 200, "{\"message\":\"Marks updated successfully\"}");
        } catch (SQLException e) {
            System.err.println("[TeacherController] UpdateMarks error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to update marks");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleClassPerformance(HttpExchange exchange, int teacherId) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String subjectIdStr = RequestUtil.extractQueryParam(query, "subject_id");
        String semesterStr = RequestUtil.extractQueryParam(query, "semester");
        if (subjectIdStr == null) {
            ResponseUtil.sendError(exchange, 400, "subject_id query parameter is required");
            return;
        }
        int subjectId = Integer.parseInt(subjectIdStr);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendError(exchange, 403, "You are not assigned to this subject");
                return;
            }
            PreparedStatement ps;
            if (semesterStr != null) {
                ps = conn.prepareStatement("SELECT * FROM get_class_performance_by_subject(?, ?)");
                ps.setInt(1, subjectId);
                ps.setInt(2, Integer.parseInt(semesterStr));
            } else {
                ps = conn.prepareStatement("SELECT * FROM get_class_performance_by_subject(?)");
                ps.setInt(1, subjectId);
            }
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"subject_id\":").append(rs.getInt("subject_id")).append(",");
                json.append("\"subject_name\":\"").append(rs.getString("subject_name")).append("\",");
                json.append("\"semester\":").append(rs.getInt("semester")).append(",");
                json.append("\"total_students\":").append(rs.getInt("total_students")).append(",");
                json.append("\"average_marks\":").append(rs.getBigDecimal("average_marks")).append(",");
                json.append("\"highest_marks\":").append(rs.getBigDecimal("highest_marks")).append(",");
                json.append("\"lowest_marks\":").append(rs.getBigDecimal("lowest_marks")).append(",");
                json.append("\"pass_count\":").append(rs.getInt("pass_count")).append(",");
                json.append("\"fail_count\":").append(rs.getInt("fail_count"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[TeacherController] ClassPerformance error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve class performance");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleGetActivity(HttpExchange exchange, int userId) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM get_user_activity(?)");
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
            System.err.println("[TeacherController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve activity");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static boolean isTeacherAssigned(Connection conn, int teacherId, int subjectId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM subjects WHERE subject_id = ? AND teacher_id = ?");
        ps.setInt(1, subjectId);
        ps.setInt(2, teacherId);
        ResultSet rs = ps.executeQuery();
        boolean assigned = rs.next();
        rs.close();
        ps.close();
        return assigned;
    }
    private static String escapeJson(String input) {
        if (input == null) return "null";
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
