import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth';
import { SpinnerLoader } from '../components/ui/Loader';

// Protected Route Wrapper
// Redirects to login if not authenticated
// Redirects to correct dashboard based on role

export default function AuthGuard({ children, allowedRoles = [] }) {
  const { user, loading, isAuthenticated } = useAuth();
  const location = useLocation();
  
  // Show loader while checking session
  if (loading) {
    return (
      <div className="min-h-screen bg-base-dark flex items-center justify-center">
        <SpinnerLoader size="large" />
      </div>
    );
  }
  
  // Not authenticated - redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  
  // Check role-based access
  if (allowedRoles.length > 0 && !allowedRoles.includes(user?.role)) {
    // User doesn't have permission - redirect to their dashboard
    const redirectPath = getRoleBasedPath(user.role);
    return <Navigate to={redirectPath} replace />;
  }
  
  // All checks passed - render children
  return children;
}

// Get dashboard path based on user role
function getRoleBasedPath(role) {
  const paths = {
    STUDENT: '/dashboard/student',
    TEACHER: '/dashboard/teacher',
    ADMIN: '/dashboard/admin',
  };
  
  return paths[role] || '/login';
}

// Helper component to redirect to role-based dashboard
export function RoleBasedRedirect() {
  const { user } = useAuth();
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  const path = getRoleBasedPath(user.role);
  return <Navigate to={path} replace />;
}
