Acadify is a role-based academic performance management web application for institutions. HTML, CSS, JavaScript, Java, SQL, and Python.
ACADIFY (Academic Management System)
│
├── DATABASE (Data Layer)
│   │
│   ├── schema.sql
│   │   ├── users
│   │   ├── students
│   │   ├── teachers
│   │   ├── admins
│   │   ├── subjects
│   │   ├── marks
│   │   └── doubts
│   │
│   └── procedures.sql
│       ├── auth_procedures
│       ├── student_operations
│       ├── teacher_operations
│       └── admin_operations
│
├── BACKEND (Business Logic Layer – Java)
│   │
│   ├── MainApplication.java
│   ├── DatabaseConfig.java
│   │
│   ├── Controllers
│   │   ├── AuthController.java
│   │   ├── StudentController.java
│   │   ├── TeacherController.java
│   │   └── AdminController.java
│   │
│   ├── Security & Auth
│   │   ├── PBKDF2Util.java
│   │   ├── SessionUtil.java
│   │   └── EntityResolver.java
│   │
│   └── Utilities
│       ├── InputValidator.java
│       ├── RequestUtil.java
│       └── ResponseUtil.java
│
├── PYTHON ANALYTICS (Intelligence Layer)
│   │
│   ├── db.py
│   ├── queries.py
│   │
│   ├── Analytics Modules
│   │   ├── student_analytics.py
│   │   ├── teacher_analytics.py
│   │   └── subject_analytics.py
│   │
│   ├── reports.py
│   └── main.py
│
├── FRONTEND UI (Presentation Layer)
│   │
│   ├── Authentication
│   │   ├── login.html
│   │   └── logout.html
│   │
│   ├── Dashboards
│   │   ├── student_dashboard.html
│   │   ├── teacher_dashboard.html
│   │   └── admin_dashboard.html
│   │
│   ├── Assets
│   │   ├── css/
│   │   ├── js/
│   │   └── images/
│   │
│   └── api_integration.js
│
└── DEPLOYMENT (Production Layer)
    │
    ├── .env
    ├── docker-compose.yml
    ├── Dockerfile
    ├── render.yaml / railway.toml
    ├── README.md
    └── migrations/
