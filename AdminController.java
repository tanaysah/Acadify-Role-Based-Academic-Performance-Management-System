package com.acadify;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;
public class AdminController {
    public static void handle(HttpExchange exchange, int userId) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        switch (path) {
            case "/admin/create-student":
                if (method.equals("POST")) handleCreateStudent(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/create-teacher":
                if (method.equals("POST")) handleCreateTeacher(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/create-subject":
                if (method.equals("POST")) handleCreateSubject(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/assign-teacher":
                if (method.equals("POST")) handleAssignTeacher(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/top-performers":
                if (method.equals("GET")) handleTopPerformers(exchange);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/lowest-performers":
                if (method.equals("GET")) handleLowestPerformers(exchange);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/backlogs":
                if (method.equals("GET")) handleDetectBacklogs(exchange);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/high-risk":
                if (method.equals("GET")) handleHighRiskStudents(exchange);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/stream-performance":
                if (method.equals("GET")) handleStreamPerformance(exchange);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/active-term":
                if (method.equals("GET")) handleGetActiveTerm(exchange);
                else if (method.equals("POST")) handleSetActiveTerm(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            case "/admin/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendError(exchange, 405, "Method not allowed");
                break;
            default:
                ResponseUtil.sendError(exchange, 404, "Endpoint not found");
        }
    }
    private static void handleCreateStudent(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password", "name", "age", "roll_number", "stream");
        String email = fields[0], password = fields[1], name = fields[2];
        String ageStr = fields[3], rollNumber = fields[4], stream = fields[5];
        if (email == null || password == null || name == null || ageStr == null || rollNumber == null || stream == null) {
            ResponseUtil.sendError(exchange, 400, "email, password, name, age, roll_number, and stream are required");
            return;
        }
        int age;
        try { age = Integer.parseInt(ageStr); }
        catch (NumberFormatException e) {
            ResponseUtil.sendError(exchange, 400, "age must be a positive integer");
            return;
        }
        if (age <= 0) {
            ResponseUtil.sendError(exchange, 400, "age must be a positive integer");
            return;
        }
        String hashedPassword = AuthController.hashPassword(password);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT INTO users (email, password, role) VALUES (?, ?, 'STUDENT') RETURNING user_id");
            insertUser.setString(1, email);
            insertUser.setString(2, hashedPassword);
            insertUser.executeUpdate();
            int newUserId = insertUser.getGeneratedKeys().getInt("user_id");
            insertUser.close();
            PreparedStatement insertStudent = conn.prepareStatement(
                    "INSERT INTO students (user_id, name, age, roll_number, stream) VALUES (?, ?, ?, ?, ?) RETURNING student_id");
            insertStudent.setInt(1, newUserId);
            insertStudent.setString(2, name);
            insertStudent.setInt(3, age);
            insertStudent.setString(4, rollNumber);
            insertStudent.setString(5, stream);
            insertStudent.executeUpdate();
            int studentId = insertStudent.getGeneratedKeys().getInt("student_id");
            insertStudent.close();
            AuthController.logActivity(conn, adminUserId, "STUDENT_CREATED", "students", studentId);
            conn.commit();
            conn.setAutoCommit(true);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Student created\",\"user_id\":" + newUserId + ",\"student_id\":" + studentId + "}");
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendError(exchange, 409, "Email or roll number already exists");
            } else {
                System.err.println("[AdminController] CreateStudent error: " + e.getMessage());
                ResponseUtil.sendError(exchange, 500, "Failed to create student");
            }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ignored) {} }
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleCreateTeacher(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password", "name", "department", "designation");
        String email = fields[0], password = fields[1], name = fields[2];
        String department = fields[3], designation = fields[4];
        if (email == null || password == null || name == null || department == null || designation == null) {
            ResponseUtil.sendError(exchange, 400, "email, password, name, department, and designation are required");
            return;
        }
        String hashedPassword = AuthController.hashPassword(password);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement insertUser = conn.prepareStatement(
                    "INSERT INTO users (email, password, role) VALUES (?, ?, 'TEACHER') RETURNING user_id");
            insertUser.setString(1, email);
            insertUser.setString(2, hashedPassword);
            insertUser.executeUpdate();
            int newUserId = insertUser.getGeneratedKeys().getInt("user_id");
            insertUser.close();
            PreparedStatement insertTeacher = conn.prepareStatement(
                    "INSERT INTO teachers (user_id, name, department, designation) VALUES (?, ?, ?, ?) RETURNING teacher_id");
            insertTeacher.setInt(1, newUserId);
            insertTeacher.setString(2, name);
            insertTeacher.setString(3, department);
            insertTeacher.setString(4, designation);
            insertTeacher.executeUpdate();
            int teacherId = insertTeacher.getGeneratedKeys().getInt("teacher_id");
            insertTeacher.close();
            AuthController.logActivity(conn, adminUserId, "TEACHER_CREATED", "teachers", teacherId);
            conn.commit();
            conn.setAutoCommit(true);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Teacher created\",\"user_id\":" + newUserId + ",\"teacher_id\":" + teacherId + "}");
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendError(exchange, 409, "Email already exists");
            } else {
                System.err.println("[AdminController] CreateTeacher error: " + e.getMessage());
                ResponseUtil.sendError(exchange, 500, "Failed to create teacher");
            }
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ignored) {} }
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleCreateSubject(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "subject_name", "semester");
        String subjectName = fields[0], semesterStr = fields[1];
        if (subjectName == null || subjectName.isBlank() || semesterStr == null) {
            ResponseUtil.sendError(exchange, 400, "subject_name and semester are required");
            return;
        }
        int semester;
        try { semester = Integer.parseInt(semesterStr); }
        catch (NumberFormatException e) {
            ResponseUtil.sendError(exchange, 400, "semester must be a positive integer");
            return;
        }
        if (semester <= 0) {
            ResponseUtil.sendError(exchange, 400, "semester must be a positive integer");
            return;
        }
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO subjects (subject_name, semester) VALUES (?, ?) RETURNING subject_id");
            ps.setString(1, subjectName);
            ps.setInt(2, semester);
            ps.executeUpdate();
            int subjectId = ps.getGeneratedKeys().getInt("subject_id");
            ps.close();
            AuthController.logActivity(conn, adminUserId, "SUBJECT_CREATED", "subjects", subjectId);
            ResponseUtil.sendJson(exchange, 201,
                    "{\"message\":\"Subject created\",\"subject_id\":" + subjectId + "}");
        } catch (SQLException e) {
            System.err.println("[AdminController] CreateSubject error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to create subject");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleAssignTeacher(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "teacher_id", "subject_id");
        String teacherIdStr = fields[0], subjectIdStr = fields[1];
        if (teacherIdStr == null || subjectIdStr == null) {
            ResponseUtil.sendError(exchange, 400, "teacher_id and subject_id are required");
            return;
        }
        int teacherId = Integer.parseInt(teacherIdStr);
        int subjectId = Integer.parseInt(subjectIdStr);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement check = conn.prepareStatement(
                    "SELECT teacher_id FROM subjects WHERE subject_id = ?");
            check.setInt(1, subjectId);
            ResultSet checkRs = check.executeQuery();
            if (!checkRs.next()) {
                checkRs.close();
                check.close();
                ResponseUtil.sendError(exchange, 404, "Subject not found");
                return;
            }
            Integer currentTeacher = checkRs.getInt("teacher_id");
            if (checkRs.wasNull()) currentTeacher = null;
            checkRs.close();
            check.close();
            if (currentTeacher != null && currentTeacher == teacherId) {
                ResponseUtil.sendError(exchange, 409, "Teacher is already assigned to this subject");
                return;
            }
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE subjects SET teacher_id = ? WHERE subject_id = ?");
            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            ps.executeUpdate();
            ps.close();
            AuthController.logActivity(conn, adminUserId, "TEACHER_ASSIGNED", "subjects", subjectId);
            ResponseUtil.sendJson(exchange, 200, "{\"message\":\"Teacher assigned successfully\"}");
        } catch (SQLException e) {
            System.err.println("[AdminController] AssignTeacher error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to assign teacher");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleTopPerformers(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String subjectIdStr = RequestUtil.extractQueryParam(query, "subject_id");
        String semesterStr = RequestUtil.extractQueryParam(query, "semester");
        String limitStr = RequestUtil.extractQueryParam(query, "limit");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM get_top_performers(";
            sql += (subjectIdStr != null ? "?" : "NULL") + ", ";
            sql += (semesterStr != null ? "?" : "NULL") + ", ";
            sql += (limitStr != null ? "?" : "10") + ")";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            if (subjectIdStr != null) ps.setInt(idx++, Integer.parseInt(subjectIdStr));
            if (semesterStr != null) ps.setInt(idx++, Integer.parseInt(semesterStr));
            if (limitStr != null) ps.setInt(idx, Integer.parseInt(limitStr));
            ResultSet rs = ps.executeQuery();
            ResponseUtil.sendJson(exchange, 200, buildPerformersJson(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("[AdminController] TopPerformers error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve top performers");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleLowestPerformers(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String subjectIdStr = RequestUtil.extractQueryParam(query, "subject_id");
        String semesterStr = RequestUtil.extractQueryParam(query, "semester");
        String limitStr = RequestUtil.extractQueryParam(query, "limit");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM get_lowest_performers(";
            sql += (subjectIdStr != null ? "?" : "NULL") + ", ";
            sql += (semesterStr != null ? "?" : "NULL") + ", ";
            sql += (limitStr != null ? "?" : "10") + ")";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            if (subjectIdStr != null) ps.setInt(idx++, Integer.parseInt(subjectIdStr));
            if (semesterStr != null) ps.setInt(idx++, Integer.parseInt(semesterStr));
            if (limitStr != null) ps.setInt(idx, Integer.parseInt(limitStr));
            ResultSet rs = ps.executeQuery();
            ResponseUtil.sendJson(exchange, 200, buildPerformersJson(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println("[AdminController] LowestPerformers error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve lowest performers");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleDetectBacklogs(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String semesterStr = RequestUtil.extractQueryParam(query, "semester");
        String passingStr = RequestUtil.extractQueryParam(query, "passing_marks");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT * FROM detect_backlogs(" +
                    (semesterStr != null ? "?" : "NULL") + ", " +
                    (passingStr != null ? "?" : "40.0") + ")";
            PreparedStatement ps = conn.prepareStatement(sql);
            int idx = 1;
            if (semesterStr != null) ps.setInt(idx++, Integer.parseInt(semesterStr));
            if (passingStr != null) ps.setDouble(idx, Double.parseDouble(passingStr));
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"student_id\":").append(rs.getInt("student_id")).append(",");
                json.append("\"student_name\":\"").append(rs.getString("student_name")).append("\",");
                json.append("\"roll_number\":\"").append(rs.getString("roll_number")).append("\",");
                json.append("\"subject_id\":").append(rs.getInt("subject_id")).append(",");
                json.append("\"subject_name\":\"").append(rs.getString("subject_name")).append("\",");
                json.append("\"marks_obtained\":").append(rs.getBigDecimal("marks_obtained")).append(",");
                json.append("\"semester\":").append(rs.getInt("semester"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[AdminController] DetectBacklogs error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to detect backlogs");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleHighRiskStudents(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String thresholdStr = RequestUtil.extractQueryParam(query, "threshold");
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            String sql = thresholdStr != null
                    ? "SELECT * FROM get_high_risk_students(?)"
                    : "SELECT * FROM get_high_risk_students()";
            PreparedStatement ps = conn.prepareStatement(sql);
            if (thresholdStr != null) ps.setDouble(1, Double.parseDouble(thresholdStr));
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"student_id\":").append(rs.getInt("student_id")).append(",");
                json.append("\"student_name\":\"").append(rs.getString("student_name")).append("\",");
                json.append("\"roll_number\":\"").append(rs.getString("roll_number")).append("\",");
                json.append("\"stream\":\"").append(rs.getString("stream")).append("\",");
                json.append("\"cgpa\":").append(rs.getBigDecimal("cgpa")).append(",");
                json.append("\"backlog_count\":").append(rs.getInt("backlog_count")).append(",");
                json.append("\"risk_score\":").append(rs.getBigDecimal("risk_score"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[AdminController] HighRiskStudents error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve high risk students");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleStreamPerformance(HttpExchange exchange) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM get_stream_performance()");
            ResultSet rs = ps.executeQuery();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{");
                json.append("\"stream\":\"").append(rs.getString("stream")).append("\",");
                json.append("\"total_students\":").append(rs.getInt("total_students")).append(",");
                json.append("\"average_cgpa\":").append(rs.getBigDecimal("average_cgpa")).append(",");
                json.append("\"highest_cgpa\":").append(rs.getBigDecimal("highest_cgpa")).append(",");
                json.append("\"lowest_cgpa\":").append(rs.getBigDecimal("lowest_cgpa"));
                json.append("}");
                first = false;
            }
            json.append("]");
            rs.close();
            ps.close();
            ResponseUtil.sendJson(exchange, 200, json.toString());
        } catch (SQLException e) {
            System.err.println("[AdminController] StreamPerformance error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve stream performance");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleGetActiveTerm(HttpExchange exchange) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM get_active_term()");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String json = "{" +
                        "\"term_id\":" + rs.getInt("term_id") + "," +
                        "\"academic_year\":\"" + rs.getString("academic_year") + "\"," +
                        "\"semester\":" + rs.getInt("semester") +
                        "}";
                rs.close();
                ps.close();
                ResponseUtil.sendJson(exchange, 200, json);
            } else {
                rs.close();
                ps.close();
                ResponseUtil.sendJson(exchange, 200, "{\"message\":\"No active term set\"}");
            }
        } catch (SQLException e) {
            System.err.println("[AdminController] GetActiveTerm error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve active term");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static void handleSetActiveTerm(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "term_id");
        String termIdStr = fields[0];
        if (termIdStr == null) {
            ResponseUtil.sendError(exchange, 400, "term_id is required");
            return;
        }
        int termId = Integer.parseInt(termIdStr);
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            CallableStatement cs = conn.prepareCall("{ CALL set_active_term(?) }");
            cs.setInt(1, termId);
            cs.execute();
            cs.close();
            AuthController.logActivity(conn, adminUserId, "ACTIVE_TERM_SET", "academic_terms", termId);
            ResponseUtil.sendJson(exchange, 200, "{\"message\":\"Active term updated\"}");
        } catch (SQLException e) {
            System.err.println("[AdminController] SetActiveTerm error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to set active term");
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
            System.err.println("[AdminController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendError(exchange, 500, "Failed to retrieve activity");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
    private static String buildPerformersJson(ResultSet rs) throws SQLException {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        while (rs.next()) {
            if (!first) json.append(",");
            json.append("{");
            json.append("\"student_id\":").append(rs.getInt("student_id")).append(",");
            json.append("\"student_name\":\"").append(rs.getString("student_name")).append("\",");
            json.append("\"roll_number\":\"").append(rs.getString("roll_number")).append("\",");
            json.append("\"subject_name\":\"").append(rs.getString("subject_name")).append("\",");
            json.append("\"marks_obtained\":").append(rs.getBigDecimal("marks_obtained")).append(",");
            json.append("\"semester\":").append(rs.getInt("semester"));
            json.append("}");
            first = false;
        }
        json.append("]");
        return json.toString();
    }
}
