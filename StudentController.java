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
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/semester-performance":
                if (method.equals("GET")) handleSemesterPerformance(exchange, studentId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/marks-trend":
                if (method.equals("GET")) handleSubjectMarksTrend(exchange, studentId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/weak-subjects":
                if (method.equals("GET")) handleWeakSubjects(exchange, exchange.getRequestURI().getQuery(), studentId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/doubt":
                if (method.equals("POST")) handleRaiseDoubt(exchange, studentId, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/doubts":
                if (method.equals("GET")) handleGetDoubts(exchange, studentId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/student/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            default:
                ResponseUtil.sendNotFound(exchange, "Endpoint not found");
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
                String data = JsonBuilder.object()
                        .add("student_id", rs.getInt("student_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("stream", rs.getString("stream"))
                        .add("current_cgpa", rs.getBigDecimal("current_cgpa"))
                        .add("total_subjects", rs.getInt("total_subjects"))
                        .add("overall_average", rs.getBigDecimal("overall_average"))
                        .add("total_backlogs", rs.getInt("total_backlogs"))
                        .add("semesters_completed", rs.getInt("semesters_completed"))
                        .build();
                
                rs.close();
                ps.close();
                ResponseUtil.sendSuccess(exchange, "Performance report retrieved successfully", data);
            } else {
                rs.close();
                ps.close();
                ResponseUtil.sendNotFound(exchange, "No performance data found");
            }
        } catch (SQLException e) {
            System.err.println("[StudentController] PerformanceReport error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve report");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String semester = JsonBuilder.object()
                        .add("semester", rs.getInt("semester"))
                        .add("total_subjects", rs.getInt("total_subjects"))
                        .add("average_marks", rs.getBigDecimal("average_marks"))
                        .add("semester_gpa", rs.getBigDecimal("semester_gpa"))
                        .add("backlogs", rs.getInt("backlogs"))
                        .build();
                array.add(semester);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Semester performance retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[StudentController] SemesterPerformance error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve semester performance");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String trend = JsonBuilder.object()
                        .add("subject_id", rs.getInt("subject_id"))
                        .add("subject_name", rs.getString("subject_name"))
                        .add("semester", rs.getInt("semester"))
                        .add("marks_obtained", rs.getBigDecimal("marks_obtained"))
                        .build();
                array.add(trend);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Marks trend retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[StudentController] MarksTrend error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve marks trend");
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
            } catch (NumberFormatException e) {
                ResponseUtil.sendBadRequest(exchange, "Invalid threshold value");
                return;
            }
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_weak_subjects(?, ?)");
            ps.setInt(1, studentId);
            ps.setDouble(2, threshold);
            ResultSet rs = ps.executeQuery();

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String subject = JsonBuilder.object()
                        .add("subject_id", rs.getInt("subject_id"))
                        .add("subject_name", rs.getString("subject_name"))
                        .add("average_marks", rs.getBigDecimal("average_marks"))
                        .add("times_below_threshold", rs.getInt("times_below_threshold"))
                        .add("latest_semester", rs.getInt("latest_semester"))
                        .add("latest_marks", rs.getBigDecimal("latest_marks"))
                        .build();
                array.add(subject);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Weak subjects retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[StudentController] WeakSubjects error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve weak subjects");
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
            ResponseUtil.sendBadRequest(exchange, "Question is required");
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

            String data = JsonBuilder.object()
                    .add("doubt_id", doubtId)
                    .build();
            
            ResponseUtil.sendCreated(exchange, "Doubt raised successfully", data);
        } catch (SQLException e) {
            System.err.println("[StudentController] RaiseDoubt error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to raise doubt");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String doubt = JsonBuilder.object()
                        .add("doubt_id", rs.getInt("doubt_id"))
                        .add("teacher_id", rs.getInt("teacher_id"))
                        .add("question", rs.getString("question"))
                        .addNullable("answer", rs.getString("answer"))
                        .add("status", rs.getString("status"))
                        .add("created_at", rs.getTimestamp("created_at"))
                        .build();
                array.add(doubt);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Doubts retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[StudentController] GetDoubts error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve doubts");
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
            System.err.println("[StudentController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve activity");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
}
