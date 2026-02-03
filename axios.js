import axios from 'axios';

// Axios Instance Configuration
// Single source for all backend communication

const api = axios.create({
  // Backend Base URL (change for production)
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  
  // Enable session cookies (CRITICAL for auth)
  withCredentials: true,
  
  // Default headers
  headers: {
    'Content-Type': 'application/json',
  },
  
  // Timeout (30 seconds)
  timeout: 30000,
});

// Request Interceptor (for debugging/logging)
api.interceptors.request.use(
  (config) => {
    // You can add auth tokens here if needed (but we use session cookies)
    // const token = localStorage.getItem('token');
    // if (token) config.headers.Authorization = `Bearer ${token}`;
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor (global error handling)
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle common errors globally
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // Unauthorized - redirect to login
          console.error('Unauthorized access - redirecting to login');
          window.location.href = '/login';
          break;
          
        case 403:
          // Forbidden - user doesn't have permission
          console.error('Access forbidden');
          break;
          
        case 404:
          console.error('Resource not found');
          break;
          
        case 500:
          console.error('Server error');
          break;
          
        default:
          console.error('API Error:', data?.message || 'Unknown error');
      }
    } else if (error.request) {
      // Network error
      console.error('Network error - check your connection');
    } else {
      console.error('Request error:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default api;
