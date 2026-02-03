# 🔌 Backend Integration Guide

Complete guide for connecting acadify frontend to Java Spring Boot backend.

---

## 🏗️ Architecture Overview

```
┌─────────────────┐
│  React Frontend │
│   (Port 3000)   │
└────────┬────────┘
         │ Axios
         ▼
┌─────────────────┐
│  Spring Boot    │
│   (Port 8080)   │  ◄─── Session Management
└────────┬────────┘
         │
    ┌────┴────┬──────────┐
    ▼         ▼          ▼
┌────────┐ ┌──────┐ ┌─────────┐
│Postgres│ │Python│ │  Redis  │
│   DB   │ │Analytics│ │(Session)│
└────────┘ └──────┘ └─────────┘
```

---

## 🔐 Session-Based Authentication

### How It Works

1. **User logs in** → Frontend sends credentials
2. **Backend validates** → Creates HTTP session
3. **Session ID stored** → In HTTP-only cookie
4. **Every request** → Browser auto-sends cookie
5. **Backend validates** → Checks session, returns user data

### Why Session Cookies?

✅ **More Secure** than localStorage (XSS-proof)
✅ **Auto-managed** by browser
✅ **HTTP-only** flag prevents JavaScript access
✅ **SameSite** flag prevents CSRF

---

## 🚀 Backend Setup Checklist

### 1. Enable CORS

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")  // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)  // CRITICAL for session cookies
                .maxAge(3600);
    }
}
```

### 2. Configure Session Management

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .csrf().disable()  // Disable for API (use token if needed)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated();
        
        return http.build();
    }
}
```

### 3. Set Cookie Properties

```properties
# application.properties

# Session timeout (30 minutes)
server.servlet.session.timeout=30m

# Cookie settings
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false  # Set true in production (HTTPS)
server.servlet.session.cookie.same-site=lax
server.servlet.session.cookie.path=/
server.servlet.session.cookie.max-age=1800
```

---

## 📡 API Endpoints Reference

### Authentication Endpoints

#### POST /api/auth/login
**Request:**
```json
{
  "email": "student@acadify.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "student@acadify.com",
  "role": "STUDENT"
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials"
}
```

**Backend Implementation:**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
    User user = authService.authenticate(request.getEmail(), request.getPassword());
    
    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
    }
    
    // Store user in session
    session.setAttribute("user", user);
    
    return ResponseEntity.ok(Map.of(
        "id", user.getId(),
        "name", user.getName(),
        "email", user.getEmail(),
        "role", user.getRole()
    ));
}
```

---

#### POST /api/auth/signup
**Request:**
```json
{
  "name": "John Doe",
  "email": "john@acadify.com",
  "password": "password123",
  "role": "STUDENT"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@acadify.com",
  "role": "STUDENT"
}
```

---

#### GET /api/auth/me
**Purpose:** Validate current session and get user data

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "student@acadify.com",
  "role": "STUDENT"
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Not authenticated"
}
```

**Backend Implementation:**
```java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(HttpSession session) {
    User user = (User) session.getAttribute("user");
    
    if (user == null) {
        return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
    }
    
    return ResponseEntity.ok(Map.of(
        "id", user.getId(),
        "name", user.getName(),
        "email", user.getEmail(),
        "role", user.getRole()
    ));
}
```

---

#### POST /api/auth/logout
**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**Backend Implementation:**
```java
@PostMapping("/logout")
public ResponseEntity<?> logout(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
}
```

---

### Student Endpoints

#### GET /api/student/dashboard
**Response:**
```json
{
  "avgScore": 85,
  "attendance": 92,
  "rank": 5,
  "totalStudents": 120,
  "recentActivity": [
    {
      "title": "New assignment posted in Mathematics",
      "time": "2 hours ago"
    }
  ]
}
```

---

#### GET /api/student/marks
**Response:**
```json
{
  "subjects": [
    {
      "name": "Mathematics",
      "score": 88,
      "grade": "A",
      "status": "Pass"
    },
    {
      "name": "Physics",
      "score": 82,
      "grade": "B+",
      "status": "Pass"
    }
  ]
}
```

---

#### GET /api/student/analytics/performance
**Purpose:** Get AI-powered insights (from Python analytics)

**Response:**
```json
{
  "trend": "UP",
  "avgScore": 78,
  "riskLevel": "LOW",
  "predictions": {
    "nextExamScore": 82,
    "confidence": 0.85
  },
  "recommendations": [
    "Focus more on Physics practicals",
    "Excellent progress in Mathematics"
  ]
}
```

