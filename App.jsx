import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './auth/useAuth';
import AuthGuard, { RoleBasedRedirect } from './auth/AuthGuard';
import DashboardLayout from './components/layout/DashboardLayout';

// Pages
import Landing from './pages/Landing';
import Login from './pages/Login';
import Signup from './pages/Signup';
import StudentDashboard from './pages/dashboard/StudentDashboard';
import TeacherDashboard from './pages/dashboard/TeacherDashboard';
import AdminDashboard from './pages/dashboard/AdminDashboard';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          
          {/* Protected Dashboard Routes */}
          <Route
            path="/dashboard"
            element={
              <AuthGuard>
                <DashboardLayout />
              </AuthGuard>
            }
          >
            {/* Redirect /dashboard to role-based dashboard */}
            <Route index element={<RoleBasedRedirect />} />
            
            {/* Student Dashboard */}
            <Route
              path="student/*"
              element={
                <AuthGuard allowedRoles={['STUDENT']}>
                  <StudentDashboard />
                </AuthGuard>
              }
            />
            
            {/* Teacher Dashboard */}
            <Route
              path="teacher/*"
              element={
                <AuthGuard allowedRoles={['TEACHER']}>
                  <TeacherDashboard />
                </AuthGuard>
              }
            />
            
            {/* Admin Dashboard */}
            <Route
              path="admin/*"
              element={
                <AuthGuard allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </AuthGuard>
              }
            />
          </Route>
          
          {/* Catch all - redirect to landing */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
