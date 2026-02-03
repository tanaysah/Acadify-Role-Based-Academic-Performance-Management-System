import { NavLink, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { useAuth } from '../../auth/useAuth';

// Sidebar Navigation Component
// Role-based menu items

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  
  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };
  
  // Role-based navigation items
  const getNavItems = () => {
    const commonItems = [
      { path: 'overview', label: 'Overview', icon: '📊' },
      { path: 'profile', label: 'Profile', icon: '👤' },
    ];
    
    const roleItems = {
      STUDENT: [
        { path: 'marks', label: 'Marks', icon: '📝' },
        { path: 'attendance', label: 'Attendance', icon: '📅' },
        { path: 'analytics', label: 'Analytics', icon: '📈' },
      ],
      TEACHER: [
        { path: 'classes', label: 'Classes', icon: '🎓' },
        { path: 'students', label: 'Students', icon: '👥' },
        { path: 'grades', label: 'Grades', icon: '✏️' },
      ],
      ADMIN: [
        { path: 'users', label: 'Users', icon: '👥' },
        { path: 'analytics', label: 'Analytics', icon: '📊' },
        { path: 'settings', label: 'Settings', icon: '⚙️' },
      ],
    };
    
    return [
      ...commonItems,
      ...(roleItems[user?.role] || []),
    ];
  };
  
  const navItems = getNavItems();
  
  return (
    <motion.aside
      className="w-64 h-screen bg-secondary-dark border-r border-white/5 flex flex-col"
      initial={{ x: -20, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ duration: 0.4 }}
    >
      {/* Logo */}
      <div className="p-6 border-b border-white/5">
        <h1 className="font-display font-medium text-2xl text-white">
          acadify
        </h1>
        <p className="text-sm text-gray-400 mt-1">
          {user?.role?.charAt(0) + user?.role?.slice(1).toLowerCase()} Portal
        </p>
      </div>
      
      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-2">
        {navItems.map((item, index) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => `
              flex items-center gap-3 px-4 py-3 rounded-lg
              font-medium text-sm
              transition-all duration-300
              ${isActive
                ? 'bg-primary/10 text-primary border-l-2 border-primary'
                : 'text-gray-400 hover:text-white hover:bg-white/5'
              }
            `}
          >
            <span className="text-lg">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
      
      {/* User Info & Logout */}
      <div className="p-4 border-t border-white/5">
        <div className="flex items-center gap-3 mb-3 px-2">
          <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary font-semibold">
            {user?.name?.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">
              {user?.name}
            </p>
            <p className="text-xs text-gray-500 truncate">
              {user?.email}
            </p>
          </div>
        </div>
        
        <button
          onClick={handleLogout}
          className="w-full px-4 py-2 rounded-lg bg-white/5 text-gray-400 hover:text-white hover:bg-white/10 transition-all duration-300 text-sm font-medium"
        >
          Logout
        </button>
      </div>
    </motion.aside>
  );
}
