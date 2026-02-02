"""
Database connection module for analytics layer.
Read-only access to the existing production database.
No SQL queries or analytics logic - connection management only.
"""

import psycopg2
from psycopg2 import pool
import os
from typing import Optional


class DatabaseConnection:
    """Manages read-only database connections for analytics."""
    
    _connection_pool: Optional[psycopg2.pool.SimpleConnectionPool] = None
    
    @classmethod
    def initialize(cls, min_conn: int = 1, max_conn: int = 5) -> None:
        """
        Initialize the connection pool.
        
        Args:
            min_conn: Minimum number of connections in pool
            max_conn: Maximum number of connections in pool
        """
        if cls._connection_pool is not None:
            print("[DB] Connection pool already initialized")
            return
        
        db_url = os.getenv("DB_URL")
        db_user = os.getenv("DB_USER")
        db_password = os.getenv("DB_PASSWORD")
        
        if not all([db_url, db_user, db_password]):
            raise ValueError("[DB] Missing required environment variables: DB_URL, DB_USER, DB_PASSWORD")
        
        try:
            cls._connection_pool = psycopg2.pool.SimpleConnectionPool(
                min_conn,
                max_conn,
                dsn=db_url,
                user=db_user,
                password=db_password,
                options="-c default_transaction_read_only=on"  # Read-only mode
            )
            print(f"[DB] Connection pool initialized ({min_conn}-{max_conn} connections)")
        except Exception as e:
            raise ConnectionError(f"[DB] Failed to initialize connection pool: {e}")
    
    @classmethod
    def get_connection(cls):
        """
        Get a connection from the pool.
        
        Returns:
            psycopg2 connection object
        """
        if cls._connection_pool is None:
            raise RuntimeError("[DB] Connection pool not initialized. Call initialize() first.")
        
        try:
            conn = cls._connection_pool.getconn()
            if conn is None:
                raise RuntimeError("[DB] Failed to get connection from pool")
            return conn
        except Exception as e:
            raise ConnectionError(f"[DB] Error getting connection: {e}")
    
    @classmethod
    def release_connection(cls, conn) -> None:
        """
        Return a connection to the pool.
        
        Args:
            conn: Connection to release
        """
        if cls._connection_pool is None:
            return
        
        try:
            cls._connection_pool.putconn(conn)
        except Exception as e:
            print(f"[DB] Error releasing connection: {e}")
    
    @classmethod
    def close_all(cls) -> None:
        """Close all connections in the pool."""
        if cls._connection_pool is not None:
            cls._connection_pool.closeall()
            cls._connection_pool = None
            print("[DB] All connections closed")


def get_db_connection():
    """
    Convenience function to get a database connection.
    
    Returns:
        psycopg2 connection object
    """
    return DatabaseConnection.get_connection()


def release_db_connection(conn) -> None:
    """
    Convenience function to release a database connection.
    
    Args:
        conn: Connection to release
    """
    DatabaseConnection.release_connection(conn)
