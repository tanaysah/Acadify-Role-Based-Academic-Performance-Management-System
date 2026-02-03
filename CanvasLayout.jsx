import { Canvas } from '@react-three/fiber';
import ParticleScene from './ParticleScene';
import useCanvasConfig from './useCanvasConfig';

// Fullscreen 3D Canvas Wrapper
// Used ONLY on: Landing, Login, Signup
// NEVER used on: Dashboards

export default function CanvasLayout({ children }) {
  const canvasConfig = useCanvasConfig();
  
  return (
    <div className="relative w-full h-screen overflow-hidden bg-base-dark">
      {/* 3D Canvas Background */}
      <div className="absolute inset-0 z-0">
        <Canvas
          dpr={canvasConfig.dpr}
          camera={canvasConfig.camera}
          gl={canvasConfig.gl}
          shadows={canvasConfig.shadows}
          linear={canvasConfig.linear}
          flat={canvasConfig.flat}
          performance={canvasConfig.performance}
        >
          <ParticleScene />
        </Canvas>
      </div>
      
      {/* Dark Gradient Overlay (for better text readability) */}
      <div className="absolute inset-0 z-10 bg-gradient-to-b from-base-dark/40 via-transparent to-base-dark/60" />
      
      {/* Content Layer */}
      <div className="relative z-20 w-full h-full">
        {children}
      </div>
    </div>
  );
}
