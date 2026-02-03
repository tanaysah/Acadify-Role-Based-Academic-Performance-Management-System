import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Routes, Route, Navigate } from 'react-router-dom';
import Topbar from '../../components/layout/Topbar';
import Card from '../../components/ui/Card';
import { CardSkeleton, TableSkeleton } from '../../components/ui/Loader';
import { dashboardAPI } from '../../api/dashboard.api';

export default function StudentDashboard() {
  return (
    <div className="min-h-screen">
      <Routes>
        <Route path="overview" element={<Overview />} />
        <Route path="marks" element={<Marks />} />
        <Route path="attendance" element={<Attendance />} />
        <Route path="analytics" element={<Analytics />} />
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
  
  useEffect(() => {
    loadData();
  }, []);
  
  const loadData = async () => {
    try {
      const overview = await dashboardAPI.student.getOverview();
      setData(overview);
    } catch (err) {
      console.error('Failed to load overview:', err);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div>
      <Topbar title="Overview" />
      
      <motion.div
        className="p-6 space-y-6"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5 }}
      >
        {loading ? (
          <CardSkeleton count={3} />
        ) : (
          <>
            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <StatsCard
                title="Average Score"
                value={data?.avgScore || '0%'}
                trend="+5%"
                trendUp={true}
              />
              <StatsCard
                title="Attendance"
                value={data?.attendance || '0%'}
                trend="-2%"
                trendUp={false}
              />
              <StatsCard
                title="Rank"
                value={data?.rank || 'N/A'}
                subtitle={`out of ${data?.totalStudents || 0}`}
              />
            </div>
            
            {/* Recent Activity */}
            <Card variant="flat" className="p-6">
              <h3 className="font-display font-semibold text-lg text-white mb-4">
                Recent Activity
              </h3>
              <div className="space-y-3">
                {data?.recentActivity?.map((activity, i) => (
                  <ActivityItem key={i} activity={activity} />
                )) || (
                  <p className="text-gray-500 text-sm">No recent activity</p>
                )}
              </div>
            </Card>
          </>
        )}
      </motion.div>
    </div>
  );
}

// Marks Page
function Marks() {
  const [marks, setMarks] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadMarks();
  }, []);
  
  const loadMarks = async () => {
    try {
      const data = await dashboardAPI.student.getMarks();
      setMarks(data);
    } catch (err) {
      console.error('Failed to load marks:', err);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div>
      <Topbar title="Marks" />
      
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <h3 className="font-display font-semibold text-lg text-white mb-4">
            Your Grades
          </h3>
          
          {loading ? (
            <TableSkeleton rows={5} />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-white/5">
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Subject</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Score</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Grade</th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-gray-400">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {marks?.subjects?.map((subject, i) => (
                    <tr key={i} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                      <td className="py-3 px-4 text-sm text-white">{subject.name}</td>
                      <td className="py-3 px-4 text-sm text-white">{subject.score}</td>
                      <td className="py-3 px-4 text-sm text-white">{subject.grade}</td>
                      <td className="py-3 px-4">
                        <span className={`text-xs px-2 py-1 rounded ${
                          subject.status === 'Pass' ? 'bg-success/20 text-success' : 'bg-error/20 text-error'
                        }`}>
                          {subject.status}
                        </span>
                      </td>
                    </tr>
                  )) || (
                    <tr>
                      <td colSpan="4" className="py-6 text-center text-gray-500 text-sm">
                        No marks available
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

// Attendance Page
function Attendance() {
  return (
    <div>
      <Topbar title="Attendance" />
      <div className="p-6">
        <Card variant="flat" className="p-6">
          <p className="text-gray-400">Attendance tracking coming soon...</p>
        </Card>
      </div>
    </div>
  );
}

// Analytics Page (with Python-powered insights)
function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadAnalytics();
  }, []);
  
  const loadAnalytics = async () => {
    try {
      const data = await dashboardAPI.student.getPerformanceAnalytics();
      setAnalytics(data);
    } catch (err) {
      console.error('Failed to load analytics:', err);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div>
      <Topbar title="Performance Analytics" />
      
      <div className="p-6 space-y-6">
        {loading ? (
          <CardSkeleton count={3} />
        ) : (
          <>
            <Card variant="flat" className="p-6">
              <h3 className="font-display font-semibold text-lg text-white mb-4">
                AI-Powered Insights
              </h3>
              <div className="space-y-4">
                <InsightCard
                  title="Performance Trend"
                  value={analytics?.trend || 'Stable'}
                  description="Based on last 3 months of data"
                />
                <InsightCard
                  title="Risk Level"
                  value={analytics?.riskLevel || 'Low'}
                  description="Predicted academic risk"
                />
              </div>
            </Card>
          </>
        )}
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
function StatsCard({ title, value, trend, trendUp, subtitle }) {
  return (
    <Card variant="flat" className="p-6" hover>
      <p className="text-sm text-gray-400 mb-2">{title}</p>
      <div className="flex items-baseline gap-2">
        <h3 className="font-display font-semibold text-3xl text-white">{value}</h3>
        {trend && (
          <span className={`text-sm ${trendUp ? 'text-success' : 'text-error'}`}>
            {trend}
          </span>
        )}
      </div>
      {subtitle && <p className="text-xs text-gray-500 mt-1">{subtitle}</p>}
    </Card>
  );
}

function ActivityItem({ activity }) {
  return (
    <div className="flex items-start gap-3 p-3 rounded-lg hover:bg-white/5 transition-colors">
      <div className="w-2 h-2 rounded-full bg-primary mt-2" />
      <div className="flex-1">
        <p className="text-sm text-white">{activity.title}</p>
        <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
      </div>
    </div>
  );
}

function InsightCard({ title, value, description }) {
  return (
    <div className="p-4 rounded-lg bg-white/5 border border-white/5">
      <p className="text-sm text-gray-400 mb-1">{title}</p>
      <p className="text-lg font-semibold text-white mb-1">{value}</p>
      <p className="text-xs text-gray-500">{description}</p>
    </div>
  );
}
