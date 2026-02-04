# Read-Only Analytics Database User - Setup Guide

## 📋 Overview

This guide will help you create a dedicated read-only database user (`analytics_user`) for your Python analytics layer. This ensures the analytics system cannot accidentally modify production data.

---

## 🎯 What We're Doing

```
┌─────────────────┐
│   PostgreSQL    │
│    Database     │
│    (acadify)    │
└────────┬────────┘
         │
         ├──────────────────────────────────────┐
         │                                      │
         ▼                                      ▼
┌─────────────────┐                  ┌─────────────────┐
│  Admin User     │                  │ analytics_user  │
│  (Full Access)  │                  │  (READ-ONLY)    │
├─────────────────┤                  ├─────────────────┤
│ • SELECT        │                  │ • SELECT ✓      │
│ • INSERT        │                  │ • INSERT ✗      │
│ • UPDATE        │                  │ • UPDATE ✗      │
│ • DELETE        │                  │ • DELETE ✗      │
│ • CREATE        │                  │ • CREATE ✗      │
│ • DROP          │                  │ • DROP ✗        │
└─────────────────┘                  └─────────────────┘
         │                                      │
         ▼                                      ▼
┌─────────────────┐                  ┌─────────────────┐
│    Backend      │                  │Python Analytics │
│   Application   │                  │     Layer       │
└─────────────────┘                  └─────────────────┘
```

---

## 🚀 Quick Start (3 Steps)

### Step 1: Run SQL Script in PostgreSQL

**Option A: Using psql (Command Line)**
```bash
# Connect to your database as admin/superuser
psql -U postgres -d acadify

# Run the setup script
\i setup_analytics_user.sql

# Verify user was created
\du analytics_user
```

**Option B: Using pgAdmin**
1. Open pgAdmin
2. Connect to your `acadify` database
3. Open Query Tool
4. Paste contents of `setup_analytics_user.sql`
5. Click Execute (▶️)

**Option C: Using DBeaver/DataGrip**
1. Connect to `acadify` database
2. Open SQL Editor
3. Paste contents of `setup_analytics_user.sql`
4. Execute script

---

### Step 2: Create .env File for Python Analytics

```bash
# In your Python analytics directory, copy the example
cp .env.example .env

# Edit .env and update with your actual password
nano .env
```

Update the `.env` file:
```env
DB_URL=postgresql://localhost:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=YourSecurePasswordHere  # ⚠️ Change this!
```

---

### Step 3: Test the Connection

**Quick Test:**
```bash
# Test database connection
psql -U analytics_user -d acadify -h localhost
```

**Python Test:**
```bash
# Test from Python
cd /path/to/analytics
python -c "from db import DatabaseConnection; DatabaseConnection.initialize(); print('✅ Connected successfully!')"
```

---

## ✅ Verification Checklist

After setup, verify these things work:

### 1. User Exists
```sql
SELECT rolname, rolsuper, rolcanlogin 
FROM pg_roles 
WHERE rolname = 'analytics_user';
```

Expected output:
```
    rolname     | rolsuper | rolcanlogin
----------------+----------+-------------
 analytics_user | f        | t
```

### 2. Can Read Tables
```sql
-- Login as analytics_user
\c acadify analytics_user

-- Try reading
SELECT COUNT(*) FROM students;
SELECT COUNT(*) FROM teachers;
SELECT COUNT(*) FROM marks;
```

Should work! ✅

### 3. Cannot Write (Safety Check)
```sql
-- Try inserting (should fail)
INSERT INTO students (user_id, name, age, roll_number, stream) 
VALUES (999, 'Test', 20, 'TEST123', 'Computer Science');
```

Expected error:
```
ERROR: permission denied for table students
```

Perfect! This means read-only is working! ✅

### 4. Can Execute Stored Procedures
```sql
-- Test stored procedure
SELECT * FROM get_student_performance_report(1);
SELECT * FROM get_semester_performance(1);
```

Should work! ✅

---

## 🔒 Security Best Practices

### 1. **Strong Password**
```bash
# Generate a strong password
openssl rand -base64 32

# Use at least 16 characters with mix of:
# - Uppercase letters (A-Z)
# - Lowercase letters (a-z)  
# - Numbers (0-9)
# - Special characters (!@#$%^&*)
```

### 2. **Environment Variables**
```bash
# Add .env to .gitignore
echo ".env" >> .gitignore

# Never commit .env to version control!
# ⚠️ NEVER DO THIS: git add .env
```

### 3. **File Permissions**
```bash
# Restrict .env file access
chmod 600 .env

# Only the owner can read/write
```

### 4. **Password Rotation**
```sql
-- Change password every 90 days
ALTER USER analytics_user WITH PASSWORD 'NewSecurePassword456';
```

### 5. **Connection Monitoring**
```sql
-- Check active connections
SELECT 
    usename,
    application_name,
    client_addr,
    state,
    query_start
FROM pg_stat_activity
WHERE usename = 'analytics_user';
```

---

## 🛠️ Troubleshooting

### Problem: "Connection refused"

**Possible causes:**
- PostgreSQL is not running
- Wrong host/port
- Firewall blocking connection

**Solutions:**
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Start PostgreSQL if stopped
sudo systemctl start postgresql

# Check which port PostgreSQL is using
sudo netstat -plnt | grep postgres
```

---

### Problem: "Authentication failed"

**Possible causes:**
- Wrong username or password
- `pg_hba.conf` not configured correctly

**Solutions:**
```bash
# Find pg_hba.conf location
psql -U postgres -c "SHOW hba_file"

# Edit pg_hba.conf (as superuser)
sudo nano /etc/postgresql/15/main/pg_hba.conf

