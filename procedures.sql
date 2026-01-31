-- Acadify PL/SQL Procedures and Functions

-- 1. Fetch top performing students by marks
CREATE OR REPLACE FUNCTION get_top_performers(
    p_subject_id INTEGER DEFAULT NULL,
    p_semester INTEGER DEFAULT NULL,
    p_limit INTEGER DEFAULT 10
)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    subject_name VARCHAR,
    marks_obtained DECIMAL,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        sub.subject_name,
        m.marks_obtained,
        m.semester
    FROM marks m
    INNER JOIN students s ON m.student_id = s.student_id
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        (p_subject_id IS NULL OR m.subject_id = p_subject_id)
        AND (p_semester IS NULL OR m.semester = p_semester)
    ORDER BY m.marks_obtained DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 2. Fetch lowest performing students by marks
CREATE OR REPLACE FUNCTION get_lowest_performers(
    p_subject_id INTEGER DEFAULT NULL,
    p_semester INTEGER DEFAULT NULL,
    p_limit INTEGER DEFAULT 10
)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    subject_name VARCHAR,
    marks_obtained DECIMAL,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        sub.subject_name,
        m.marks_obtained,
        m.semester
    FROM marks m
    INNER JOIN students s ON m.student_id = s.student_id
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        (p_subject_id IS NULL OR m.subject_id = p_subject_id)
        AND (p_semester IS NULL OR m.semester = p_semester)
    ORDER BY m.marks_obtained ASC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 3. Sort marks high to low
CREATE OR REPLACE FUNCTION get_marks_high_to_low(
    p_subject_id INTEGER DEFAULT NULL,
    p_semester INTEGER DEFAULT NULL
)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    subject_name VARCHAR,
    marks_obtained DECIMAL,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        sub.subject_name,
        m.marks_obtained,
        m.semester
    FROM marks m
    INNER JOIN students s ON m.student_id = s.student_id
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        (p_subject_id IS NULL OR m.subject_id = p_subject_id)
        AND (p_semester IS NULL OR m.semester = p_semester)
    ORDER BY m.marks_obtained DESC;
END;
$$ LANGUAGE plpgsql;

-- 4. Sort marks low to high
CREATE OR REPLACE FUNCTION get_marks_low_to_high(
    p_subject_id INTEGER DEFAULT NULL,
    p_semester INTEGER DEFAULT NULL
)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    subject_name VARCHAR,
    marks_obtained DECIMAL,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        sub.subject_name,
        m.marks_obtained,
        m.semester
    FROM marks m
    INNER JOIN students s ON m.student_id = s.student_id
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        (p_subject_id IS NULL OR m.subject_id = p_subject_id)
        AND (p_semester IS NULL OR m.semester = p_semester)
    ORDER BY m.marks_obtained ASC;
END;
$$ LANGUAGE plpgsql;

-- 5. Backlog detection procedure (passing marks < 40)
CREATE OR REPLACE FUNCTION detect_backlogs(
    p_semester INTEGER DEFAULT NULL,
    p_passing_marks DECIMAL DEFAULT 40.0
)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    subject_id INTEGER,
    subject_name VARCHAR,
    marks_obtained DECIMAL,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        m.subject_id,
        sub.subject_name,
        m.marks_obtained,
        m.semester
    FROM marks m
    INNER JOIN students s ON m.student_id = s.student_id
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        m.marks_obtained < p_passing_marks
        AND (p_semester IS NULL OR m.semester = p_semester)
    ORDER BY m.semester, s.roll_number, m.marks_obtained;
END;
$$ LANGUAGE plpgsql;

-- 6. Calculate CGPA for a student
CREATE OR REPLACE FUNCTION calculate_cgpa(p_student_id INTEGER)
RETURNS DECIMAL AS $$
DECLARE
    v_cgpa DECIMAL(3, 2);
BEGIN
    SELECT ROUND(AVG(marks_obtained) / 10, 2) INTO v_cgpa
    FROM marks
    WHERE student_id = p_student_id;
    
    RETURN COALESCE(v_cgpa, 0.00);
END;
$$ LANGUAGE plpgsql;

-- 7. Update student CGPA
CREATE OR REPLACE PROCEDURE update_student_cgpa(p_student_id INTEGER)
LANGUAGE plpgsql AS $$
DECLARE
    v_cgpa DECIMAL(3, 2);
BEGIN
    v_cgpa := calculate_cgpa(p_student_id);
    
    UPDATE students
    SET cgpa = v_cgpa
    WHERE student_id = p_student_id;
END;
$$;

