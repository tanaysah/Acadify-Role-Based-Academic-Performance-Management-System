"""
Query module for analytics layer.
Contains ALL SQL SELECT queries and stored procedure calls.
Returns raw data only - no processing or calculations.
"""

from typing import List, Dict, Any, Optional
from db import get_db_connection, release_db_connection


def execute_query(query: str, params: tuple = ()) -> List[Dict[str, Any]]:
    """
    Execute a SELECT query and return results as list of dictionaries.
    
    Args:
        query: SQL query string
        params: Query parameters
        
    Returns:
        List of dictionaries with column names as keys
    """
    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute(query, params)
        
        columns = [desc[0] for desc in cursor.description]
        results = [dict(zip(columns, row)) for row in cursor.fetchall()]
        
        cursor.close()
        return results
    except Exception as e:
        print(f"[QUERIES] Error executing query: {e}")
        raise
    finally:
        if conn:
            release_db_connection(conn)


# ============================================================================
# STUDENT QUERIES
# ============================================================================

def get_student_performance_report(student_id: int) -> Optional[Dict[str, Any]]:
    """
    Call get_student_performance_report() stored procedure.
    
    Args:
        student_id: Student ID
        
    Returns:
        Performance report dictionary or None
    """
    query = "SELECT * FROM get_student_performance_report(%s)"
    results = execute_query(query, (student_id,))
    return results[0] if results else None


def get_semester_performance(student_id: int) -> List[Dict[str, Any]]:
    """
    Call get_semester_performance() stored procedure.
    
    Args:
        student_id: Student ID
        
    Returns:
        List of semester performance records
    """
    query = "SELECT * FROM get_semester_performance(%s)"
    return execute_query(query, (student_id,))


def get_subject_marks_trend(student_id: int) -> List[Dict[str, Any]]:
    """
    Call get_subject_marks_trend() stored procedure.
    
    Args:
        student_id: Student ID
        
    Returns:
        List of subject marks across semesters
    """
    query = "SELECT * FROM get_subject_marks_trend(%s)"
    return execute_query(query, (student_id,))


def get_weak_subjects(student_id: int, threshold: float = 50.0) -> List[Dict[str, Any]]:
    """
    Call get_weak_subjects() stored procedure.
    
    Args:
        student_id: Student ID
        threshold: Marks threshold for weakness
        
    Returns:
        List of weak subjects
    """
    query = "SELECT * FROM get_weak_subjects(%s, %s)"
    return execute_query(query, (student_id, threshold))


def get_all_students() -> List[Dict[str, Any]]:
    """
    Get all students with basic info.
    
    Returns:
        List of all students
    """
    query = """
        SELECT student_id, user_id, name, age, roll_number, stream, cgpa
        FROM students
        ORDER BY roll_number
    """
    return execute_query(query)


def get_student_marks(student_id: int) -> List[Dict[str, Any]]:
    """
    Get all marks for a specific student.
    
    Args:
        student_id: Student ID
        
    Returns:
        List of marks records
    """
    query = """
        SELECT m.mark_id, m.student_id, m.subject_id, s.subject_name, 
               m.marks_obtained, m.semester
        FROM marks m
        JOIN subjects s ON m.subject_id = s.subject_id
        WHERE m.student_id = %s
        ORDER BY m.semester, s.subject_name
    """
    return execute_query(query, (student_id,))


def get_student_doubts(student_id: int) -> List[Dict[str, Any]]:
    """
    Get all doubts raised by a student.
    
    Args:
        student_id: Student ID
        
    Returns:
        List of doubt records
    """
    query = """
        SELECT doubt_id, student_id, teacher_id, question, answer, 
               status, created_at
        FROM doubts
        WHERE student_id = %s
        ORDER BY created_at DESC
    """
    return execute_query(query, (student_id,))


# ============================================================================
# TEACHER QUERIES
# ============================================================================

def get_teacher_students(teacher_id: int) -> List[Dict[str, Any]]:
    """
    Call get_teacher_students() stored procedure.
    
    Args:
        teacher_id: Teacher ID
        
    Returns:
        List of students taught by this teacher
    """
    query = "SELECT * FROM get_teacher_students(%s)"
    return execute_query(query, (teacher_id,))


