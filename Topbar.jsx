import { motion } from 'framer-motion';

// Topbar Component
// Shows page title and actions

export default function Topbar({ title, actions }) {
  return (
    <motion.header
      className="h-16 bg-base-dark border-b border-white/5 px-6 flex items-center justify-between"
      initial={{ y: -10, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.4 }}
    >
      {/* Page Title */}
      <h2 className="font-display font-semibold text-xl text-white">
        {title}
      </h2>
      
      {/* Actions (optional) */}
      {actions && (
        <div className="flex items-center gap-3">
          {actions}
        </div>
      )}
    </motion.header>
  );
}