# Add this line (if not present):
# local   all             analytics_user                          md5
# host    all             analytics_user  127.0.0.1/32            md5

# Reload PostgreSQL
sudo systemctl reload postgresql
```

---

### Problem: "Permission denied for table X"

**Possible causes:**
- `setup_analytics_user.sql` not executed completely
- New tables created after user setup

**Solutions:**
```sql
-- Re-grant permissions as superuser
\c acadify postgres

GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO analytics_user;

-- Ensure future tables get permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO analytics_user;
```

---

### Problem: "Role 'analytics_user' does not exist"

**Solution:**
```sql
-- Create the user
CREATE USER analytics_user WITH PASSWORD 'YourPassword';

-- Then run the rest of setup_analytics_user.sql
```

---

## 📁 File Locations Reference

```
Your Project/
│
├── Backend/                    # Your main application
│   ├── .env                   # Backend DB credentials (FULL ACCESS)
│   └── ...
│
├── Python Analytics/          # Analytics layer
│   ├── .env                   # Analytics DB credentials (READ-ONLY)
│   ├── .env.example          # Template (this file)
│   ├── db.py                  # Database connection
│   ├── queries.py            # SQL queries
│   └── ...
│
└── Database/
    └── setup_analytics_user.sql  # User creation script
```

---

## 🔄 Different Scenarios

### Scenario 1: Local Development
```env
# .env for local development
DB_URL=postgresql://localhost:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=dev_password_123
```

### Scenario 2: Remote Database
```env
# .env for remote database
DB_URL=postgresql://db.example.com:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=secure_remote_password
```

### Scenario 3: Docker Container
```env
# .env for Docker
DB_URL=postgresql://postgres-container:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=docker_password
```

### Scenario 4: Cloud Database (AWS RDS, etc.)
```env
# .env for cloud database
DB_URL=postgresql://acadify.xxxxx.us-east-1.rds.amazonaws.com:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=cloud_password_xyz
```

---

## 🧪 Testing the Setup

### Full Integration Test

Create a test script `test_connection.py`:

```python
#!/usr/bin/env python3
"""Test analytics database connection and permissions."""

from db import DatabaseConnection
import queries

def test_connection():
    """Test basic database connection."""
    print("Testing database connection...")
    try:
        DatabaseConnection.initialize()
        print("✅ Connection pool initialized successfully!")
        return True
    except Exception as e:
        print(f"❌ Connection failed: {e}")
        return False

def test_read_operations():
    """Test read operations."""
    print("\nTesting read operations...")
    try:
        students = queries.get_all_students()
        print(f"✅ Successfully read {len(students)} students")
        
        teachers = queries.get_all_teachers()
        print(f"✅ Successfully read {len(teachers)} teachers")
        
        subjects = queries.get_all_subjects()
        print(f"✅ Successfully read {len(subjects)} subjects")
        
        return True
    except Exception as e:
        print(f"❌ Read operations failed: {e}")
        return False

def test_stored_procedures():
    """Test stored procedure execution."""
    print("\nTesting stored procedures...")
    try:
        # Test if we can call stored procedures
        report = queries.get_student_performance_report(1)
        if report:
            print("✅ Stored procedures work correctly")
        else:
            print("⚠️  No data found (this is OK if no students exist)")
        return True
    except Exception as e:
        print(f"❌ Stored procedures failed: {e}")
        return False

def main():
    """Run all tests."""
    print("=" * 60)
    print("ANALYTICS DATABASE CONNECTION TEST")
    print("=" * 60)
    
    results = []
    results.append(("Connection", test_connection()))
    results.append(("Read Operations", test_read_operations()))
    results.append(("Stored Procedures", test_stored_procedures()))
    
    print("\n" + "=" * 60)
    print("TEST RESULTS")
    print("=" * 60)
    
    for test_name, passed in results:
        status = "✅ PASSED" if passed else "❌ FAILED"
        print(f"{test_name}: {status}")
    
    all_passed = all(result[1] for result in results)
    
    if all_passed:
        print("\n🎉 All tests passed! Analytics database is ready to use!")
    else:
        print("\n⚠️  Some tests failed. Please check the errors above.")
    
    DatabaseConnection.close_all()
    return all_passed

if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)
```

Run the test:
```bash
python test_connection.py
```

---

## 📚 Additional Resources

### Useful PostgreSQL Commands

```sql
-- List all users
\du

-- List all databases
\l

-- Connect to database
\c acadify

-- List all tables
\dt

-- Show table permissions
\dp

-- Show user's permissions
SELECT * FROM information_schema.table_privileges 
WHERE grantee = 'analytics_user';
```

### Connection String Formats

```
# Standard format
postgresql://user:password@host:port/database

# With SSL
postgresql://user:password@host:port/database?sslmode=require

# Unix socket
postgresql:///database?host=/var/run/postgresql

# Multiple hosts (failover)
postgresql://host1,host2,host3/database
```

---

## ✨ Summary

You've successfully:

1. ✅ Created a read-only `analytics_user` in PostgreSQL
2. ✅ Granted appropriate SELECT permissions
3. ✅ Configured `.env` file for Python analytics
4. ✅ Tested the connection
5. ✅ Verified read-only restrictions work

**Your analytics layer is now secure and production-ready!**

---

## 📞 Need Help?

If you encounter issues:

1. Check the troubleshooting section above
2. Verify all SQL commands executed successfully
3. Check PostgreSQL logs: `/var/log/postgresql/`
4. Test connection manually with `psql`
5. Verify `.env` file has correct credentials

---

**Last Updated:** 2024-02-04
**Version:** 1.0
