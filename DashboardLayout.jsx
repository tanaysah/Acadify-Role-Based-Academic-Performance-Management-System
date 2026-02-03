import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

// Dashboard Layout Wrapper
// NO 3D Canvas - Performance First
// Flat dark background

export default function DashboardLayout() {
  return (
    <div className="flex h-screen bg-base-dark overflow-hidden">
      {/* Sidebar */}
      <Sidebar />
      
      {/* Main Content Area */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
