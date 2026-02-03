import { motion } from 'framer-motion';

// Acadify Loader Component
// Skeleton loaders preferred over spinners

export function SpinnerLoader({ size = 'default' }) {
  const sizes = {
    small: 'h-4 w-4',
    default: 'h-8 w-8',
    large: 'h-12 w-12',
  };
  
  return (
    <div className="flex items-center justify-center">
      <motion.div
        className={`${sizes[size]} border-2 border-primary border-t-transparent rounded-full`}
        animate={{ rotate: 360 }}
        transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
      />
    </div>
  );
}

export function SkeletonLoader({ className = '', variant = 'default' }) {
  const variants = {
    default: 'h-4 w-full',
    text: 'h-4 w-3/4',
    title: 'h-6 w-1/2',
    avatar: 'h-12 w-12 rounded-full',
    card: 'h-32 w-full rounded-xl',
  };
  
  return (
    <motion.div
      className={`
        bg-secondary-dark/40
        rounded
        ${variants[variant]}
        ${className}
      `}
      animate={{
        opacity: [0.5, 0.8, 0.5],
      }}
      transition={{
        duration: 1.5,
        repeat: Infinity,
        ease: 'easeInOut',
      }}
    />
  );
}

export function TableSkeleton({ rows = 5 }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-4">
          <SkeletonLoader variant="text" className="w-1/4" />
          <SkeletonLoader variant="text" className="w-1/4" />
          <SkeletonLoader variant="text" className="w-1/4" />
          <SkeletonLoader variant="text" className="w-1/4" />
        </div>
      ))}
    </div>
  );
}

export function CardSkeleton({ count = 3 }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-6 bg-secondary-dark rounded-xl space-y-4">
          <SkeletonLoader variant="title" />
          <SkeletonLoader variant="text" />
          <SkeletonLoader variant="text" className="w-1/2" />
        </div>
      ))}
    </div>
  );
}
