# Analytics User Setup - Quick Reference

## 🚀 Quick Setup (Copy-Paste Commands)

### 1. Create Database User (Run as PostgreSQL admin)

```sql
-- Connect to database
psql -U postgres -d acadify

-- Create user
CREATE USER analytics_user WITH PASSWORD 'StrongPassword123';
GRANT CONNECT ON DATABASE acadify TO analytics_user;
GRANT USAGE ON SCHEMA public TO analytics_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO analytics_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO analytics_user;

-- Future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO analytics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO analytics_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO analytics_user;

-- Security
REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA public FROM analytics_user;
ALTER USER analytics_user WITH NOSUPERUSER NOCREATEDB NOCREATEROLE;
ALTER USER analytics_user CONNECTION LIMIT 10;
```

### 2. Create .env File (In Python analytics directory)

```bash
cat > .env << 'EOF'
DB_URL=postgresql://localhost:5432/acadify
DB_USER=analytics_user
DB_PASSWORD=StrongPassword123
EOF

chmod 600 .env
```

### 3. Test Connection

```bash
# Test with psql
psql -U analytics_user -d acadify -h localhost

# Test with Python
python test_connection.py
```

---

## 🔑 Connection Strings

### Local Database
```
postgresql://analytics_user:password@localhost:5432/acadify
```

### Remote Database
```
postgresql://analytics_user:password@hostname:5432/acadify
```

### With SSL
```
postgresql://analytics_user:password@hostname:5432/acadify?sslmode=require
```

### Docker
```
postgresql://analytics_user:password@postgres-container:5432/acadify
```

---

## 🔍 Verification Commands

### Check User Exists
```sql
\du analytics_user
```

### Check Permissions
```sql
SELECT table_name, privilege_type 
FROM information_schema.table_privileges 
WHERE grantee = 'analytics_user';
```

### Test Read Access
```sql
SELECT COUNT(*) FROM students;
```

### Test Write Protection (Should Fail)
```sql
INSERT INTO students VALUES (999, 999, 'Test', 20, 'TEST', 'CS', 0.0);
-- Expected: ERROR: permission denied
```

---

## 🛠️ Common Tasks

### Change Password
```sql
ALTER USER analytics_user WITH PASSWORD 'NewPassword123';
```

### Grant Access to New Table
```sql
GRANT SELECT ON new_table TO analytics_user;
```

### Revoke Access
```sql
REVOKE SELECT ON some_table FROM analytics_user;
```

### Delete User
```sql
DROP USER analytics_user;
```

---

## 🐛 Troubleshooting

### Can't Connect?
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check pg_hba.conf
sudo nano /etc/postgresql/*/main/pg_hba.conf
# Add: host all analytics_user 127.0.0.1/32 md5

# Reload config
sudo systemctl reload postgresql
```

### Permission Denied?
```sql
-- Re-grant permissions
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;
```

### User Doesn't Exist?
```sql
-- Check if user exists
SELECT * FROM pg_roles WHERE rolname = 'analytics_user';
```

---

## 📋 Environment Variables

| Variable | Example | Description |
|----------|---------|-------------|
| `DB_URL` | `postgresql://localhost:5432/acadify` | Database URL |
| `DB_USER` | `analytics_user` | Database username |
| `DB_PASSWORD` | `StrongPassword123` | User password |

---

## ✅ Checklist

- [ ] Run `setup_analytics_user.sql`
- [ ] Create `.env` file with credentials
- [ ] Test connection with `psql`
- [ ] Run `test_connection.py`
- [ ] Verify read-only (write should fail)
- [ ] Add `.env` to `.gitignore`
- [ ] Set file permissions: `chmod 600 .env`

---

## 🔒 Security Reminders

1. ✅ Use strong passwords (16+ characters)
2. ✅ Add `.env` to `.gitignore`
3. ✅ Set file permissions: `chmod 600 .env`
4. ✅ Rotate passwords every 90 days
5. ✅ Never commit credentials to Git
6. ✅ Use environment variables, not hardcoded values

---

## 📞 Quick Help

**Connection Issues?**
```bash
psql -U postgres -c "SELECT version()"  # Check PostgreSQL
netstat -plnt | grep 5432              # Check port
```

**Permission Issues?**
```sql
\c acadify postgres
GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;
```

**User Issues?**
```sql
CREATE USER analytics_user WITH PASSWORD 'password';
```

---

## 📂 File Locations

```
Project/
├── setup_analytics_user.sql    ← Run this in PostgreSQL
├── .env.example               ← Template
├── .env                       ← Your credentials (DON'T COMMIT!)
├── test_connection.py         ← Test script
└── SETUP_GUIDE.md            ← Full documentation
```

---

**Need more help?** See `SETUP_GUIDE.md` for detailed instructions.
