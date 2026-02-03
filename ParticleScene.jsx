import { useRef, useMemo } from 'react';
import { useFrame } from '@react-three/fiber';
import * as THREE from 'three';

// Abstract Particle Field (Blue → Purple Glow)
// Premium, Neutral, GPU-Light, Non-Distracting

export default function ParticleScene() {
  const particlesRef = useRef();
  const mousePosition = useRef({ x: 0, y: 0 });
  
  // Particle Configuration
  const particleCount = 3000; // Light on GPU
  
  // Generate particle positions
  const particles = useMemo(() => {
    const positions = new Float32Array(particleCount * 3);
    const colors = new Float32Array(particleCount * 3);
    
    for (let i = 0; i < particleCount; i++) {
      const i3 = i * 3;
      
      // Spread particles in 3D space
      positions[i3] = (Math.random() - 0.5) * 50;
      positions[i3 + 1] = (Math.random() - 0.5) * 50;
      positions[i3 + 2] = (Math.random() - 0.5) * 30;
      
      // Color gradient: Blue → Purple
      const mixFactor = Math.random();
      
      // Electric Blue: #4f9cff (79, 156, 255)
      // Soft Purple: #7c7cff (124, 124, 255)
      colors[i3] = (79 + mixFactor * (124 - 79)) / 255;
      colors[i3 + 1] = (156 + mixFactor * (124 - 156)) / 255;
      colors[i3 + 2] = 1.0; // Both blues have full blue channel
    }
    
    return { positions, colors };
  }, []);
  
  // Mouse tracking (subtle interaction)
  useMemo(() => {
    const handleMouseMove = (e) => {
      mousePosition.current = {
        x: (e.clientX / window.innerWidth) * 2 - 1,
        y: -(e.clientY / window.innerHeight) * 2 + 1,
      };
    };
    
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);
  
  // Animation loop (slow drift)
  useFrame((state) => {
    if (!particlesRef.current) return;
    
    const time = state.clock.getElapsedTime();
    
    // Slow rotation
    particlesRef.current.rotation.y = time * 0.03;
    particlesRef.current.rotation.x = Math.sin(time * 0.02) * 0.1;
    
    // Subtle mouse interaction
    const targetRotationY = mousePosition.current.x * 0.1;
    const targetRotationX = mousePosition.current.y * 0.1;
    
    particlesRef.current.rotation.y += (targetRotationY - particlesRef.current.rotation.y) * 0.02;
    particlesRef.current.rotation.x += (targetRotationX - particlesRef.current.rotation.x) * 0.02;
  });
  
  return (
    <points ref={particlesRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={particleCount}
          array={particles.positions}
          itemSize={3}
        />
        <bufferAttribute
          attach="attributes-color"
          count={particleCount}
          array={particles.colors}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial
        size={0.15}
        vertexColors
        transparent
        opacity={0.6}
        sizeAttenuation
        blending={THREE.AdditiveBlending}
        depthWrite={false}
      />
    </points>
  );
}
