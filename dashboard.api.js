import api from './axios';

// Dashboard API Endpoints
// Role-specific data fetching

export const dashboardAPI = {
  // ==================
  // STUDENT ENDPOINTS
  // ==================
  
  student: {
    // Get student dashboard overview
    getOverview: async () => {
      try {
        const response = await api.get('/student/dashboard');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch student dashboard' };
      }
    },
    
    // Get student marks/grades
    getMarks: async () => {
      try {
        const response = await api.get('/student/marks');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch marks' };
      }
    },
    
    // Get attendance data
    getAttendance: async () => {
      try {
        const response = await api.get('/student/attendance');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch attendance' };
      }
    },
    
    // Get performance analytics (from Python)
    getPerformanceAnalytics: async () => {
      try {
        const response = await api.get('/student/analytics/performance');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch analytics' };
      }
    },
  },
  
  // ==================
  // TEACHER ENDPOINTS
  // ==================
  
  teacher: {
    // Get teacher dashboard overview
    getOverview: async () => {
      try {
        const response = await api.get('/teacher/dashboard');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch teacher dashboard' };
      }
    },
    
    // Get classes taught
    getClasses: async () => {
      try {
        const response = await api.get('/teacher/classes');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch classes' };
      }
    },
    
    // Get class performance (with Python analytics)
    getClassPerformance: async (classId) => {
      try {
        const response = await api.get(`/teacher/analytics/class/${classId}`);
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch class performance' };
      }
    },
    
    // Submit grades
    submitGrades: async (gradesData) => {
      try {
        const response = await api.post('/teacher/grades/submit', gradesData);
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to submit grades' };
      }
    },
  },
  
  // ==================
  // ADMIN ENDPOINTS
  // ==================
  
  admin: {
    // Get admin dashboard overview
    getOverview: async () => {
      try {
        const response = await api.get('/admin/dashboard');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch admin dashboard' };
      }
    },
    
    // Get all users
    getUsers: async (role = null) => {
      try {
        const params = role ? { role } : {};
        const response = await api.get('/admin/users', { params });
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch users' };
      }
    },
    
    // Get system analytics (from Python)
    getSystemAnalytics: async () => {
      try {
        const response = await api.get('/admin/analytics/system');
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to fetch system analytics' };
      }
    },
    
    // Create new user
    createUser: async (userData) => {
      try {
        const response = await api.post('/admin/users/create', userData);
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to create user' };
      }
    },
    
    // Update user
    updateUser: async (userId, userData) => {
      try {
        const response = await api.put(`/admin/users/${userId}`, userData);
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to update user' };
      }
    },
    
    // Delete user
    deleteUser: async (userId) => {
      try {
        const response = await api.delete(`/admin/users/${userId}`);
        return response.data;
      } catch (error) {
        throw error.response?.data || { message: 'Failed to delete user' };
      }
    },
  },
};

// ==================
// COMMON ENDPOINTS
// ==================

export const commonAPI = {
  // Get user profile
  getProfile: async () => {
    try {
      const response = await api.get('/profile');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to fetch profile' };
    }
  },
  
  // Update profile
  updateProfile: async (profileData) => {
    try {
      const response = await api.put('/profile', profileData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Failed to update profile' };
    }
  },
};
