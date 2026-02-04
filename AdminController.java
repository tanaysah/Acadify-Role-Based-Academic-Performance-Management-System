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
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/create-teacher":
                if (method.equals("POST")) handleCreateTeacher(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/create-subject":
                if (method.equals("POST")) handleCreateSubject(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/assign-teacher":
                if (method.equals("POST")) handleAssignTeacher(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/top-performers":
                if (method.equals("GET")) handleTopPerformers(exchange);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/lowest-performers":
                if (method.equals("GET")) handleLowestPerformers(exchange);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/backlogs":
                if (method.equals("GET")) handleDetectBacklogs(exchange);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/high-risk":
                if (method.equals("GET")) handleHighRiskStudents(exchange);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/stream-performance":
                if (method.equals("GET")) handleStreamPerformance(exchange);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/active-term":
                if (method.equals("GET")) handleGetActiveTerm(exchange);
                else if (method.equals("POST")) handleSetActiveTerm(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            case "/admin/activity":
                if (method.equals("GET")) handleGetActivity(exchange, userId);
                else ResponseUtil.sendMethodNotAllowed(exchange);
                break;
            default:
                ResponseUtil.sendNotFound(exchange, "Endpoint not found");
        }
    }

    private static void handleCreateStudent(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password", "name", "age", "roll_number", "stream");
        String email = fields[0], password = fields[1], name = fields[2];
        String ageStr = fields[3], rollNumber = fields[4], stream = fields[5];

        if (email == null || password == null || name == null || ageStr == null || rollNumber == null || stream == null) {
            ResponseUtil.sendBadRequest(exchange, "Email, password, name, age, roll number, and stream are required");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Age must be a positive integer");
            return;
        }

        if (age <= 0) {
            ResponseUtil.sendBadRequest(exchange, "Age must be a positive integer");
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

            String data = JsonBuilder.object()
                    .add("user_id", newUserId)
                    .add("student_id", studentId)
                    .build();
            
            ResponseUtil.sendCreated(exchange, "Student created successfully", data);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendConflict(exchange, "Email or roll number already exists");
            } else {
                System.err.println("[AdminController] CreateStudent error: " + e.getMessage());
                ResponseUtil.sendServerError(exchange, "Failed to create student");
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleCreateTeacher(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "email", "password", "name", "department", "designation");
        String email = fields[0], password = fields[1], name = fields[2];
        String department = fields[3], designation = fields[4];

        if (email == null || password == null || name == null || department == null || designation == null) {
            ResponseUtil.sendBadRequest(exchange, "Email, password, name, department, and designation are required");
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

            String data = JsonBuilder.object()
                    .add("user_id", newUserId)
                    .add("teacher_id", teacherId)
                    .build();
            
            ResponseUtil.sendCreated(exchange, "Teacher created successfully", data);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                ResponseUtil.sendConflict(exchange, "Email already exists");
            } else {
                System.err.println("[AdminController] CreateTeacher error: " + e.getMessage());
                ResponseUtil.sendServerError(exchange, "Failed to create teacher");
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleCreateSubject(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "subject_name", "semester");
        String subjectName = fields[0], semesterStr = fields[1];

        if (subjectName == null || subjectName.isBlank() || semesterStr == null) {
            ResponseUtil.sendBadRequest(exchange, "Subject name and semester are required");
            return;
        }

        int semester;
        try {
            semester = Integer.parseInt(semesterStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Semester must be a positive integer");
            return;
        }

        if (semester <= 0) {
            ResponseUtil.sendBadRequest(exchange, "Semester must be a positive integer");
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

            String data = JsonBuilder.object()
                    .add("subject_id", subjectId)
                    .build();
            
            ResponseUtil.sendCreated(exchange, "Subject created successfully", data);
        } catch (SQLException e) {
            System.err.println("[AdminController] CreateSubject error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to create subject");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleAssignTeacher(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "teacher_id", "subject_id");
        String teacherIdStr = fields[0], subjectIdStr = fields[1];

        if (teacherIdStr == null || subjectIdStr == null) {
            ResponseUtil.sendBadRequest(exchange, "Teacher ID and subject ID are required");
            return;
        }

        int teacherId, subjectId;
        try {
            teacherId = Integer.parseInt(teacherIdStr);
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Invalid teacher ID or subject ID");
            return;
        }

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
                ResponseUtil.sendNotFound(exchange, "Subject not found");
                return;
            }

            Integer currentTeacher = checkRs.getInt("teacher_id");
            if (checkRs.wasNull()) currentTeacher = null;
            checkRs.close();
            check.close();

            if (currentTeacher != null && currentTeacher == teacherId) {
                ResponseUtil.sendConflict(exchange, "Teacher is already assigned to this subject");
                return;
            }

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE subjects SET teacher_id = ? WHERE subject_id = ?");
            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            ps.executeUpdate();
            ps.close();

            AuthController.logActivity(conn, adminUserId, "TEACHER_ASSIGNED", "subjects", subjectId);
            ResponseUtil.sendSuccess(exchange, "Teacher assigned successfully");
        } catch (SQLException e) {
            System.err.println("[AdminController] AssignTeacher error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to assign teacher");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleTopPerformers(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String limitStr = RequestUtil.extractQueryParam(query, "limit");
        int limit = (limitStr != null) ? Integer.parseInt(limitStr) : 10;

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_top_performers(?)");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String performer = JsonBuilder.object()
                        .add("student_id", rs.getInt("student_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("subject_name", rs.getString("subject_name"))
                        .add("marks_obtained", rs.getBigDecimal("marks_obtained"))
                        .add("semester", rs.getInt("semester"))
                        .build();
                array.add(performer);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Top performers retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[AdminController] TopPerformers error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve top performers");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleLowestPerformers(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String limitStr = RequestUtil.extractQueryParam(query, "limit");
        int limit = (limitStr != null) ? Integer.parseInt(limitStr) : 10;

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_lowest_performers(?)");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String performer = JsonBuilder.object()
                        .add("student_id", rs.getInt("student_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("subject_name", rs.getString("subject_name"))
                        .add("marks_obtained", rs.getBigDecimal("marks_obtained"))
                        .add("semester", rs.getInt("semester"))
                        .build();
                array.add(performer);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Lowest performers retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[AdminController] LowestPerformers error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve lowest performers");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleDetectBacklogs(HttpExchange exchange) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM detect_backlogs()");
            ResultSet rs = ps.executeQuery();

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String backlog = JsonBuilder.object()
                        .add("student_id", rs.getInt("student_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("stream", rs.getString("stream"))
                        .add("backlog_count", rs.getInt("backlog_count"))
                        .build();
                array.add(backlog);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Backlogs retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[AdminController] DetectBacklogs error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to detect backlogs");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleHighRiskStudents(HttpExchange exchange) throws IOException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM get_high_risk_students()");
            ResultSet rs = ps.executeQuery();

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String student = JsonBuilder.object()
                        .add("student_id", rs.getInt("student_id"))
                        .add("student_name", rs.getString("student_name"))
                        .add("roll_number", rs.getString("roll_number"))
                        .add("stream", rs.getString("stream"))
                        .add("cgpa", rs.getBigDecimal("cgpa"))
                        .add("backlog_count", rs.getInt("backlog_count"))
                        .add("risk_score", rs.getBigDecimal("risk_score"))
                        .build();
                array.add(student);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "High risk students retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[AdminController] HighRiskStudents error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve high risk students");
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

            JsonBuilder.JsonArrayBuilder array = JsonBuilder.array();
            while (rs.next()) {
                String stream = JsonBuilder.object()
                        .add("stream", rs.getString("stream"))
                        .add("total_students", rs.getInt("total_students"))
                        .add("average_cgpa", rs.getBigDecimal("average_cgpa"))
                        .add("highest_cgpa", rs.getBigDecimal("highest_cgpa"))
                        .add("lowest_cgpa", rs.getBigDecimal("lowest_cgpa"))
                        .build();
                array.add(stream);
            }
            
            rs.close();
            ps.close();
            ResponseUtil.sendSuccess(exchange, "Stream performance retrieved successfully", array.build());
        } catch (SQLException e) {
            System.err.println("[AdminController] StreamPerformance error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve stream performance");
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
                String data = JsonBuilder.object()
                        .add("term_id", rs.getInt("term_id"))
                        .add("academic_year", rs.getString("academic_year"))
                        .add("semester", rs.getInt("semester"))
                        .build();
                
                rs.close();
                ps.close();
                ResponseUtil.sendSuccess(exchange, "Active term retrieved successfully", data);
            } else {
                rs.close();
                ps.close();
                ResponseUtil.sendSuccess(exchange, "No active term set");
            }
        } catch (SQLException e) {
            System.err.println("[AdminController] GetActiveTerm error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve active term");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }

    private static void handleSetActiveTerm(HttpExchange exchange, int adminUserId) throws IOException {
        String body = RequestUtil.readBody(exchange);
        String[] fields = RequestUtil.parseJson(body, "term_id");
        String termIdStr = fields[0];

        if (termIdStr == null) {
            ResponseUtil.sendBadRequest(exchange, "Term ID is required");
            return;
        }

        int termId;
        try {
            termId = Integer.parseInt(termIdStr);
        } catch (NumberFormatException e) {
            ResponseUtil.sendBadRequest(exchange, "Invalid term ID");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            CallableStatement cs = conn.prepareCall("{ CALL set_active_term(?) }");
            cs.setInt(1, termId);
            cs.execute();
            cs.close();

            AuthController.logActivity(conn, adminUserId, "ACTIVE_TERM_SET", "academic_terms", termId);
            ResponseUtil.sendSuccess(exchange, "Active term updated successfully");
        } catch (SQLException e) {
            System.err.println("[AdminController] SetActiveTerm error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to set active term");
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
            System.err.println("[AdminController] GetActivity error: " + e.getMessage());
            ResponseUtil.sendServerError(exchange, "Failed to retrieve activity");
        } finally {
            DatabaseConfig.releaseConnection(conn);
        }
    }
}
