DROP USER IF EXISTS analytics_user;

CREATE USER analytics_user WITH PASSWORD 'StrongPassword123';

COMMENT ON ROLE analytics_user IS 'Read-only user for analytics layer';

GRANT CONNECT ON DATABASE acadify TO analytics_user;

GRANT USAGE ON SCHEMA public TO analytics_user;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO analytics_user;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO analytics_user;

GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO analytics_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO analytics_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO analytics_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT EXECUTE ON FUNCTIONS TO analytics_user;

SELECT 
    grantee,
    table_schema,
    table_name,
    privilege_type
FROM information_schema.table_privileges
WHERE grantee = 'analytics_user'
ORDER BY table_name, privilege_type;

SELECT 
    rolname,
    rolsuper,
    rolinherit,
    rolcreaterole,
    rolcreatedb,
    rolcanlogin
FROM pg_roles
WHERE rolname = 'analytics_user';

REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA public FROM analytics_user;
REVOKE CREATE ON SCHEMA public FROM analytics_user;
REVOKE TEMPORARY ON DATABASE acadify FROM analytics_user;

ALTER USER analytics_user WITH NOSUPERUSER NOCREATEDB NOCREATEROLE;

ALTER USER analytics_user CONNECTION LIMIT 10;

SELECT 'User created successfully' AS status
WHERE EXISTS (
    SELECT 1 FROM pg_roles WHERE rolname = 'analytics_user'
);

SELECT 
    'analytics_user is read-only' AS status,
    NOT rolsuper AS is_not_superuser,
    NOT rolcreatedb AS cannot_create_db,
    NOT rolcreaterole AS cannot_create_roles
FROM pg_roles
WHERE rolname = 'analytics_user';

SELECT 
    table_name,
    string_agg(privilege_type, ', ' ORDER BY privilege_type) AS privileges
FROM information_schema.table_privileges
WHERE grantee = 'analytics_user'
GROUP BY table_name
ORDER BY table_name;