-- 8. Fetch semester-wise performance for a student
CREATE OR REPLACE FUNCTION get_semester_performance(p_student_id INTEGER)
RETURNS TABLE (
    semester INTEGER,
    total_subjects INTEGER,
    average_marks DECIMAL,
    semester_gpa DECIMAL,
    backlogs INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        m.semester,
        COUNT(*)::INTEGER AS total_subjects,
        ROUND(AVG(m.marks_obtained), 2) AS average_marks,
        ROUND(AVG(m.marks_obtained) / 10, 2) AS semester_gpa,
        COUNT(CASE WHEN m.marks_obtained < 40 THEN 1 END)::INTEGER AS backlogs
    FROM marks m
    WHERE m.student_id = p_student_id
    GROUP BY m.semester
    ORDER BY m.semester;
END;
$$ LANGUAGE plpgsql;

-- 9. Fetch subject-wise marks trend for a student
CREATE OR REPLACE FUNCTION get_subject_marks_trend(p_student_id INTEGER)
RETURNS TABLE (
    subject_id INTEGER,
    subject_name VARCHAR,
    semester INTEGER,
    marks_obtained DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sub.subject_id,
        sub.subject_name,
        m.semester,
        m.marks_obtained
    FROM marks m
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE m.student_id = p_student_id
    ORDER BY sub.subject_name, m.semester;
END;
$$ LANGUAGE plpgsql;

-- 10. Identify weak subjects for a student (marks < 50)
CREATE OR REPLACE FUNCTION get_weak_subjects(
    p_student_id INTEGER,
    p_threshold DECIMAL DEFAULT 50.0
)
RETURNS TABLE (
    subject_id INTEGER,
    subject_name VARCHAR,
    average_marks DECIMAL,
    times_below_threshold INTEGER,
    latest_semester INTEGER,
    latest_marks DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sub.subject_id,
        sub.subject_name,
        ROUND(AVG(m.marks_obtained), 2) AS average_marks,
        COUNT(CASE WHEN m.marks_obtained < p_threshold THEN 1 END)::INTEGER AS times_below_threshold,
        MAX(m.semester) AS latest_semester,
        (SELECT m2.marks_obtained 
         FROM marks m2 
         WHERE m2.student_id = p_student_id 
           AND m2.subject_id = sub.subject_id 
         ORDER BY m2.semester DESC 
         LIMIT 1) AS latest_marks
    FROM marks m
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE m.student_id = p_student_id
    GROUP BY sub.subject_id, sub.subject_name
    HAVING AVG(m.marks_obtained) < p_threshold
    ORDER BY average_marks ASC;
END;
$$ LANGUAGE plpgsql;

-- 11. Get overall class performance by subject
CREATE OR REPLACE FUNCTION get_class_performance_by_subject(
    p_subject_id INTEGER,
    p_semester INTEGER DEFAULT NULL
)
RETURNS TABLE (
    subject_id INTEGER,
    subject_name VARCHAR,
    semester INTEGER,
    total_students INTEGER,
    average_marks DECIMAL,
    highest_marks DECIMAL,
    lowest_marks DECIMAL,
    pass_count INTEGER,
    fail_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sub.subject_id,
        sub.subject_name,
        m.semester,
        COUNT(DISTINCT m.student_id)::INTEGER AS total_students,
        ROUND(AVG(m.marks_obtained), 2) AS average_marks,
        MAX(m.marks_obtained) AS highest_marks,
        MIN(m.marks_obtained) AS lowest_marks,
        COUNT(CASE WHEN m.marks_obtained >= 40 THEN 1 END)::INTEGER AS pass_count,
        COUNT(CASE WHEN m.marks_obtained < 40 THEN 1 END)::INTEGER AS fail_count
    FROM marks m
    INNER JOIN subjects sub ON m.subject_id = sub.subject_id
    WHERE 
        m.subject_id = p_subject_id
        AND (p_semester IS NULL OR m.semester = p_semester)
    GROUP BY sub.subject_id, sub.subject_name, m.semester
    ORDER BY m.semester;
END;
$$ LANGUAGE plpgsql;

-- 12. Get student complete performance report
CREATE OR REPLACE FUNCTION get_student_performance_report(p_student_id INTEGER)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    stream VARCHAR,
    current_cgpa DECIMAL,
    total_subjects INTEGER,
    overall_average DECIMAL,
    total_backlogs INTEGER,
    semesters_completed INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        s.stream,
        s.cgpa AS current_cgpa,
        COUNT(DISTINCT m.subject_id)::INTEGER AS total_subjects,
        ROUND(AVG(m.marks_obtained), 2) AS overall_average,
        COUNT(CASE WHEN m.marks_obtained < 40 THEN 1 END)::INTEGER AS total_backlogs,
        COUNT(DISTINCT m.semester)::INTEGER AS semesters_completed
    FROM students s
    LEFT JOIN marks m ON s.student_id = m.student_id
    WHERE s.student_id = p_student_id
    GROUP BY s.student_id, s.name, s.roll_number, s.stream, s.cgpa;
END;
$$ LANGUAGE plpgsql;

-- 13. Trigger to auto-update CGPA when marks are inserted or updated
CREATE OR REPLACE FUNCTION update_cgpa_trigger()
RETURNS TRIGGER AS $$
BEGIN
    CALL update_student_cgpa(COALESCE(NEW.student_id, OLD.student_id));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER marks_cgpa_update
AFTER INSERT OR UPDATE OR DELETE ON marks
FOR EACH ROW
EXECUTE FUNCTION update_cgpa_trigger();

-- 14. Get stream-wise performance comparison
CREATE OR REPLACE FUNCTION get_stream_performance()
RETURNS TABLE (
    stream VARCHAR,
    total_students INTEGER,
    average_cgpa DECIMAL,
    highest_cgpa DECIMAL,
    lowest_cgpa DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.stream,
        COUNT(*)::INTEGER AS total_students,
        ROUND(AVG(s.cgpa), 2) AS average_cgpa,
        MAX(s.cgpa) AS highest_cgpa,
        MIN(s.cgpa) AS lowest_cgpa
    FROM students s
    GROUP BY s.stream
    ORDER BY average_cgpa DESC;
END;
$$ LANGUAGE plpgsql;

-- 15. Get pending doubts for a teacher
CREATE OR REPLACE FUNCTION get_pending_doubts_by_teacher(p_teacher_id INTEGER)
RETURNS TABLE (
    doubt_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    question TEXT,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d.doubt_id,
        s.name AS student_name,
        s.roll_number,
        d.question,
        d.created_at
    FROM doubts d
    INNER JOIN students s ON d.student_id = s.student_id
    WHERE 
        d.teacher_id = p_teacher_id
        AND d.status = 'PENDING'
    ORDER BY d.created_at ASC;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- ADDITIONAL PROCEDURES
-- =====================================================

-- 16. Activity logging procedure
CREATE OR REPLACE PROCEDURE log_activity(
    p_user_id INTEGER,
    p_action VARCHAR,
    p_entity_type VARCHAR,
    p_entity_id INTEGER DEFAULT NULL
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO activity_logs (user_id, action, entity_type, entity_id, created_at)
    VALUES (p_user_id, p_action, p_entity_type, p_entity_id, CURRENT_TIMESTAMP);
END;
$$;

-- 17. Performance risk scoring function
CREATE OR REPLACE FUNCTION calculate_risk_score(p_student_id INTEGER)
RETURNS DECIMAL AS $$
DECLARE
    v_cgpa DECIMAL;
    v_backlog_count INTEGER;
    v_recent_avg DECIMAL;
    v_risk_score DECIMAL;
BEGIN
    SELECT cgpa INTO v_cgpa
    FROM students
    WHERE student_id = p_student_id;
    
    SELECT COUNT(*) INTO v_backlog_count
    FROM marks
    WHERE student_id = p_student_id
    AND marks_obtained < 40;
    
    SELECT AVG(marks_obtained) INTO v_recent_avg
    FROM (
        SELECT marks_obtained
        FROM marks
        WHERE student_id = p_student_id
        ORDER BY semester DESC, mark_id DESC
        LIMIT 5
    ) recent_marks;
    
    v_cgpa := COALESCE(v_cgpa, 0);
    v_backlog_count := COALESCE(v_backlog_count, 0);
    v_recent_avg := COALESCE(v_recent_avg, 0);
    
    v_risk_score := 
        (10 - v_cgpa) * 10 + 
        v_backlog_count * 15 + 
        (100 - v_recent_avg) * 0.5;
    
    RETURN ROUND(v_risk_score, 2);
END;
$$ LANGUAGE plpgsql;

-- 18. Get high-risk students
CREATE OR REPLACE FUNCTION get_high_risk_students(p_threshold DECIMAL DEFAULT 50.0)
RETURNS TABLE (
    student_id INTEGER,
    student_name VARCHAR,
    roll_number VARCHAR,
    stream VARCHAR,
    cgpa DECIMAL,
    backlog_count INTEGER,
    risk_score DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.student_id,
        s.name AS student_name,
        s.roll_number,
        s.stream,
        s.cgpa,
        (SELECT COUNT(*)::INTEGER 
         FROM marks m 
         WHERE m.student_id = s.student_id 
         AND m.marks_obtained < 40) AS backlog_count,
        calculate_risk_score(s.student_id) AS risk_score
    FROM students s
    WHERE calculate_risk_score(s.student_id) >= p_threshold
    ORDER BY risk_score DESC;
END;
$$ LANGUAGE plpgsql;

-- 19. Get user activity history
CREATE OR REPLACE FUNCTION get_user_activity(
    p_user_id INTEGER,
    p_limit INTEGER DEFAULT 50
)
RETURNS TABLE (
    log_id INTEGER,
    action VARCHAR,
    entity_type VARCHAR,
    entity_id INTEGER,
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        al.log_id,
        al.action,
        al.entity_type,
        al.entity_id,
        al.created_at
    FROM activity_logs al
    WHERE al.user_id = p_user_id
    ORDER BY al.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- 20. Get active academic term
CREATE OR REPLACE FUNCTION get_active_term()
RETURNS TABLE (
    term_id INTEGER,
    academic_year VARCHAR,
    semester INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        at.term_id,
        at.academic_year,
        at.semester
    FROM academic_terms at
    WHERE at.is_active = TRUE
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

-- 21. Set active academic term
CREATE OR REPLACE PROCEDURE set_active_term(p_term_id INTEGER)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE academic_terms
    SET is_active = FALSE
    WHERE is_active = TRUE;
    
    UPDATE academic_terms
    SET is_active = TRUE
    WHERE term_id = p_term_id;
END;
$$;

-- =====================================================
-- ADDITIONAL TRIGGERS
-- =====================================================

-- Trigger function for logging marks activities
CREATE OR REPLACE FUNCTION log_marks_activity()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id INTEGER;
    v_action VARCHAR(50);
BEGIN
    SELECT user_id INTO v_user_id
    FROM students
    WHERE student_id = COALESCE(NEW.student_id, OLD.student_id);
    
    IF TG_OP = 'INSERT' THEN
        v_action := 'MARKS_INSERTED';
        CALL log_activity(v_user_id, v_action, 'marks', NEW.mark_id);
    ELSIF TG_OP = 'UPDATE' THEN
        v_action := 'MARKS_UPDATED';
        CALL log_activity(v_user_id, v_action, 'marks', NEW.mark_id);
    ELSIF TG_OP = 'DELETE' THEN
        v_action := 'MARKS_DELETED';
        CALL log_activity(v_user_id, v_action, 'marks', OLD.mark_id);
    END IF;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER marks_activity_log
AFTER INSERT OR UPDATE OR DELETE ON marks
FOR EACH ROW
EXECUTE FUNCTION log_marks_activity();

-- Trigger function for logging doubt activities
CREATE OR REPLACE FUNCTION log_doubt_activity()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id INTEGER;
    v_action VARCHAR(50);
BEGIN
    IF TG_OP = 'INSERT' THEN
        SELECT user_id INTO v_user_id
        FROM students
        WHERE student_id = NEW.student_id;
        v_action := 'DOUBT_CREATED';
        CALL log_activity(v_user_id, v_action, 'doubts', NEW.doubt_id);
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status = 'PENDING' AND NEW.status = 'ANSWERED' THEN
            SELECT user_id INTO v_user_id
            FROM teachers
            WHERE teacher_id = NEW.teacher_id;
            v_action := 'DOUBT_ANSWERED';
            CALL log_activity(v_user_id, v_action, 'doubts', NEW.doubt_id);
            
            SELECT user_id INTO v_user_id
            FROM students
            WHERE student_id = NEW.student_id;
            CALL log_activity(v_user_id, 'DOUBT_RESOLVED', 'doubts', NEW.doubt_id);
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER doubt_activity_log
AFTER INSERT OR UPDATE ON doubts
FOR EACH ROW
EXECUTE FUNCTION log_doubt_activity();

-- Trigger function for logging CGPA updates
CREATE OR REPLACE FUNCTION log_cgpa_update()
RETURNS TRIGGER AS $$
DECLARE
    v_user_id INTEGER;
BEGIN
    IF OLD.cgpa IS DISTINCT FROM NEW.cgpa THEN
        SELECT user_id INTO v_user_id
        FROM students
        WHERE student_id = NEW.student_id;
        
        CALL log_activity(v_user_id, 'CGPA_UPDATED', 'students', NEW.student_id);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cgpa_update_log
AFTER UPDATE ON students
FOR EACH ROW
EXECUTE FUNCTION log_cgpa_update();