**Backend Implementation:**
```java
@GetMapping("/analytics/performance")
public ResponseEntity<?> getPerformanceAnalytics(HttpSession session) {
    User user = (User) session.getAttribute("user");
    
    // 1. Fetch student data from database
    List<Mark> marks = markRepository.findByStudentId(user.getId());
    
    // 2. Call Python analytics service
    AnalyticsResponse analytics = pythonService.analyzePerformance(marks);
    
    // 3. Return results
    return ResponseEntity.ok(analytics);
}
```

---

### Teacher Endpoints

#### GET /api/teacher/classes
**Response:**
```json
[
  {
    "id": 1,
    "name": "Math 101",
    "subject": "Mathematics",
    "students": 45,
    "schedule": "Mon, Wed, Fri 9:00 AM"
  }
]
```

---

### Admin Endpoints

#### GET /api/admin/users?role=STUDENT
**Response:**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@acadify.com",
    "role": "STUDENT",
    "active": true
  }
]
```

---

## 🐍 Python Analytics Integration

### How Backend Calls Python

```java
@Service
public class PythonAnalyticsService {
    
    public AnalyticsResponse analyzePerformance(List<Mark> marks) {
        try {
            // 1. Convert marks to JSON
            String marksJson = objectMapper.writeValueAsString(marks);
            
            // 2. Call Python script
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3",
                "analytics/performance.py",
                marksJson
            );
            
            Process process = processBuilder.start();
            
            // 3. Read Python output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String result = reader.readLine();
            
            // 4. Parse and return
            return objectMapper.readValue(result, AnalyticsResponse.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Python analytics failed", e);
        }
    }
}
```

### Alternative: Python REST API

```java
@Service
public class PythonAnalyticsService {
    
    private final RestTemplate restTemplate;
    private final String pythonApiUrl = "http://localhost:5000";
    
    public AnalyticsResponse analyzePerformance(List<Mark> marks) {
        String url = pythonApiUrl + "/analyze/performance";
        
        return restTemplate.postForObject(
            url,
            marks,
            AnalyticsResponse.class
        );
    }
}
```

---

## 🧪 Testing the Integration

### 1. Test Login Flow

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@acadify.com","password":"pass123"}' \
  -c cookies.txt

# Check session
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt
```

### 2. Test Protected Endpoints

```bash
# Get student dashboard (requires auth)
curl -X GET http://localhost:8080/api/student/dashboard \
  -b cookies.txt
```

### 3. Frontend Testing

Open browser console:
```javascript
// Check if cookies are being sent
console.log(document.cookie);  // Should see session ID

// Test API call
fetch('http://localhost:8080/api/auth/me', {
  credentials: 'include'  // CRITICAL for cookies
})
.then(r => r.json())
.then(console.log);
```

---

## 🐛 Troubleshooting

### Issue: "CORS error"
**Solution:**
- Check `allowedOrigins` includes frontend URL
- Ensure `allowCredentials(true)` is set
- Frontend must use `withCredentials: true` in Axios

### Issue: "Session not persisting"
**Solution:**
- Check cookie settings in browser DevTools
- Verify `SameSite` attribute
- Ensure both apps on same domain/localhost

### Issue: "401 Unauthorized on every request"
**Solution:**
- Check if session is being created on login
- Verify session timeout isn't too short
- Check if session is in Redis/memory

### Issue: "Python analytics not working"
**Solution:**
- Test Python script independently
- Check Python environment/dependencies
- Verify JSON serialization

---

## 📦 Database Schema (Recommended)

### Users Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashed
    role VARCHAR(50) NOT NULL,  -- STUDENT, TEACHER, ADMIN
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### Marks Table
```sql
CREATE TABLE marks (
    id SERIAL PRIMARY KEY,
    student_id INT REFERENCES users(id),
    subject VARCHAR(100) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    grade VARCHAR(10),
    exam_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## ✅ Production Checklist

- [ ] Enable HTTPS
- [ ] Set `cookie.secure=true`
- [ ] Configure session store (Redis)
- [ ] Add rate limiting
- [ ] Enable request logging
- [ ] Set up monitoring
- [ ] Configure firewall
- [ ] Add health check endpoint

---

**Integration Status:** Ready for backend development ✅
