import { forwardRef, useState } from 'react';
import { motion } from 'framer-motion';

// Acadify Input Component
// Premium glass-style inputs with focus glow

const Input = forwardRef(({ 
  label,
  error,
  type = 'text',
  className = '',
  ...props 
}, ref) => {
  const [isFocused, setIsFocused] = useState(false);
  
  return (
    <div className="w-full">
      {label && (
        <label className="block text-sm font-medium text-gray-300 mb-2">
          {label}
        </label>
      )}
      
      <motion.div
        className="relative"
        animate={{
          boxShadow: isFocused 
            ? '0 0 0 2px rgba(79, 156, 255, 0.3)' 
            : '0 0 0 0px rgba(79, 156, 255, 0)',
        }}
        transition={{ duration: 0.3 }}
      >
        <input
          ref={ref}
          type={type}
          className={`
            w-full px-4 py-3
            bg-secondary-dark/60 backdrop-blur-glass
            border border-white/10
            rounded-xl
            text-white placeholder-gray-500
            font-normal
            transition-all duration-300
            focus:outline-none
            focus:border-primary/50
            disabled:opacity-50 disabled:cursor-not-allowed
            ${error ? 'border-error' : ''}
            ${className}
          `}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          {...props}
        />
      </motion.div>
      
      {error && (
        <motion.p
          initial={{ opacity: 0, y: -5 }}
          animate={{ opacity: 1, y: 0 }}
          className="mt-1.5 text-sm text-error"
        >
          {error}
        </motion.p>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;
