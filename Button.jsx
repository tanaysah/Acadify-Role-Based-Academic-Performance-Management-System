import { motion } from 'framer-motion';
import { forwardRef } from 'react';

// Acadify Button Component
// Variants: primary, secondary, ghost

const Button = forwardRef(({ 
  children, 
  variant = 'primary', 
  size = 'default',
  loading = false,
  disabled = false,
  className = '',
  onClick,
  type = 'button',
  ...props 
}, ref) => {
  
  // Variant Styles
  const variants = {
    primary: `
      bg-primary text-white font-medium
      hover:bg-primary-hover
      disabled:opacity-50 disabled:cursor-not-allowed
      transition-all duration-300
      hover:shadow-[0_0_30px_rgba(79,156,255,0.6)]
    `,
    secondary: `
      bg-secondary-dark text-gray-200 font-medium border border-white/10
      hover:border-primary hover:text-white
      disabled:opacity-50 disabled:cursor-not-allowed
      transition-all duration-300
    `,
    ghost: `
      bg-transparent text-gray-300 font-medium
      hover:text-primary
      disabled:opacity-50 disabled:cursor-not-allowed
      transition-all duration-300
    `,
  };
  
  // Size Styles
  const sizes = {
    small: 'px-4 py-2 text-sm rounded-lg',
    default: 'px-6 py-3 text-base rounded-xl',
    large: 'px-8 py-4 text-lg rounded-xl',
  };
  
  return (
    <motion.button
      ref={ref}
      type={type}
      disabled={disabled || loading}
      onClick={onClick}
      className={`
        ${variants[variant]}
        ${sizes[size]}
        ${className}
        relative overflow-hidden
      `}
      whileHover={{ scale: disabled || loading ? 1 : 1.02 }}
      whileTap={{ scale: disabled || loading ? 1 : 0.98 }}
      {...props}
    >
      {loading ? (
        <div className="flex items-center justify-center gap-2">
          <svg 
            className="animate-spin h-5 w-5" 
            xmlns="http://www.w3.org/2000/svg" 
            fill="none" 
            viewBox="0 0 24 24"
          >
            <circle 
              className="opacity-25" 
              cx="12" 
              cy="12" 
              r="10" 
              stroke="currentColor" 
              strokeWidth="4"
            />
            <path 
              className="opacity-75" 
              fill="currentColor" 
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          <span>Loading...</span>
        </div>
      ) : (
        children
      )}
    </motion.button>
  );
});

Button.displayName = 'Button';

export default Button;
