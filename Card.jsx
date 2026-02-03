import { motion } from 'framer-motion';

// Acadify Card Component
// Glass morphism for auth pages, flat for dashboards

export default function Card({ 
  children, 
  variant = 'glass', 
  hover = false,
  className = '',
  ...props 
}) {
  
  // Variant Styles
  const variants = {
    glass: `
      bg-secondary-dark/60 backdrop-blur-glass
      border border-white/10
      shadow-xl
    `,
    flat: `
      bg-secondary-dark
      border border-white/5
    `,
  };
  
  return (
    <motion.div
      className={`
        rounded-2xl
        ${variants[variant]}
        ${className}
      `}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      whileHover={hover ? { y: -4 } : {}}
      {...props}
    >
      {children}
    </motion.div>
  );
}
