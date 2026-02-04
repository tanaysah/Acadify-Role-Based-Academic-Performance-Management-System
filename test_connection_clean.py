#!/usr/bin/env python3

import sys
import os
from typing import Tuple

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

try:
    from db import DatabaseConnection
    import queries
except ImportError as e:
    print(f"❌ Import Error: {e}")
    print("Make sure you're running this script from the analytics directory")
    sys.exit(1)


def print_header(text: str) -> None:
    print("\n" + "=" * 70)
    print(f"  {text}")
    print("=" * 70)


def print_test(name: str, passed: bool, details: str = "") -> None:
    status = "✅ PASS" if passed else "❌ FAIL"
    print(f"{status} | {name}")
    if details:
        print(f"      {details}")


def test_environment_variables() -> Tuple[bool, str]:
    required_vars = ['DB_URL', 'DB_USER', 'DB_PASSWORD']
    missing = [var for var in required_vars if not os.getenv(var)]
    
    if missing:
        return False, f"Missing variables: {', '.join(missing)}"
    
    db_user = os.getenv('DB_USER')
    if db_user != 'analytics_user':
        return False, f"DB_USER should be 'analytics_user', got '{db_user}'"
    
    db_url = os.getenv('DB_URL')
    if 'acadify' not in db_url:
        return False, f"DB_URL should contain 'acadify' database"
    
    return True, "All environment variables configured correctly"


def test_connection_pool() -> Tuple[bool, str]:
    try:
        DatabaseConnection.initialize()
        return True, "Connection pool initialized successfully"
    except ValueError as e:
        return False, f"Configuration error: {str(e)}"
    except Exception as e:
        return False, f"Connection failed: {str(e)}"


def test_basic_read() -> Tuple[bool, str]:
    try:
        students = queries.get_all_students()
        teachers = queries.get_all_teachers()
        subjects = queries.get_all_subjects()
        
        details = f"Read {len(students)} students, {len(teachers)} teachers, {len(subjects)} subjects"
        return True, details
    except Exception as e:
        return False, f"Read failed: {str(e)}"


def test_stored_procedures() -> Tuple[bool, str]:
    try:
        report = queries.get_student_performance_report(1)
        
        if report:
            student_name = report.get('student_name', 'Unknown')
            details = f"Stored procedure works - Retrieved data for {student_name}"
        else:
            details = "Stored procedure works - No data for student_id=1 (this is OK)"
        
        return True, details
    except Exception as e:
        return False, f"Stored procedure failed: {str(e)}"


def test_read_only_enforcement() -> Tuple[bool, str]:
    from db import get_db_connection, release_db_connection
    
    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        cursor.execute("""
            INSERT INTO students (user_id, name, age, roll_number, stream) 
            VALUES (999999, 'Test Student', 20, 'TEST999', 'Test Stream')
        """)
        
        cursor.close()
        return False, "WARNING: Write operation succeeded! Database is NOT read-only!"
        
    except Exception as e:
        error_msg = str(e).lower()
        if 'read-only' in error_msg or 'permission denied' in error_msg or 'cannot execute' in error_msg:
            return True, "Read-only enforcement working correctly"
        else:
            return False, f"Unexpected error: {str(e)}"
    finally:
        if conn:
            release_db_connection(conn)


def test_connection_limit() -> Tuple[bool, str]:
    try:
        from db import get_db_connection, release_db_connection
        conn = get_db_connection()
        
        cursor = conn.cursor()
        cursor.execute("SELECT 1")
        result = cursor.fetchone()
        cursor.close()
        
        release_db_connection(conn)
        
        if result and result[0] == 1:
            return True, "Connection pool working correctly"
        else:
            return False, "Query returned unexpected result"
            
    except Exception as e:
        return False, f"Connection pool error: {str(e)}"


def main():
    print_header("ANALYTICS DATABASE CONNECTION TEST")
    print(f"\nTesting read-only analytics_user connection to acadify database")
    print(f"Date: {__import__('datetime').datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    all_tests = []
    
    print("\n📋 Checking Configuration...")
    passed, details = test_environment_variables()
    print_test("Environment Variables", passed, details)
    all_tests.append(passed)
    
    if not passed:
        print("\n⚠️  Cannot proceed without proper environment configuration.")
        print("Please check your .env file and ensure:")
        print("  - DB_URL is set (e.g., postgresql://localhost:5432/acadify)")
        print("  - DB_USER is set to 'analytics_user'")
        print("  - DB_PASSWORD is set")
        return False
    
    print("\n🔌 Testing Database Connection...")
    passed, details = test_connection_pool()
    print_test("Connection Pool", passed, details)
    all_tests.append(passed)
    
    if not passed:
        print("\n⚠️  Cannot connect to database. Please check:")
        print("  - PostgreSQL is running")
        print("  - analytics_user exists in database")
        print("  - Password is correct")
        print("  - Database 'acadify' exists")
        return False
    
    print("\n🔄 Testing Connection Pool...")
    passed, details = test_connection_limit()
    print_test("Connection Pool Operations", passed, details)
    all_tests.append(passed)
    
    print("\n📖 Testing Read Operations...")
    passed, details = test_basic_read()
    print_test("Read Tables", passed, details)
    all_tests.append(passed)
    
    print("\n⚙️  Testing Stored Procedures...")
    passed, details = test_stored_procedures()
    print_test("Execute Stored Procedures", passed, details)
    all_tests.append(passed)
    
    print("\n🔒 Testing Read-Only Enforcement...")
    passed, details = test_read_only_enforcement()
    print_test("Read-Only Protection", passed, details)
    all_tests.append(passed)
    
    print_header("TEST SUMMARY")
    
    passed_count = sum(all_tests)
    total_count = len(all_tests)
    
    print(f"\nTests Passed: {passed_count}/{total_count}")
    print(f"Success Rate: {(passed_count/total_count)*100:.1f}%\n")
    
    if all(all_tests):
        print("🎉 SUCCESS! All tests passed!")
        print("\nYour analytics database connection is properly configured:")
        print("  ✓ Environment variables are set correctly")
        print("  ✓ Database connection is working")
        print("  ✓ Read operations are functional")
        print("  ✓ Stored procedures are accessible")
        print("  ✓ Write protection is active (read-only enforced)")
        print("\n✨ Analytics layer is ready to use!")
        result = True
    else:
        print("⚠️  WARNING: Some tests failed!")
        print("\nPlease review the errors above and:")
        print("  1. Check your .env file configuration")
        print("  2. Verify analytics_user exists and has correct permissions")
        print("  3. Ensure setup_analytics_user.sql was executed completely")
        print("  4. Check PostgreSQL logs for additional details")
        result = False
    
    print("\n🧹 Cleaning up...")
    DatabaseConnection.close_all()
    print("Connections closed.\n")
    
    print("=" * 70)
    
    return result


if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n⚠️  Test interrupted by user")
        DatabaseConnection.close_all()
        sys.exit(1)
    except Exception as e:
        print(f"\n\n❌ FATAL ERROR: {str(e)}")
        import traceback
        traceback.print_exc()
        DatabaseConnection.close_all()
        sys.exit(1)
