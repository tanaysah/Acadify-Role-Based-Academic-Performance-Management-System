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
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/teacher/doubt/answer":
                if (method.equals("POST")) handleAnswerDoubt(exchange, teacherId, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/teacher/marks":
                if (method.equals("POST")) handleAddMarks(exchange, teacherId, userId);
                else if (method.equals("PUT")) handleUpdateMarks(exchange, teacherId, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/teacher/class-performance":
                if (method.equals("GET")) handleClassPerformance(exchange, teacherId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/teacher/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            default:
                ResponseUtil.sendNotFound(exchange, "Endpoint not found");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String doubt = JsonBuilder.object()
                        .add("doubt_id", rs.getInt("doubt_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("question", rs.getString("question"))
                        .add("created_at", rs.getTimestamp("created_at"))
                        .build();
                array.add(doubt);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Pending doubts retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[TeacherController] GetPendingDoubts error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve doubts");
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
            ResponseUtil.sendBadRequest(exchange, "Doubt ID and answer are required");
            return;
        }

        int doubtId;
        try {
            doubtId = Integer.parseInt(doubtIdStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Invalid doubt ID");
            return;
        }

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
                ResponseUtil.sendNotFound(exchange, "Doubt not found or already answered");
                return;
            }

            int assignedTeacherId = checkRs.getInt("teacher_id");
            checkRs.close();
            check.close();

            if (assignedTeacherId != teacherId) {
                ResponseUtil.sendForbidden(exchange, "This doubt is not assigned to you");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE doubts SET answer = ?, status = 'ANSWERED' WHERE doubt_id = ?");
            ps.setString(1, answer);
            ps.setInt(2, doubtId);
            ps.executeUpdate();
            ps.close();

            AuthController.logActivity(conn, userId, "DOUBT_ANSWERED", "doubts", doubtId);
            ResponseUtil.sendSuccess(exchange, "Doubt answered successfully");
        } catch (SQLException e) {
            System.err.println("[TeacherController] AnswerDoubt error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to answer doubt");
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
            ResponseUtil.sendBadRequest(exchange, "Student ID, subject ID, marks obtained, and semester are required");
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
            ResponseUtil.sendBadRequest(exchange, "Invalid number format for student ID, subject ID, marks, or semester");
            return;
        }

        if (marksObtained < 0 || marksObtained > 100) {
            ResponseUtil.sendBadRequest(exchange, "Marks must be between 0 and 100");
            return;
        }

        if (semester <= 0) {
            ResponseUtil.sendBadRequest(exchange, "Semester must be a positive integer");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            
            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendForbidden(exchange, "You are not assigned to this subject");
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

            String data = JsonBuilder.object()
                    .add("mark_id", markId)
                    .build();
            
            ResponseUtil.sendCreated(exchange, "Marks added successfully", data);
        } catch (SQLException e) {
            System.err.println("[TeacherController] AddMarks error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to add marks");
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
            ResponseUtil.sendBadRequest(exchange, "Mark ID and marks obtained are required");
            return;
        }

        int markId;
        double marksObtained;
        try {
            markId = Integer.parseInt(markIdStr);
            marksObtained = Double.parseDouble(marksStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Invalid number format for mark ID or marks");
            return;
        }

        if (marksObtained < 0 || marksObtained > 100) {
            ResponseUtil.sendBadRequest(exchange, "Marks must be between 0 and 100");
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
                ResponseUtil.sendNotFound(exchange, "Mark record not found");
                return;
            }
            
            int subjectId = checkRs.getInt("subject_id");
            checkRs.close();
            check.close();

            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendForbidden(exchange, "You are not assigned to this subject");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE marks SET marks_obtained = ? WHERE mark_id = ?");
            ps.setDouble(1, marksObtained);
            ps.setInt(2, markId);
            ps.executeUpdate();
            ps.close();

            AuthController.logActivity(conn, userId, "MARKS_UPDATED", "marks", markId);
            ResponseUtil.sendSuccess(exchange, "Marks updated successfully");
        } catch (SQLException e) {
            System.err.println("[TeacherController] UpdateMarks error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to update marks");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleClassPerformance(HttpExchange exchange, int teacherId) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String subjectIdStr = RequestUtil.extractQueryParam(query, "subject_id");
        String semesterStr = RequestUtil.extractQueryParam(query, "semester");

        if (subjectIdStr == null) {
            ResponseUtil.sendBadRequest(exchange, "Subject ID query parameter is required");
            return;
        }

        int subjectId;
        try {
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Invalid subject ID");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            
            if (!isTeacherAssigned(conn, teacherId, subjectId)) {
                ResponseUtil.sendForbidden(exchange, "You are not assigned to this subject");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String performance = JsonBuilder.object()
                        .add("subject_id", rs.getInt("subject_id"))
                        .add("subject_name", rs.getString("subject_name"))
                        .add("semester", rs.getInt("semester"))
                        .add("total_students", rs.getInt("total_students"))
                        .add("average_marks", rs.getBigDecimal("average_marks"))
                        .add("highest_marks", rs.getBigDecimal("highest_marks"))
                        .add("lowest_marks", rs.getBigDecimal("lowest_marks"))
                        .add("pass_count", rs.getInt("pass_count"))
                        .add("fail_count", rs.getInt("fail_count"))
                        .build();
                array.add(performance);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Class performance retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[TeacherController] ClassPerformance error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve class performance");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String activity = JsonBuilder.object()
                        .add("log_id", rs.getInt("log_id"))
                        .add("action", rs.getString("action"))
                        .add("entity_type", rs.getString("entity_type"))
                        .add("entity_id", rs.getInt("entity_id"))
                        .add("created_at", rs.getTimestamp("created_at"))
                        .build();
                array.add(activity);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Activity log retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[TeacherController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve activity");
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
}