def get_all_teachers() -> List[Dict[str, Any]]:
    """
    Get all teachers with basic info.
    
    Returns:
        List of all teachers
    """
    query = """
        SELECT teacher_id, user_id, name, department, designation
        FROM teachers
        ORDER BY name
    """
    return execute_query(query)


def get_teacher_subjects(teacher_id: int) -> List[Dict[str, Any]]:
    """
    Get all subjects taught by a teacher.
    
    Args:
        teacher_id: Teacher ID
        
    Returns:
        List of subjects
    """
    query = """
        SELECT subject_id, subject_name, semester
        FROM subjects
        WHERE teacher_id = %s
        ORDER BY semester, subject_name
    """
    return execute_query(query, (teacher_id,))


def get_teacher_doubts(teacher_id: int, status: Optional[str] = None) -> List[Dict[str, Any]]:
    """
    Get doubts assigned to a teacher.
    
    Args:
        teacher_id: Teacher ID
        status: Filter by status (PENDING/ANSWERED) or None for all
        
    Returns:
        List of doubt records
    """
    if status:
        query = """
            SELECT d.doubt_id, d.student_id, s.name as student_name, 
                   d.question, d.answer, d.status, d.created_at
            FROM doubts d
            JOIN students s ON d.student_id = s.student_id
            WHERE d.teacher_id = %s AND d.status = %s
            ORDER BY d.created_at DESC
        """
        return execute_query(query, (teacher_id, status))
    else:
        query = """
            SELECT d.doubt_id, d.student_id, s.name as student_name, 
                   d.question, d.answer, d.status, d.created_at
            FROM doubts d
            JOIN students s ON d.student_id = s.student_id
            WHERE d.teacher_id = %s
            ORDER BY d.created_at DESC
        """
        return execute_query(query, (teacher_id,))


# ============================================================================
# SUBJECT QUERIES
# ============================================================================

def get_top_performers(subject_id: Optional[int] = None, semester: Optional[int] = None, 
                       limit: int = 10) -> List[Dict[str, Any]]:
    """
    Call get_top_performers() stored procedure.
    
    Args:
        subject_id: Filter by subject or None for all
        semester: Filter by semester or None for all
        limit: Number of top performers
        
    Returns:
        List of top performing students
    """
    query = "SELECT * FROM get_top_performers(%s, %s, %s)"
    return execute_query(query, (subject_id, semester, limit))


def get_lowest_performers(subject_id: Optional[int] = None, semester: Optional[int] = None, 
                          limit: int = 10) -> List[Dict[str, Any]]:
    """
    Call get_lowest_performers() stored procedure.
    
    Args:
        subject_id: Filter by subject or None for all
        semester: Filter by semester or None for all
        limit: Number of lowest performers
        
    Returns:
        List of lowest performing students
    """
    query = "SELECT * FROM get_lowest_performers(%s, %s, %s)"
    return execute_query(query, (subject_id, semester, limit))


def get_marks_high_to_low(subject_id: Optional[int] = None, 
                          semester: Optional[int] = None) -> List[Dict[str, Any]]:
    """
    Call get_marks_high_to_low() stored procedure.
    
    Args:
        subject_id: Filter by subject or None for all
        semester: Filter by semester or None for all
        
    Returns:
        All marks sorted high to low
    """
    query = "SELECT * FROM get_marks_high_to_low(%s, %s)"
    return execute_query(query, (subject_id, semester))


def get_marks_low_to_high(subject_id: Optional[int] = None, 
                          semester: Optional[int] = None) -> List[Dict[str, Any]]:
    """
    Call get_marks_low_to_high() stored procedure.
    
    Args:
        subject_id: Filter by subject or None for all
        semester: Filter by semester or None for all
        
    Returns:
        All marks sorted low to high
    """
    query = "SELECT * FROM get_marks_low_to_high(%s, %s)"
    return execute_query(query, (subject_id, semester))


