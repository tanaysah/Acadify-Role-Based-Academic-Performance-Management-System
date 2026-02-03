// Acadify Typography System - Premium & Minimal

export const typography = {
  // Font Families
  fonts: {
    primary: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    display: '"Satoshi", "Inter", -apple-system, BlinkMacSystemFont, sans-serif',
  },
  
  // Font Weights (Maximum 3)
  weights: {
    normal: 400,
    medium: 500,
    semibold: 600,
  },
  
  // Font Sizes (Hierarchy)
  sizes: {
    // Display (Hero, App Name)
    hero: '4rem',      // 64px
    h1: '3rem',        // 48px
    h2: '2rem',        // 32px
    
    // UI Text
    large: '1.25rem',  // 20px
    base: '1rem',      // 16px
    small: '0.875rem', // 14px
    tiny: '0.75rem',   // 12px
  },
  
  // Line Heights
  lineHeights: {
    tight: 1.2,
    normal: 1.5,
    relaxed: 1.75,
  },
  
  // Letter Spacing
  letterSpacing: {
    tight: '-0.02em',
    normal: '0',
    wide: '0.05em',
  },
};

// Usage Rules (Enforced in Components)
export const typographyRules = {
  // App Name / Hero → Satoshi
  appName: {
    fontFamily: typography.fonts.display,
    fontWeight: typography.weights.medium,
    fontSize: typography.sizes.hero,
  },
  
  // Headings → Satoshi
  heading: {
    fontFamily: typography.fonts.display,
    fontWeight: typography.weights.semibold,
  },
  
  // Everything Else → Inter
  body: {
    fontFamily: typography.fonts.primary,
    fontWeight: typography.weights.normal,
  },
  
  // Buttons
  button: {
    fontFamily: typography.fonts.primary,
    fontWeight: typography.weights.medium,
  },
};

// NEVER use:
// ❌ Bold everywhere
// ❌ ALL CAPS for headings
// ❌ More than 3 weights
// ❌ More than 2 font families
