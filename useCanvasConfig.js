import { useMemo } from 'react';

// Canvas Performance Configuration
// Ensures smooth 60fps and caps pixel ratio for performance

export default function useCanvasConfig() {
  const config = useMemo(() => ({
    // Cap pixel ratio to prevent excessive rendering on high-DPI displays
    dpr: Math.min(window.devicePixelRatio, 2),
    
    // Performance mode
    performance: {
      current: 1,
      min: 0.5,
      max: 1,
      debounce: 200,
    },
    
    // Frame rate cap
    frameloop: 'always',
    
    // Camera settings
    camera: {
      position: [0, 0, 20],
      fov: 75,
      near: 0.1,
      far: 1000,
    },
    
    // WebGL settings
    gl: {
      antialias: true,
      alpha: true,
      powerPreference: 'high-performance',
    },
    
    // Disable shadows (not needed for particles)
    shadows: false,
    
    // Linear tone mapping (better for glows)
    linear: true,
    flat: true,
  }), []);
  
  return config;
}
