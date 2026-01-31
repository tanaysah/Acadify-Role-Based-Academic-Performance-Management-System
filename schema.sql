DROP TABLE IF EXISTS doubts CASCADE;
DROP TABLE IF EXISTS marks CASCADE;
DROP TABLE IF EXISTS subjects CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS teachers CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    user_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    student_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL CHECK (age > 0),
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    stream VARCHAR(100) NOT NULL,
    cgpa DECIMAL(3, 2) CHECK (cgpa >= 0 AND cgpa <= 10),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE teachers (
    teacher_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE admins (
    admin_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE subjects (
    subject_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    subject_name VARCHAR(255) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester > 0),
    teacher_id INTEGER,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

CREATE TABLE marks (
    mark_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    student_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    marks_obtained DECIMAL(5, 2) NOT NULL CHECK (marks_obtained >= 0 AND marks_obtained <= 100),
    semester INTEGER NOT NULL CHECK (semester > 0),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    UNIQUE (student_id, subject_id, semester)
);

CREATE TABLE doubts (
    doubt_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    student_id INTEGER NOT NULL,
    teacher_id INTEGER,
    question TEXT NOT NULL,
    answer TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ANSWERED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_students_roll ON students(roll_number);
CREATE INDEX idx_students_stream ON students(stream);
CREATE INDEX idx_marks_student ON marks(student_id);
CREATE INDEX idx_marks_subject ON marks(subject_id);
CREATE INDEX idx_marks_semester ON marks(semester);
CREATE INDEX idx_subjects_semester ON subjects(semester);
CREATE INDEX idx_doubts_student ON doubts(student_id);
CREATE INDEX idx_doubts_teacher ON doubts(teacher_id);
CREATE INDEX idx_doubts_status ON doubts(status);


CREATE TABLE activity_logs (
    log_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE password_resets (
    reset_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id INTEGER NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_flag BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE academic_terms (
    term_id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    academic_year VARCHAR(20) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester > 0),
    is_active BOOLEAN DEFAULT FALSE,
    UNIQUE (academic_year, semester)
);

CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created ON activity_logs(created_at);
CREATE INDEX idx_password_resets_token ON password_resets(reset_token);
CREATE INDEX idx_academic_terms_active ON academic_terms(is_active);
