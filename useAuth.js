import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/auth.api';

// Auth Context
const AuthContext = createContext(null);

// Auth Provider Component
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Check session on mount
  useEffect(() => {
    checkSession();
  }, []);
  
  // Validate current session
  const checkSession = async () => {
    try {
      setLoading(true);
      const userData = await authAPI.me();
      setUser(userData);
      setError(null);
    } catch (err) {
      setUser(null);
      setError(null); // Don't show error on initial check
    } finally {
      setLoading(false);
    }
  };
  
  // Login function
  const login = async (email, password) => {
    try {
      setLoading(true);
      setError(null);
      const userData = await authAPI.login(email, password);
      setUser(userData);
      return userData;
    } catch (err) {
      setError(err.message || 'Login failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  // Signup function
  const signup = async (userData) => {
    try {
      setLoading(true);
      setError(null);
      const newUser = await authAPI.signup(userData);
      setUser(newUser);
      return newUser;
    } catch (err) {
      setError(err.message || 'Signup failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };
  
  // Logout function
  const logout = async () => {
    try {
      await authAPI.logout();
      setUser(null);
      setError(null);
    } catch (err) {
      console.error('Logout error:', err);
      // Still clear user even if API call fails
      setUser(null);
    }
  };
  
  const value = {
    user,
    loading,
    error,
    login,
    signup,
    logout,
    checkSession,
    isAuthenticated: !!user,
  };
  
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// Custom hook to use auth context
export function useAuth() {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
}
