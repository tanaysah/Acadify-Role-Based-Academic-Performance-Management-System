import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Routes, Route, Navigate } from 'react-router-dom';
import Topbar from '../../components/layout/Topbar';
import Card from '../../components/ui/Card';
import { CardSkeleton } from '../../components/ui/Loader';
import ErrorCard from '../../components/ui/ErrorCard';
import { dashboardAPI } from '../../api/dashboard.api';

export default function TeacherDashboard() {
  return (
    <div className="min-h-screen">
      <Routes>
        <Route path="overview" element={<Overview />} />
        <Route path="classes" element={<Classes />} />
        <Route path="students" element={<Students />} />
        <Route path="grades" element={<Grades />} />
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
      
      const overview = await dashboardAPI.teacher.getOverview();
      
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
        <Topbar title="Teacher Overview" />
        <motion.div
          className="p-6 space-y-6"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.5 }}
        >
          <CardSkeleton count={3} />
        </motion.div>
      </div>
    );
  }
  
  if (error) return <ErrorCard message={error} onRetry={loadData} />;
  
  return (
    <div>
      <Topbar title="Teacher Overview" />
      
      <motion.div
        className="p-6 space-y-6"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <StatsCard
              title="Total Classes"
              value={data?.totalClasses || 0}
            />
            <StatsCard
              title="Total Students"
              value={data?.totalStudents || 0}
            />
            <StatsCard
              title="Pending Grades"
              value={data?.pendingGrades || 0}
            />
          </div>
      </motion.div>
    </div>
  );
}

// Classes Page
function Classes() {
  const [classes, setClasses] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    loadClasses();
  }, []);
  
  const loadClasses = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const data = await dashboardAPI.teacher.getClasses();
      
      if (!data.success) {
        throw new Error(data.message || 'Failed to load classes');
      }
      
      setClasses(data.data);
    } catch (err) {
      console.error('Failed to load classes:', err);
      setError(err.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return (
      <div>
        <Topbar title="My Classes" />
        <div className="p-6">
          <CardSkeleton count={3} />
        </div>
      </div>
    );
  }
  
  if (error) return <ErrorCard message={error} onRetry={loadClasses} />;
  
  return (
    <div>
      <Topbar title="My Classes" />
      
      <div className="p-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {classes?.map((classItem, i) => (
              <ClassCard key={i} classData={classItem} />
            )) || (
              <Card variant="flat" className="p-6 col-span-full">
                <p className="text-gray-400">No classes assigned</p>
              </Card>
            )}
          </div>
      </div>
    </div>
  );
}

// Students Page
function Students() {
  return (
    <div>
      <Topbar title="Students" />
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <p className="text-gray-400">Student list coming soon...</p>
        </Card>
      </div>
    </div>
  );
}

// Grades Page
function Grades() {
  return (
    <div>
      <Topbar title="Grade Management" />
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <p className="text-gray-400">Grade submission interface coming soon...</p>
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
function StatsCard({ title, value }) {
  return (
    <Card variant="flat" className="p-6" hover>
      <p className="text-sm text-gray-400 mb-2">{title}</p>
      <h3 className="font-display font-semibold text-3xl text-white">{value}</h3>
    </Card>
  );
}

function ClassCard({ classData }) {
  return (
    <Card variant="flat" className="p-6" hover>
      <h4 className="font-display font-semibold text-lg text-white mb-2">
        {classData.name}
      </h4>
      <p className="text-sm text-gray-400 mb-3">{classData.subject}</p>
      <div className="flex items-center justify-between text-xs text-gray-500">
        <span>{classData.students} students</span>
        <span>{classData.schedule}</span>
      </div>
    </Card>
  );
}
