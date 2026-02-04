import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Routes, Route, Navigate } from 'react-router-dom';
import Topbar from '../../components/layout/Topbar';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { CardSkeleton, TableSkeleton } from '../../components/ui/Loader';
import ErrorCard from '../../components/ui/ErrorCard';
import { dashboardAPI } from '../../api/dashboard.api';

export default function AdminDashboard() {
  return (
    <div className="min-h-screen">
      <Routes>
        <Route path="overview" element={<Overview />} />
        <Route path="users" element={<Users />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="settings" element={<Settings />} />
        <Route path="profile" element={<Profile />} />
        <Route path="*" element={<Navigate to="overview" replace />} />
      </Routes>
    </div>
  );
}

// Overview Page
function Overview() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    loadData();
  }, []);
  
  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const overview = await dashboardAPI.admin.getOverview();
      
      if (!overview.success) {
        throw new Error(overview.message || 'Failed to load overview');
      }
      
      setData(overview.data);
    } catch (err) {
      console.error('Failed to load overview:', err);
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return (
      <div>
        <Topbar title="Admin Overview" />
        <motion.div
          className="p-6 space-y-6"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.5 }}
        >
          <CardSkeleton count={4} />
        </motion.div>
      </div>
    );
  }
  
  if (error) return <ErrorCard message={error} onRetry={loadData} />;
  
  return (
    <div>
      <Topbar title="Admin Overview" />
      
      <motion.div
        className="p-6 space-y-6"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <StatsCard
              title="Total Users"
              value={data?.totalUsers || 0}
              icon="👥"
            />
            <StatsCard
              title="Students"
              value={data?.totalStudents || 0}
              icon="🎓"
            />
            <StatsCard
              title="Teachers"
              value={data?.totalTeachers || 0}
              icon="👨‍🏫"
            />
            <StatsCard
              title="Active Sessions"
              value={data?.activeSessions || 0}
              icon="🔒"
            />
          </div>
      </motion.div>
    </div>
  );
}

// Users Management Page
function Users() {
  const [users, setUsers] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedRole, setSelectedRole] = useState(null);
  
  useEffect(() => {
    loadUsers();
  }, [selectedRole]);
  
  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await dashboardAPI.admin.getUsers(selectedRole);
      
      if (!data.success) {
        throw new Error(data.message || 'Failed to load users');
      }
      
      setUsers(data.data);
    } catch (err) {
      console.error('Failed to load users:', err);
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (error) return <ErrorCard message={error} onRetry={loadUsers} />;
  
  return (
    <div>
      <Topbar
        title="User Management"
        actions={
          <Button size="small">Add New User</Button>
        }
      />
      
      <div className="p-6">
        {/* Role Filter */}
        <div className="mb-6 flex gap-3">
          {[
            { label: 'All Users', value: null },
            { label: 'Students', value: 'STUDENT' },
            { label: 'Teachers', value: 'TEACHER' },
            { label: 'Admins', value: 'ADMIN' },
          ].map((filter) => (
            <button
              key={filter.label}
              onClick={() => setSelectedRole(filter.value)}
              className={`
                px-4 py-2 rounded-lg text-sm font-medium
                transition-all duration-300
                ${selectedRole === filter.value
                  ? 'bg-primary text-white'
                  : 'bg-secondary-dark text-gray-400 hover:text-white'
                }
              `}
            >
              {filter.label}
            </button>
          ))}
        </div>
        
        <Card variant="flat" className="p-6">
          {loading ? (
            <TableSkeleton rows={8} />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-white/5">
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Name</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Email</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Role</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Status</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users?.map((user, i) => (
                    <tr key={i} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                      <td className="py-3 px-4 text-sm text-white">{user.name}</td>
                      <td className="py-3 px-4 text-sm text-gray-400">{user.email}</td>
                      <td className="py-3 px-4">
                        <span className="text-xs px-2 py-1 rounded bg-primary/20 text-primary">
                          {user.role}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`text-xs px-2 py-1 rounded ${
                          user.active ? 'bg-success/20 text-success' : 'bg-gray-700 text-gray-400'
                        }`}>
                          {user.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex gap-2">
                          <button className="text-xs text-primary hover:text-primary-hover">
                            Edit
                          </button>
                          <button className="text-xs text-error hover:text-red-400">
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  )) || (
                    <tr>
                      <td colSpan="5" className="py-6 text-center text-gray-500 text-sm">
                        No users found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}

// Analytics Page
function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    loadAnalytics();
  }, []);
  
  const loadAnalytics = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await dashboardAPI.admin.getSystemAnalytics();
      
      if (!data.success) {
        throw new Error(data.message || 'Failed to load analytics');
      }
      
      setAnalytics(data.data);
    } catch (err) {
      console.error('Failed to load analytics:', err);
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return (
      <div>
        <Topbar title="System Analytics" />
        <div className="p-6 space-y-6">
          <CardSkeleton count={3} />
        </div>
      </div>
    );
  }
  
  if (error) return <ErrorCard message={error} onRetry={loadAnalytics} />;
  
  return (
    <div>
      <Topbar title="System Analytics" />
      
      <div className="p-6 space-y-6">
        <Card variant="flat" className="p-6">
              <h3 className="font-display font-semibold text-lg text-white mb-4">
                Platform Insights
              </h3>
              <p className="text-gray-400">
                Python-powered system analytics will be displayed here
              </p>
            </Card>
      </div>
    </div>
  );
}

// Settings Page
function Settings() {
  return (
    <div>
      <Topbar title="Settings" />
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <p className="text-gray-400">System settings coming soon...</p>
        </Card>
      </div>
    </div>
  );
}

// Profile Page
function Profile() {
  return (
    <div>
      <Topbar title="Profile" />
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <p className="text-gray-400">Profile settings coming soon...</p>
        </Card>
      </div>
    </div>
  );
}

// Helper Components
function StatsCard({ title, value, icon }) {
  return (
    <Card variant="flat" className="p-6" hover>
      <div className="flex items-start justify-between mb-2">
        <p className="text-sm text-gray-400">{title}</p>
        <span className="text-2xl">{icon}</span>
      </div>
      <h3 className="font-display font-semibold text-3xl text-white">{value}</h3>
    </Card>
  );
}
