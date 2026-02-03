// Acadify Motion System - Slow & Smooth

export const motion = {
  // Timing (0.3s - 0.6s ONLY)
  duration: {
    fast: 0.3,
    normal: 0.4,
    slow: 0.6,
  },
  
  // Easing Functions
  easing: {
    default: 'cubic-bezier(0.4, 0, 0.2, 1)',
    smooth: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
    bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
  },
  
  // Framer Motion Variants
  variants: {
    // Page Load
    pageLoad: {
      initial: { opacity: 0, scale: 0.98 },
      animate: { 
        opacity: 1, 
        scale: 1,
        transition: { duration: 0.5, ease: [0.4, 0, 0.2, 1] }
      },
    },
    
    // Fade In
    fadeIn: {
      initial: { opacity: 0 },
      animate: { 
        opacity: 1,
        transition: { duration: 0.4 }
      },
    },
    
    // Slide Up
    slideUp: {
      initial: { opacity: 0, y: 20 },
      animate: { 
        opacity: 1, 
        y: 0,
        transition: { duration: 0.4, ease: [0.4, 0, 0.2, 1] }
      },
    },
    
    // Stagger Children (for lists/cards)
    staggerContainer: {
      animate: {
        transition: {
          staggerChildren: 0.1,
        }
      }
    },
    
    // Card Lift (Hover)
    cardHover: {
      initial: { y: 0 },
      hover: { 
        y: -4,
        transition: { duration: 0.3 }
      },
    },
  },
  
  // Glow Effect (for buttons)
  glow: {
    boxShadow: [
      '0 0 20px rgba(79, 156, 255, 0.3)',
      '0 0 30px rgba(79, 156, 255, 0.6)',
      '0 0 20px rgba(79, 156, 255, 0.3)',
    ],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

// Motion Rules
export const motionRules = {
  // ✅ DO
  // - Slow & smooth transitions
  // - Motion only where it adds meaning
  // - Fade + slight scale for page loads
  // - Glow on button hover
  // - Lift on card hover
  
  // ❌ DON'T
  // - No bouncing
  // - No fast transitions
  // - No background animation in dashboards
  // - No distracting motion
};

// Canvas Animation Settings
export const canvasMotion = {
  // Slow drift for particles
  driftSpeed: 0.0005,
  rotationSpeed: 0.0003,
  
  // Mouse interaction (subtle)
  mouseInfluence: 0.02,
  
  // Performance
  maxFPS: 60,
  pixelRatio: Math.min(window.devicePixelRatio, 2),
};
