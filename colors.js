// Acadify Brand Colors - LOCKED & FINAL
export const colors = {
  // Base
  baseDark: '#0b1020',
  secondaryDark: '#0f172a',
  
  // Primary Accent (Trust & Intelligence)
  primary: '#4f9cff',
  primaryHover: '#6badff',
  primaryGlow: 'rgba(79, 156, 255, 0.3)',
  
  // Secondary Accent (Innovation)
  secondary: '#7c7cff',
  secondaryGlow: 'rgba(124, 124, 255, 0.3)',
  
  // Status
  success: '#22c55e',
  error: '#ef4444',
  warning: '#f59e0b',
  
  // Neutrals
  textPrimary: '#f9fafb',
  textSecondary: '#9ca3af',
  textMuted: '#6b7280',
  
  // Glass/Overlay
  glassBackground: 'rgba(15, 23, 42, 0.6)',
  overlayDark: 'rgba(11, 16, 32, 0.8)',
  
  // Borders
  borderSubtle: 'rgba(255, 255, 255, 0.1)',
  borderPrimary: 'rgba(79, 156, 255, 0.2)',
};

// CSS Custom Properties for runtime theme switching (if needed)
export const setCSSVariables = () => {
  const root = document.documentElement;
  Object.entries(colors).forEach(([key, value]) => {
    root.style.setProperty(`--color-${key}`, value);
  });
};