def detect_backlogs(semester: Optional[int] = None, 
                    passing_marks: float = 40.0) -> List[Dict[str, Any]]:
    """
    Call detect_backlogs() stored procedure.
    
    Args:
        semester: Filter by semester or None for all
        passing_marks: Passing threshold
        
    Returns:
        List of students with backlogs
    """
    query = "SELECT * FROM detect_backlogs(%s, %s)"
    return execute_query(query, (semester, passing_marks))


def get_subject_statistics(subject_id: int, semester: int) -> Optional[Dict[str, Any]]:
    """
    Call get_subject_statistics() stored procedure.
    
    Args:
        subject_id: Subject ID
        semester: Semester number
        
    Returns:
        Subject statistics dictionary or None
    """
    query = "SELECT * FROM get_subject_statistics(%s, %s)"
    results = execute_query(query, (subject_id, semester))
    return results[0] if results else None


def get_all_subjects() -> List[Dict[str, Any]]:
    """
    Get all subjects.
    
    Returns:
        List of all subjects
    """
    query = """
        SELECT s.subject_id, s.subject_name, s.semester, s.teacher_id, t.name as teacher_name
        FROM subjects s
        LEFT JOIN teachers t ON s.teacher_id = t.teacher_id
        ORDER BY s.semester, s.subject_name
    """
    return execute_query(query)


def get_subject_marks(subject_id: int, semester: Optional[int] = None) -> List[Dict[str, Any]]:
    """
    Get all marks for a specific subject.
    
    Args:
        subject_id: Subject ID
        semester: Filter by semester or None for all
        
    Returns:
        List of marks for this subject
    """
    if semester:
        query = """
            SELECT m.mark_id, m.student_id, st.name as student_name, 
                   st.roll_number, m.marks_obtained, m.semester
            FROM marks m
            JOIN students st ON m.student_id = st.student_id
            WHERE m.subject_id = %s AND m.semester = %s
            ORDER BY m.marks_obtained DESC
        """
        return execute_query(query, (subject_id, semester))
    else:
        query = """
            SELECT m.mark_id, m.student_id, st.name as student_name, 
                   st.roll_number, m.marks_obtained, m.semester
            FROM marks m
            JOIN students st ON m.student_id = st.student_id
            WHERE m.subject_id = %s
            ORDER BY m.semester, m.marks_obtained DESC
        """
        return execute_query(query, (subject_id,))


# ============================================================================
# GENERAL QUERIES
# ============================================================================

def get_user_activity(user_id: int, limit: int = 50) -> List[Dict[str, Any]]:
    """
    Call get_user_activity() stored procedure.
    
    Args:
        user_id: User ID
        limit: Number of activity records
        
    Returns:
        List of activity log records
    """
    query = "SELECT * FROM get_user_activity(%s, %s)"
    return execute_query(query, (user_id, limit))


def get_all_marks() -> List[Dict[str, Any]]:
    """
    Get all marks with student and subject information.
    
    Returns:
        List of all marks
    """
    query = """
        SELECT m.mark_id, m.student_id, st.name as student_name, st.roll_number,
               m.subject_id, sub.subject_name, m.marks_obtained, m.semester
        FROM marks m
        JOIN students st ON m.student_id = st.student_id
        JOIN subjects sub ON m.subject_id = sub.subject_id
        ORDER BY m.semester, st.roll_number, sub.subject_name
    """
    return execute_query(query)


def get_semester_wise_data(semester: int) -> List[Dict[str, Any]]:
    """
    Get all marks for a specific semester.
    
    Args:
        semester: Semester number
        
    Returns:
        List of marks for this semester
    """
    query = """
        SELECT m.mark_id, m.student_id, st.name as student_name, st.roll_number,
               m.subject_id, sub.subject_name, m.marks_obtained, m.semester
        FROM marks m
        JOIN students st ON m.student_id = st.student_id
        JOIN subjects sub ON m.subject_id = sub.subject_id
        WHERE m.semester = %s
        ORDER BY st.roll_number, sub.subject_name
    """
    return execute_query(query, (semester,))


def get_active_term() -> Optional[Dict[str, Any]]:
    """
    Call get_active_term() stored procedure.
    
    Returns:
        Active academic term or None
    """
    query = "SELECT * FROM get_active_term()"
    results = execute_query(query)
    return results[0] if results else None
